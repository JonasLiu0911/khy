package com.example.ky.analysis.domain.vehicle.algorithm;

import com.example.ky.analysis.domain.vehicle.model.GpsPoint;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
public class GpsCleanAlgorithm {
    public List<GpsPoint> clean(List<GpsPoint> points) {
        if (points == null) {
            return List.of();
        }
        return points.stream()
                .filter(p -> p.glongitude() >= -180 && p.glongitude() <= 180)
                .filter(p -> p.glatitude() >= -90 && p.glatitude() <= 90)
                .filter(p -> p.speed() >= 0 && p.speed() <= 150)
                .sorted(Comparator.comparing(GpsPoint::gpsTime))
                .distinct()
                .toList();
    }
}
