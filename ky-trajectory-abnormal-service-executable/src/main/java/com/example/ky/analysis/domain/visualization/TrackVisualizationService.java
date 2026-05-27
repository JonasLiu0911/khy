package com.example.ky.analysis.domain.visualization;

import com.example.ky.analysis.domain.common.AbnormalLevel;
import com.example.ky.analysis.domain.geo.CoordinateTransformService;
import com.example.ky.analysis.domain.geo.GeoPoint;
import com.example.ky.analysis.domain.line.model.LineStation;
import com.example.ky.analysis.domain.line.port.LineStationDataPort;
import com.example.ky.analysis.domain.result.AbnormalResultService;
import com.example.ky.analysis.domain.result.model.VehicleAbnormalResult;
import com.example.ky.analysis.domain.vehicle.model.GpsPoint;
import com.example.ky.analysis.domain.vehicle.port.GpsTrackDataPort;
import com.example.ky.analysis.domain.visualization.model.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 轨迹异常检测结果可视化聚合服务。
 *
 * 架构位置：领域层应用服务。
 * 设计原则：
 * 1. 不重新执行异常检测算法，只读取已沉淀的 VehicleAbnormalResult；
 * 2. 复用既有 GpsTrackDataPort、LineStationDataPort 等领域端口；
 * 3. 将检测窗口内的 GPS 点按自然日组织为 dailyTracks，方便前端叠加展示每天轨迹；
 * 4. 对单日轨迹点做轻量抽稀，避免前端地图一次渲染过多点。
 */
@Service
public class TrackVisualizationService {

    private static final int DEFAULT_WINDOW_DAYS = 30;
    private static final int MIN_POINTS_PER_DAY = 50;
    private static final int MAX_POINTS_PER_DAY_LIMIT = Integer.MAX_VALUE;

    private final AbnormalResultService abnormalResultService;
    private final GpsTrackDataPort gpsTrackDataPort;
    private final LineStationDataPort lineStationDataPort;
    private final CoordinateTransformService coordinateTransformService;

    public TrackVisualizationService(AbnormalResultService abnormalResultService,
                                     GpsTrackDataPort gpsTrackDataPort,
                                     LineStationDataPort lineStationDataPort,
                                     CoordinateTransformService coordinateTransformService) {
        this.abnormalResultService = abnormalResultService;
        this.gpsTrackDataPort = gpsTrackDataPort;
        this.lineStationDataPort = lineStationDataPort;
        this.coordinateTransformService = coordinateTransformService;
    }

    public TrackVisualizationResult buildVehicleTrackVisualization(String vehicleId,
                                                                   LocalDate statDate,
                                                                   boolean includeStations,
                                                                   int maxPointsPerDay) {
        VehicleAbnormalResult result = abnormalResultService.getDetail(vehicleId, statDate)
                .orElseThrow(() -> new IllegalArgumentException("未找到该车辆在指定统计日的异常检测结果"));

        LocalDateTime windowStart = result.getWindowStart();
        LocalDateTime windowEnd = result.getWindowEnd();
        if (windowStart == null || windowEnd == null) {
            windowStart = statDate.minusDays(DEFAULT_WINDOW_DAYS).atStartOfDay();
            windowEnd = statDate.minusDays(1).atTime(23, 59, 59);
        }

        List<GpsPoint> gpsPoints = gpsTrackDataPort.queryGpsPoints(vehicleId, windowStart, windowEnd);
        List<DailyTrackView> dailyTracks = buildDailyTracks(gpsPoints, normalizeMaxPoints(maxPointsPerDay));

        List<StationView> stations = includeStations && result.getLineId() != null
                ? buildStationViews(lineStationDataPort.queryLineStationsForVisualize(result.getLineId()))
                : List.of();

        VehicleView vehicle = new VehicleView(
                result.getVehicleId(),
                result.getPlateNo(),
                result.getLineId(),
                result.getLineName()
        );

        AbnormalResultView abnormal = new AbnormalResultView(
                result.getAbnormalLevel(),
                result.getTrackScore(),
                result.getEventScore(),
                result.getFinalScore(),
                result.isConflictFlag(),
                result.getEvidenceSummary(),
                result.getSuggestion(),
                result.getAlgorithmVersion()
        );

        Map<String, Object> evidence = new LinkedHashMap<>();
        evidence.put("evidenceSummary", result.getEvidenceSummary());
        evidence.put("suggestion", result.getSuggestion());
        evidence.put("trackAnalysis", result.getTrackAnalysis());
        evidence.put("stationEventAnalysis", result.getStationEventAnalysis());

        return new TrackVisualizationResult(
                vehicle,
                new AnalysisWindowView(statDate, windowStart, windowEnd, DEFAULT_WINDOW_DAYS),
                abnormal,
                stations,
                dailyTracks,
                evidence
        );
    }

    public List<TrackVisualizationResult> getAbnormalLevelVehiclesDetail(AbnormalLevel abnormalLevel, LocalDate statDate) {
        List<VehicleAbnormalResult> results  = abnormalResultService.getDetailByLevel(abnormalLevel, statDate);
        if (results == null || results.isEmpty()) {
            return List.of();
        }

        return results.stream().map(result -> {
            VehicleView vehicle = new VehicleView(
                    result.getVehicleId(),
                    result.getPlateNo(),
                    result.getLineId(),
                    result.getLineName()
            );
            AbnormalResultView abnormal = new AbnormalResultView(
                    result.getAbnormalLevel(),
                    result.getTrackScore(),
                    result.getEventScore(),
                    result.getFinalScore(),
                    result.isConflictFlag(),
                    result.getEvidenceSummary(),
                    result.getSuggestion(),
                    result.getAlgorithmVersion()
            );
            return new TrackVisualizationResult(
                    vehicle,
                    null,
                    abnormal,
                    null,
                    null,
                    null
            );
        }).toList();
    }

    private int normalizeMaxPoints(int maxPointsPerDay) {
        if (maxPointsPerDay <= 0) {
            return 1200;
        }
        return Math.min(Math.max(maxPointsPerDay, MIN_POINTS_PER_DAY), MAX_POINTS_PER_DAY_LIMIT);
    }

    private List<StationView> buildStationViews(List<LineStation> stations) {
        if (stations == null || stations.isEmpty()) {
            return List.of();
        }

        Map<String, LineStation> deduped = new LinkedHashMap<>();
        for (LineStation s : stations) {
            String key = String.valueOf(s.stationId());
            deduped.putIfAbsent(key, s);
        }

        return deduped.values().stream()
                .sorted(Comparator.comparing(LineStation::stationOrder, Comparator.nullsLast(Integer::compareTo)))
                .map(s -> new StationView(
                        s.lineId(),
                        s.lineName(),
                        s.stationId(),
                        s.stationName(),
                        s.longitude(), // 直接用原始经度
                        s.latitude(),  // 直接用原始纬度
                        s.stationOrder(),
                        s.keyStation()
                ))
                .toList();
    }

    private List<DailyTrackView> buildDailyTracks(List<GpsPoint> gpsPoints, int maxPointsPerDay) {
        if (gpsPoints == null || gpsPoints.isEmpty()) {
            return List.of();
        }

        Map<LocalDate, List<GpsPoint>> grouped = gpsPoints.stream()
                .filter(p -> p.gpsTime() != null)
                .sorted(Comparator.comparing(GpsPoint::gpsTime))
                .collect(Collectors.groupingBy(
                        p -> p.gpsTime().toLocalDate(),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        List<DailyTrackView> result = new ArrayList<>();
        for (Map.Entry<LocalDate, List<GpsPoint>> entry : grouped.entrySet()) {
            List<GpsPoint> dayPoints = entry.getValue();
            List<GpsPoint> sampled = sample(dayPoints, maxPointsPerDay);
            List<TrackPointView> pointViews = sampled.stream()
                    .map(p -> new TrackPointView(p.gpsTime(), p.glongitude(), p.glatitude(), p.glongitude(), p.glatitude(), p.speed()))
                    .toList();

            LocalDateTime startTime = dayPoints.get(0).gpsTime();
            LocalDateTime endTime = dayPoints.get(dayPoints.size() - 1).gpsTime();
            double distanceKm = calcDistanceKm(sampled);
            String dayLevel = dayPoints.size() < 8 ? "DATA_INSUFFICIENT" : "NORMAL";

            result.add(new DailyTrackView(
                    entry.getKey(),
                    dayPoints.size(),
                    distanceKm,
                    startTime,
                    endTime,
                    dayLevel,
                    pointViews
            ));
        }
        return result;
    }

    /**
     * 按序均匀抽稀。生产环境如需更精细展示，可替换为 Douglas-Peucker 或按时间/距离混合抽稀。
     */
    private List<GpsPoint> sample(List<GpsPoint> points, int maxPoints) {
        if (points.size() <= maxPoints) {
            return points;
        }
        List<GpsPoint> sampled = new ArrayList<>(maxPoints);
        double step = (points.size() - 1) * 1.0 / (maxPoints - 1);
        for (int i = 0; i < maxPoints; i++) {
            sampled.add(points.get((int) Math.round(i * step)));
        }
        return sampled;
    }

    private double calcDistanceKm(List<GpsPoint> points) {
        if (points == null || points.size() < 2) {
            return 0.0;
        }
        double meters = 0.0;
        for (int i = 1; i < points.size(); i++) {
            GpsPoint previous = points.get(i - 1);
            GpsPoint current = points.get(i);
            meters += haversineMeters(previous.glatitude(), previous.glongitude(), current.glatitude(), current.glongitude());
        }
        return Math.round((meters / 1000.0) * 100.0) / 100.0;
    }

    private double haversineMeters(double lat1, double lon1, double lat2, double lon2) {
        double r = 6371000.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return 2 * r * Math.asin(Math.sqrt(a));
    }
}
