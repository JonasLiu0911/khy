package com.example.ky.analysis.domain.vehicle.algorithm;

import com.example.ky.analysis.domain.common.port.SpatialCalculationPort;
import com.example.ky.analysis.domain.config.TrajectoryAnalysisConfig;
import com.example.ky.analysis.domain.vehicle.model.GpsPoint;
import com.example.ky.analysis.domain.vehicle.model.TripSegment;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class TripSplitAlgorithm {
    private final SpatialCalculationPort spatial;

    public TripSplitAlgorithm(SpatialCalculationPort spatial) {
        this.spatial = spatial;
    }

    public List<TripSegment> split(List<GpsPoint> points, TrajectoryAnalysisConfig config) {
        List<TripSegment> result = new ArrayList<>();
        if (points == null || points.isEmpty()) {
            return result;
        }
        List<GpsPoint> current = new ArrayList<>();
        current.add(points.get(0));
        for (int i = 1; i < points.size(); i++) {
            GpsPoint prev = points.get(i - 1);
            GpsPoint now = points.get(i);
            long gap = Duration.between(prev.gpsTime(), now.gpsTime()).toMinutes();
            boolean crossDay = !prev.gpsTime().toLocalDate().equals(now.gpsTime().toLocalDate());
            if (gap > config.getMaxGapMinutes() || crossDay) {
                addIfValid(result, current, config);
                current = new ArrayList<>();
            }
            current.add(now);
        }
        addIfValid(result, current, config);
        return result;
    }

    private void addIfValid(List<TripSegment> result, List<GpsPoint> points, TrajectoryAnalysisConfig config) {
        if (points.size() < config.getMinTripPointCount()) {
            return;
        }
        double distanceKm = calcDistanceKm(points);
        long minutes = Duration.between(points.get(0).gpsTime(), points.get(points.size() - 1).gpsTime()).toMinutes();
        if (distanceKm < config.getMinTripDistanceKm()) {
            return;
        }
        String tripId = points.get(0).vehicleId() + "-" + UUID.randomUUID();
        result.add(new TripSegment(tripId, points.get(0).vehicleId(), points.get(0).gpsTime(),
                points.get(points.size() - 1).gpsTime(), List.copyOf(points), distanceKm, minutes));
    }

    private double calcDistanceKm(List<GpsPoint> points) {
        double meters = 0;
        for (int i = 1; i < points.size(); i++) {
            meters += spatial.distanceMeters(points.get(i - 1), points.get(i));
        }
        return meters / 1000.0;
    }
}
