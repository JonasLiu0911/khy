package com.fjkhy.abnormal.domain.business.vehicle.servicerange;

import com.fjkhy.abnormal.domain.common.CalcStatus;
import com.fjkhy.abnormal.domain.common.ObjectType;
import com.fjkhy.abnormal.domain.common.RiskLevel;
import com.fjkhy.abnormal.domain.detection.AbnormalDetector;
import com.fjkhy.abnormal.domain.detection.DetectionContext;
import com.fjkhy.abnormal.domain.result.AbnormalResult;
import com.fjkhy.abnormal.domain.result.EvidenceRecord;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 车辆跨乡镇/跨县运行辅助检测：一期实现为“疑似线路服务范围外运行”。
 *
 * 当前算法不依赖乡镇、区县行政边界 polygon，不能输出确定性跨乡镇/跨县结论；
 * 它基于车辆绑定线路集合、线路站点序列、车辆GPS轨迹、全量站点邻近关系和过站记录，
 * 输出“疑似线路服务范围外运行”辅助提示。
 */
@Component
public class VehicleServiceRangeOutOfScopeDetector implements AbnormalDetector {
    public static final String CODE = "VEHICLE_SERVICE_RANGE_OUT_OF_SCOPE";
    private static final String CATEGORY = "车辆资质与运行合规异常";
    private static final String SUBTYPE = "疑似线路服务范围外运行";

    private final VehicleServiceRangeRepository repository;
    private final VehicleServiceRangeConfig config;

    public VehicleServiceRangeOutOfScopeDetector(VehicleServiceRangeRepository repository,
                                                 VehicleServiceRangeConfig config) {
        this.repository = repository;
        this.config = config;
    }

    @Override public String detectorCode() { return CODE; }
    @Override public String detectorName() { return "车辆跨乡镇/跨县运行辅助检测（服务范围外运行）"; }
    @Override public String objectTypeName() { return "车辆"; }
    @Override public String abnormalCategory() { return CATEGORY; }
    @Override public String abnormalSubtype() { return SUBTYPE; }

    @Override
    public boolean supports(DetectionContext context) {
        return context.objectType() == ObjectType.VEHICLE || context.objectType() == ObjectType.ALL;
    }

    @Override
    public List<AbnormalResult> detect(DetectionContext context) {
        if (!repository.configured()) {
            return List.of(notConfiguredResult(context));
        }

        DetectionPeriod period = parsePeriod(context.period());
        List<VehicleServiceRangeRepository.VehicleKey> vehicles = repository.findCandidateVehicles(config.maxVehiclesPerRun());
        List<VehicleServiceRangeRepository.StationPoint> allStations = repository.findAllStations();
        List<AbnormalResult> results = new ArrayList<>();

        for (VehicleServiceRangeRepository.VehicleKey vehicle : vehicles) {
            try {
                Optional<AbnormalResult> r = detectOne(context, period, vehicle, allStations);
                r.ifPresent(results::add);
            } catch (Exception ex) {
                results.add(errorResult(context, vehicle, ex.getMessage()));
            }
        }
        return results;
    }

    private Optional<AbnormalResult> detectOne(DetectionContext context,
                                               DetectionPeriod period,
                                               VehicleServiceRangeRepository.VehicleKey vehicle,
                                               List<VehicleServiceRangeRepository.StationPoint> allStations) {
        List<VehicleServiceRangeRepository.LineStationRow> boundRows = repository.findBoundStations(vehicle.vehicleId());
        if (boundRows.isEmpty()) return Optional.empty();

        VehicleProfile profile = buildProfile(vehicle, boundRows);
        if (profile.boundStations.isEmpty()) return Optional.empty();

        List<VehicleServiceRangeRepository.GpsPoint> gps = repository.findGpsPoints(vehicle.vehicleId(), period.start(), period.end(), config.coordinateMode());
        List<VehicleServiceRangeRepository.GpsPoint> cleanGps = cleanGps(gps);
        if (cleanGps.size() < config.minValidPoints()) return Optional.empty();

        List<VehicleServiceRangeRepository.StationPoint> unboundStations = allStations.stream()
                .filter(s -> s.stationId() != null && !profile.boundStationIds.contains(s.stationId()))
                .filter(s -> s.longitude() != null && s.latitude() != null)
                .toList();

        List<PointState> pointStates = classifyPoints(cleanGps, profile, unboundStations);
        Aggregation aggregation = aggregate(pointStates, profile);
        List<VehicleServiceRangeRepository.PassEvent> passEvents = repository.findPassEvents(vehicle.vehicleId(), period.start(), period.end());
        List<VehicleServiceRangeRepository.PassEvent> unboundPassEvents = passEvents.stream()
                .filter(e -> e.stationId() != null && !profile.boundStationIds.contains(e.stationId()))
                .toList();

        Set<String> triggered = new LinkedHashSet<>();
        if (aggregation.outsideRatio >= config.outsidePointRatioThreshold()
                && aggregation.maxContinuousOutsideMinutes >= config.minOutsideDurationMinutes()) {
            triggered.add("R1_长期偏离所有绑定线路服务点");
        }
        if (aggregation.maxContinuousOutsideMinutes >= config.minOutsideDurationMinutes()) {
            triggered.add("R2_连续偏离绑定线路服务范围");
        }
        if (aggregation.nearUnboundEvents.size() >= config.nearUnboundStationCountThreshold()) {
            triggered.add("R3_频繁接近非绑定线路站点");
        }
        if (!aggregation.nearOtherTownEvents.isEmpty()) {
            triggered.add("R4_接近非服务乡镇站点辅助提示");
        }
        if (!aggregation.nearOtherCountyEvents.isEmpty()) {
            triggered.add("R5_接近其他区县站点辅助提示");
        }
        if (!unboundPassEvents.isEmpty()) {
            triggered.add("R6_非绑定站点过站记录增强证据");
        }

        if (triggered.isEmpty()) {
            return Optional.empty();
        }

        RiskLevel riskLevel = risk(triggered);
        String abnormalId = "SR-" + context.taskId() + "-" + vehicle.vehicleId();
        AbnormalResult result = new AbnormalResult();
        result.setAbnormalId(abnormalId);
        result.setTaskId(context.taskId());
        result.setObjectType(ObjectType.VEHICLE);
        result.setObjectId(String.valueOf(vehicle.vehicleId()));
        result.setObjectName(vehicle.plateNo() == null ? String.valueOf(vehicle.vehicleId()) : vehicle.plateNo());
        result.setCountyName(context.countyName());
        result.setAbnormalCategory(CATEGORY);
        result.setAbnormalSubtype(SUBTYPE);
        result.setRuleCode(CODE + "_RULE");
        result.setDetectorCode(CODE);
        result.setFlowCode(context.flowCode());
        result.setNodeCode(context.currentNodeCode());
        result.setPeriod(context.period());
        result.setRiskLevel(riskLevel);
        result.setCalcStatus(CalcStatus.CALCULABLE);
        result.setReviewStatus("待复核");
        result.setGeneratedTime(LocalDateTime.now());
        result.setEvidenceSummary(summary(vehicle, profile, aggregation, unboundPassEvents, triggered));
        result.setEvidenceRecords(evidences(abnormalId, vehicle, profile, aggregation, unboundPassEvents, triggered, period));
        return Optional.of(result);
    }

    private VehicleProfile buildProfile(VehicleServiceRangeRepository.VehicleKey vehicle,
                                        List<VehicleServiceRangeRepository.LineStationRow> rows) {
        List<StationCoord> boundStations = rows.stream()
                .filter(r -> r.stationId() != null && r.longitude() != null && r.latitude() != null)
                .map(r -> new StationCoord(r.stationId(), r.stationName(), r.longitude(), r.latitude(), r.stationParentAreaCode(), r.stationParentAreaName(), r.lineCountyCode()))
                .toList();
        Set<String> boundStationIds = boundStations.stream().map(StationCoord::stationId).collect(Collectors.toCollection(LinkedHashSet::new));
        Set<String> lineIds = rows.stream().map(VehicleServiceRangeRepository.LineStationRow::lineId).filter(Objects::nonNull).collect(Collectors.toCollection(LinkedHashSet::new));
        Set<String> lineNames = rows.stream().map(VehicleServiceRangeRepository.LineStationRow::lineName).filter(Objects::nonNull).collect(Collectors.toCollection(LinkedHashSet::new));
        Set<String> serviceTownSet = rows.stream().map(VehicleServiceRangeRepository.LineStationRow::stationParentAreaCode).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<String> serviceCountySet = rows.stream().map(VehicleServiceRangeRepository.LineStationRow::lineCountyCode).filter(Objects::nonNull).collect(Collectors.toSet());
        List<Segment> corridors = buildCorridors(rows);
        return new VehicleProfile(vehicle.vehicleId(), vehicle.plateNo(), lineIds, lineNames, boundStations, boundStationIds, serviceTownSet, serviceCountySet, corridors);
    }

    private List<Segment> buildCorridors(List<VehicleServiceRangeRepository.LineStationRow> rows) {
        Map<String, List<VehicleServiceRangeRepository.LineStationRow>> byLine = rows.stream()
                .filter(r -> r.longitude() != null && r.latitude() != null)
                .collect(Collectors.groupingBy(VehicleServiceRangeRepository.LineStationRow::lineId, LinkedHashMap::new, Collectors.toList()));
        List<Segment> segments = new ArrayList<>();
        for (List<VehicleServiceRangeRepository.LineStationRow> lineRows : byLine.values()) {
            lineRows.sort(Comparator.comparing(r -> r.stationOrder() == null ? 9999 : r.stationOrder()));
            for (int i = 1; i < lineRows.size(); i++) {
                VehicleServiceRangeRepository.LineStationRow a = lineRows.get(i - 1);
                VehicleServiceRangeRepository.LineStationRow b = lineRows.get(i);
                segments.add(new Segment(a.longitude(), a.latitude(), b.longitude(), b.latitude()));
            }
        }
        return segments;
    }

    private List<VehicleServiceRangeRepository.GpsPoint> cleanGps(List<VehicleServiceRangeRepository.GpsPoint> gps) {
        List<VehicleServiceRangeRepository.GpsPoint> list = gps.stream()
                .filter(p -> p.gpsTime() != null && p.lng() != null && p.lat() != null)
                .filter(p -> p.lng() >= 70 && p.lng() <= 140 && p.lat() >= 0 && p.lat() <= 60)
                .sorted(Comparator.comparing(VehicleServiceRangeRepository.GpsPoint::gpsTime))
                .toList();
        List<VehicleServiceRangeRepository.GpsPoint> cleaned = new ArrayList<>();
        VehicleServiceRangeRepository.GpsPoint prev = null;
        for (VehicleServiceRangeRepository.GpsPoint p : list) {
            if (prev == null || !prev.gpsTime().equals(p.gpsTime()) || !prev.lng().equals(p.lng()) || !prev.lat().equals(p.lat())) {
                cleaned.add(p);
            }
            prev = p;
        }
        return cleaned;
    }

    private List<PointState> classifyPoints(List<VehicleServiceRangeRepository.GpsPoint> gps,
                                            VehicleProfile profile,
                                            List<VehicleServiceRangeRepository.StationPoint> unboundStations) {
        List<PointState> states = new ArrayList<>();
        VehicleServiceRangeRepository.GpsPoint prev = null;
        for (VehicleServiceRangeRepository.GpsPoint p : gps) {
            boolean drift = isDrift(prev, p);
            NearestBound nearestBound = nearestBoundStation(p, profile.boundStations);
            double corridorDist = nearestCorridorDistance(p, profile.corridors);
            NearestUnbound nearestUnbound = nearestUnboundStation(p, unboundStations);

            Status status;
            if (drift) status = Status.GPS_SUSPECT;
            else if (nearestBound.distanceMeters <= config.stationRadiusMeters()) status = Status.NEAR_BOUND_STATION;
            else if (corridorDist <= config.corridorRadiusMeters()) status = Status.IN_BOUND_CORRIDOR;
            else if (nearestUnbound.station != null && nearestUnbound.distanceMeters <= config.otherStationRadiusMeters()) status = Status.NEAR_UNBOUND_STATION;
            else status = Status.OUTSIDE_BOUND_SERVICE_RANGE;

            states.add(new PointState(p, status, nearestBound.distanceMeters, corridorDist, nearestUnbound.station, nearestUnbound.distanceMeters));
            prev = p;
        }
        return states;
    }

    private boolean isDrift(VehicleServiceRangeRepository.GpsPoint prev, VehicleServiceRangeRepository.GpsPoint p) {
        if (p.speed() != null && p.speed() > config.gpsDriftSpeedThresholdKmh()) return true;
        if (prev == null || prev.gpsTime() == null || p.gpsTime() == null) return false;
        long seconds = Math.max(1, Duration.between(prev.gpsTime(), p.gpsTime()).toSeconds());
        if (seconds <= 0 || seconds > config.gpsGapThresholdMinutes() * 60L) return false;
        double distM = haversineMeters(prev.lng(), prev.lat(), p.lng(), p.lat());
        double kmh = distM / 1000.0 / (seconds / 3600.0);
        return kmh > config.gpsDriftSpeedThresholdKmh();
    }

    private Aggregation aggregate(List<PointState> states, VehicleProfile profile) {
        long valid = states.stream().filter(s -> s.status != Status.GPS_SUSPECT).count();
        if (valid == 0) return Aggregation.empty();
        long outside = states.stream().filter(this::isOutsideLike).count();
        double ratio = outside * 1.0 / valid;
        long maxDuration = maxContinuousOutsideMinutes(states);
        List<NearStationEvent> nearEvents = mergeNearUnboundEvents(states);
        List<NearStationEvent> otherTown = nearEvents.stream()
                .filter(e -> e.station != null && e.station.parentAreaCode() != null && !profile.serviceTownSet.contains(e.station.parentAreaCode()))
                .toList();
        List<NearStationEvent> otherCounty = nearEvents.stream()
                .filter(e -> e.station != null && e.station.countyCode() != null && !profile.serviceCountySet.contains(e.station.countyCode()))
                .toList();
        return new Aggregation(ratio, maxDuration, nearEvents, otherTown, otherCounty);
    }

    private boolean isOutsideLike(PointState s) {
        return s.status == Status.OUTSIDE_BOUND_SERVICE_RANGE || s.status == Status.NEAR_UNBOUND_STATION;
    }

    private long maxContinuousOutsideMinutes(List<PointState> states) {
        LocalDateTime start = null;
        LocalDateTime last = null;
        long max = 0;
        for (PointState s : states) {
            LocalDateTime t = s.point.gpsTime();
            if (t == null) continue;
            boolean gap = last != null && Duration.between(last, t).toMinutes() > config.gpsGapThresholdMinutes();
            if (isOutsideLike(s) && !gap) {
                if (start == null) start = t;
            } else {
                if (start != null && last != null) max = Math.max(max, Duration.between(start, last).toMinutes());
                start = isOutsideLike(s) ? t : null;
            }
            last = t;
        }
        if (start != null && last != null) max = Math.max(max, Duration.between(start, last).toMinutes());
        return max;
    }

    private List<NearStationEvent> mergeNearUnboundEvents(List<PointState> states) {
        List<NearStationEvent> events = new ArrayList<>();
        NearStationEvent current = null;
        for (PointState s : states) {
            if (s.status != Status.NEAR_UNBOUND_STATION || s.nearestUnboundStation == null || s.point.gpsTime() == null) continue;
            if (current != null
                    && Objects.equals(current.station.stationId(), s.nearestUnboundStation.stationId())
                    && Duration.between(current.endTime, s.point.gpsTime()).toMinutes() <= config.eventMergeWindowMinutes()) {
                current.endTime = s.point.gpsTime();
                current.minDistanceMeters = Math.min(current.minDistanceMeters, s.nearestUnboundDistanceMeters);
                current.pointCount++;
            } else {
                current = new NearStationEvent(s.nearestUnboundStation, s.point.gpsTime(), s.point.gpsTime(), s.nearestUnboundDistanceMeters, 1);
                events.add(current);
            }
        }
        return events;
    }

    private RiskLevel risk(Set<String> triggered) {
        boolean major = triggered.stream().anyMatch(r -> r.startsWith("R1") || r.startsWith("R2"));
        boolean enhanced = triggered.stream().anyMatch(r -> r.startsWith("R4") || r.startsWith("R5") || r.startsWith("R6"));
        if (major && enhanced) return RiskLevel.HIGH;
        if (major) return RiskLevel.MEDIUM;
        return RiskLevel.LOW;
    }

    private String summary(VehicleServiceRangeRepository.VehicleKey vehicle, VehicleProfile profile,
                           Aggregation agg, List<VehicleServiceRangeRepository.PassEvent> unboundPassEvents,
                           Set<String> triggered) {
        return "车辆" + safe(vehicle.plateNo(), String.valueOf(vehicle.vehicleId())) + "在检测周期内轨迹与其绑定线路服务范围存在明显不一致。"
                + "绑定线路：" + String.join("、", profile.lineNames) + "；"
                + "服务范围外轨迹点比例约" + String.format(Locale.ROOT, "%.1f%%", agg.outsideRatio * 100) + "，"
                + "最大连续偏离时长约" + agg.maxContinuousOutsideMinutes + "分钟，"
                + "接近非绑定线路站点" + agg.nearUnboundEvents.size() + "次，"
                + "非绑定站点过站记录" + unboundPassEvents.size() + "条。"
                + "触发规则：" + String.join("、", triggered) + "。"
                + "当前未接入乡镇、区县行政边界数据，本结果为疑似服务范围外运行辅助提示，需结合临时调度、轨迹质量和人工复核确认。";
    }

    private List<EvidenceRecord> evidences(String abnormalId, VehicleServiceRangeRepository.VehicleKey vehicle,
                                           VehicleProfile profile, Aggregation agg,
                                           List<VehicleServiceRangeRepository.PassEvent> unboundPassEvents,
                                           Set<String> triggered, DetectionPeriod period) {
        List<EvidenceRecord> records = new ArrayList<>();
        Map<String, Object> summaryPayload = new LinkedHashMap<>();
        summaryPayload.put("vehicleId", vehicle.vehicleId());
        summaryPayload.put("plateNo", safe(vehicle.plateNo(), ""));
        summaryPayload.put("periodStart", period.start().toString());
        summaryPayload.put("periodEnd", period.end().toString());
        summaryPayload.put("boundLineIds", profile.lineIds);
        summaryPayload.put("boundLineNames", profile.lineNames);
        summaryPayload.put("outsideRatio", agg.outsideRatio);
        summaryPayload.put("maxContinuousOutsideMinutes", agg.maxContinuousOutsideMinutes);
        summaryPayload.put("nearUnboundStationCount", agg.nearUnboundEvents.size());
        summaryPayload.put("nearOtherTownCount", agg.nearOtherTownEvents.size());
        summaryPayload.put("nearOtherCountyCount", agg.nearOtherCountyEvents.size());
        summaryPayload.put("triggeredRules", triggered);
        records.add(new EvidenceRecord("EV-" + abnormalId + "-SUMMARY", abnormalId, "SERVICE_RANGE_STAT", "服务范围外运行统计",
                "车辆轨迹与绑定线路站点集合的空间邻近统计。",
                summaryPayload, LocalDateTime.now()));

        List<Map<String, Object>> nearStations = agg.nearUnboundEvents.stream().limit(10).map(e -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("stationId", e.station.stationId());
            m.put("stationName", e.station.stationName());
            m.put("startTime", e.startTime.toString());
            m.put("endTime", e.endTime.toString());
            m.put("minDistanceMeters", Math.round(e.minDistanceMeters));
            m.put("parentAreaName", e.station.parentAreaName());
            m.put("countyCode", e.station.countyCode());
            return m;
        }).toList();
        records.add(new EvidenceRecord("EV-" + abnormalId + "-NEAR-STATION", abnormalId, "NEAR_UNBOUND_STATION", "接近非绑定线路站点事件",
                "展示车辆接近非车辆绑定线路站点的事件，用作疑似跨乡镇/跨县辅助证据。",
                Map.of("events", nearStations), LocalDateTime.now()));

        if (!unboundPassEvents.isEmpty()) {
            List<Map<String, Object>> pass = unboundPassEvents.stream().limit(10).map(e -> {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("stationId", e.stationId());
                m.put("stationName", e.stationName());
                m.put("eventTime", e.eventTime() == null ? null : e.eventTime().toString());
                m.put("parentAreaName", e.parentAreaName());
                m.put("countyCode", e.countyCode());
                return m;
            }).toList();
            records.add(new EvidenceRecord("EV-" + abnormalId + "-PASS", abnormalId, "UNBOUND_PASS_EVENT", "非绑定站点过站记录",
                    "存在非车辆绑定线路站点过站记录，作为风险增强证据。", Map.of("events", pass), LocalDateTime.now()));
        }
        return records;
    }

    private AbnormalResult notConfiguredResult(DetectionContext context) {
        return AbnormalResult.demo("SR-" + context.taskId() + "-NO-DB", context.taskId(), ObjectType.VEHICLE,
                "数据库未配置", context.countyName(), CATEGORY, SUBTYPE, CODE, context.flowCode(), context.currentNodeCode(),
                context.period(), RiskLevel.NONE, CalcStatus.NOT_CALCULABLE,
                "未配置 khy.abnormal.datasource.url，无法读取 v_khy_algo_vehicle_line_station、vehicle_gps_points 等真实数据。检测器已注册，可在配置数据库后执行真实检测。");
    }

    private AbnormalResult errorResult(DetectionContext context, VehicleServiceRangeRepository.VehicleKey vehicle, String message) {
        return AbnormalResult.demo("SR-" + context.taskId() + "-ERR-" + vehicle.vehicleId(), context.taskId(), ObjectType.VEHICLE,
                safe(vehicle.plateNo(), String.valueOf(vehicle.vehicleId())), context.countyName(), CATEGORY, SUBTYPE, CODE,
                context.flowCode(), context.currentNodeCode(), context.period(), RiskLevel.NONE, CalcStatus.NOT_CALCULABLE,
                "车辆服务范围外运行检测失败：" + message);
    }

    private DetectionPeriod parsePeriod(String period) {
        if (period == null || period.isBlank() || "当前周期".equals(period)) {
            LocalDate first = LocalDate.now().withDayOfMonth(1);
            return new DetectionPeriod(first.atStartOfDay(), first.plusMonths(1).atStartOfDay());
        }
        String p = period.trim();
        try {
            if (p.matches("\\d{4}-\\d{2}")) {
                LocalDate first = LocalDate.parse(p + "-01", DateTimeFormatter.ISO_LOCAL_DATE);
                return new DetectionPeriod(first.atStartOfDay(), first.plusMonths(1).atStartOfDay());
            }
            if (p.matches("\\d{4}-\\d{2}-\\d{2}")) {
                LocalDate day = LocalDate.parse(p, DateTimeFormatter.ISO_LOCAL_DATE);
                return new DetectionPeriod(day.atStartOfDay(), day.plusDays(1).atStartOfDay());
            }
        } catch (Exception ignored) {}
        LocalDate first = LocalDate.now().withDayOfMonth(1);
        return new DetectionPeriod(first.atStartOfDay(), first.plusMonths(1).atStartOfDay());
    }

    private NearestBound nearestBoundStation(VehicleServiceRangeRepository.GpsPoint p, List<StationCoord> stations) {
        double min = Double.MAX_VALUE;
        StationCoord nearest = null;
        for (StationCoord s : stations) {
            double d = haversineMeters(p.lng(), p.lat(), s.lng(), s.lat());
            if (d < min) { min = d; nearest = s; }
        }
        return new NearestBound(nearest, min);
    }

    private NearestUnbound nearestUnboundStation(VehicleServiceRangeRepository.GpsPoint p, List<VehicleServiceRangeRepository.StationPoint> stations) {
        double min = Double.MAX_VALUE;
        VehicleServiceRangeRepository.StationPoint nearest = null;
        for (VehicleServiceRangeRepository.StationPoint s : stations) {
            double d = haversineMeters(p.lng(), p.lat(), s.longitude(), s.latitude());
            if (d < min) { min = d; nearest = s; }
        }
        return new NearestUnbound(nearest, min);
    }

    private double nearestCorridorDistance(VehicleServiceRangeRepository.GpsPoint p, List<Segment> segments) {
        if (segments == null || segments.isEmpty()) return Double.MAX_VALUE;
        double min = Double.MAX_VALUE;
        for (Segment s : segments) {
            double d = distanceToSegmentMeters(p.lng(), p.lat(), s.lng1, s.lat1, s.lng2, s.lat2);
            if (d < min) min = d;
        }
        return min;
    }

    private static double haversineMeters(double lng1, double lat1, double lng2, double lat2) {
        double r = 6371000.0;
        double phi1 = Math.toRadians(lat1);
        double phi2 = Math.toRadians(lat2);
        double dPhi = Math.toRadians(lat2 - lat1);
        double dLambda = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dPhi / 2) * Math.sin(dPhi / 2)
                + Math.cos(phi1) * Math.cos(phi2) * Math.sin(dLambda / 2) * Math.sin(dLambda / 2);
        return 2 * r * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    private static double distanceToSegmentMeters(double lng, double lat, double lng1, double lat1, double lng2, double lat2) {
        // 局部平面近似：对站点间短距离足够用于辅助判定。
        double meanLat = Math.toRadians((lat1 + lat2 + lat) / 3.0);
        double scaleX = 111320.0 * Math.cos(meanLat);
        double scaleY = 110540.0;
        double px = lng * scaleX, py = lat * scaleY;
        double ax = lng1 * scaleX, ay = lat1 * scaleY;
        double bx = lng2 * scaleX, by = lat2 * scaleY;
        double dx = bx - ax, dy = by - ay;
        if (dx == 0 && dy == 0) return Math.hypot(px - ax, py - ay);
        double t = ((px - ax) * dx + (py - ay) * dy) / (dx * dx + dy * dy);
        t = Math.max(0, Math.min(1, t));
        double cx = ax + t * dx, cy = ay + t * dy;
        return Math.hypot(px - cx, py - cy);
    }

    private static String safe(String v, String fallback) { return v == null || v.isBlank() ? fallback : v; }

    private record DetectionPeriod(LocalDateTime start, LocalDateTime end) {}
    private record StationCoord(String stationId, String stationName, double lng, double lat, String parentAreaCode, String parentAreaName, String countyCode) {}
    private record Segment(double lng1, double lat1, double lng2, double lat2) {}
    private record VehicleProfile(long vehicleId, String plateNo, Set<String> lineIds, Set<String> lineNames,
                                  List<StationCoord> boundStations, Set<String> boundStationIds,
                                  Set<String> serviceTownSet, Set<String> serviceCountySet, List<Segment> corridors) {}
    private record NearestBound(StationCoord station, double distanceMeters) {}
    private record NearestUnbound(VehicleServiceRangeRepository.StationPoint station, double distanceMeters) {}
    private enum Status { NEAR_BOUND_STATION, IN_BOUND_CORRIDOR, NEAR_UNBOUND_STATION, OUTSIDE_BOUND_SERVICE_RANGE, GPS_SUSPECT }
    private record PointState(VehicleServiceRangeRepository.GpsPoint point, Status status, double nearestBoundDistanceMeters,
                              double corridorDistanceMeters, VehicleServiceRangeRepository.StationPoint nearestUnboundStation,
                              double nearestUnboundDistanceMeters) {}
    private static class NearStationEvent {
        VehicleServiceRangeRepository.StationPoint station;
        LocalDateTime startTime;
        LocalDateTime endTime;
        double minDistanceMeters;
        int pointCount;
        NearStationEvent(VehicleServiceRangeRepository.StationPoint station, LocalDateTime startTime, LocalDateTime endTime, double minDistanceMeters, int pointCount) {
            this.station = station; this.startTime = startTime; this.endTime = endTime; this.minDistanceMeters = minDistanceMeters; this.pointCount = pointCount;
        }
    }
    private record Aggregation(double outsideRatio, long maxContinuousOutsideMinutes, List<NearStationEvent> nearUnboundEvents,
                               List<NearStationEvent> nearOtherTownEvents, List<NearStationEvent> nearOtherCountyEvents) {
        static Aggregation empty() { return new Aggregation(0, 0, List.of(), List.of(), List.of()); }
    }
}
