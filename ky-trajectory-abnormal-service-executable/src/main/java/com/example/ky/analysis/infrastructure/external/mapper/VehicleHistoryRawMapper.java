package com.example.ky.analysis.infrastructure.external.mapper;

import com.example.ky.analysis.domain.common.port.SpatialCalculationPort;
import com.example.ky.analysis.domain.line.model.LineStation;
import com.example.ky.analysis.domain.station.model.StationEvent;
import com.example.ky.analysis.domain.vehicle.model.GpsPoint;
import com.example.ky.analysis.infrastructure.external.dto.VehicleHistoryRawPoint;
import com.example.ky.analysis.infrastructure.external.dto.VehicleHistoryRawResponse;
import com.example.ky.analysis.infrastructure.external.dto.VehicleHistoryRawStopData;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class VehicleHistoryRawMapper {
    private static final DateTimeFormatter STOP_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter SRV_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final ObjectMapper objectMapper;
    private final SpatialCalculationPort spatial;

    public VehicleHistoryRawMapper(ObjectMapper objectMapper, SpatialCalculationPort spatial) {
        this.objectMapper = objectMapper;
        this.spatial = spatial;
    }

    public List<GpsPoint> toGpsPoints(String vehicleId, VehicleHistoryRawResponse response) {
        if (response == null || response.getPoints() == null) {
            return List.of();
        }
        List<GpsPoint> points = new ArrayList<>();
        for (VehicleHistoryRawPoint p : response.getPoints()) {
            LocalDateTime time = parseSrvTime(p.getExts());
            if (time == null) {
                continue;
            }
            Double lon = parseDouble(p.getLng());
            Double lat = parseDouble(p.getLat());
            Double glon = parseDouble(p.getGlng());
            Double glat = parseDouble(p.getGlat());
            Double speed = parseDouble(p.getSpeed());
            if (lon == null || lat == null || speed == null) {
                continue;
            }
            points.add(new GpsPoint(vehicleId, time, lon, lat, glon, glat, speed));
        }
        return points;
    }

    public List<StationEvent> toStationEvents(String vehicleId,
                                              VehicleHistoryRawResponse response,
                                              List<LineStation> stations,
                                              int stationRadiusMeters,
                                              String sourceSystem) {
        if (response == null || response.getStopData() == null) {
            return List.of();
        }
        List<StationEvent> events = new ArrayList<>();
        for (VehicleHistoryRawStopData stop : response.getStopData()) {
            LocalDateTime time = parseStopTime(stop.getStartTime());
            if (time == null) {
                continue;
            }
            Double lon = firstDouble(stop.getGlng(), stop.getLng(), stop.getBlng());
            Double lat = firstDouble(stop.getGlat(), stop.getLat(), stop.getBlat());
            if (lon == null || lat == null) {
                continue;
            }
            String stationId = findNearestStationId(lon, lat, stations, stationRadiusMeters).orElse(null);
            if (stationId == null) {
                continue;
            }
            events.add(new StationEvent(vehicleId, stationId, time, sourceSystem));
        }
        return events;
    }

    private Optional<String> findNearestStationId(double lon,
                                                  double lat,
                                                  List<LineStation> stations,
                                                  int radiusMeters) {
        if (stations == null || stations.isEmpty()) {
            return Optional.empty();
        }
        double best = Double.MAX_VALUE;
        String hit = null;
        for (LineStation station : stations) {
            double dist = spatial.distanceMeters(lon, lat, station.longitude(), station.latitude());
            if (dist <= radiusMeters && dist < best) {
                best = dist;
                hit = station.stationId();
            }
        }
        return Optional.ofNullable(hit);
    }

    private LocalDateTime parseStopTime(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return LocalDateTime.parse(value.trim(), STOP_TIME_FORMAT);
    }

    private LocalDateTime parseSrvTime(String exts) {
        if (exts == null || exts.isBlank()) {
            return null;
        }
        try {
            JsonNode node = objectMapper.readTree(exts);
            String srvTime = node.path("srvTime").asText(null);
            if (srvTime == null || srvTime.isBlank()) {
                return null;
            }
            return LocalDateTime.parse(srvTime, SRV_TIME_FORMAT);
        } catch (Exception ex) {
            return null;
        }
    }

    private Double firstDouble(String... values) {
        for (String v : values) {
            Double d = parseDouble(v);
            if (d != null) {
                return d;
            }
        }
        return null;
    }

    private Double parseDouble(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
