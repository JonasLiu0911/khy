package com.example.ky.analysis.infrastructure.gis;

import com.example.ky.analysis.domain.common.port.SpatialCalculationPort;
import org.springframework.stereotype.Component;

@Component
public class HaversineSpatialCalculationAdapter implements SpatialCalculationPort {
    private static final double EARTH_RADIUS_M = 6371000.0;

    @Override
    public double distanceMeters(double lon1, double lat1, double lon2, double lat2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return EARTH_RADIUS_M * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    @Override
    public String gridCode(double longitude, double latitude, int gridSizeMeters) {
        double metersPerDegreeLat = 111_000.0;
        double metersPerDegreeLon = 111_000.0 * Math.cos(Math.toRadians(latitude));
        long gx = Math.round(longitude * metersPerDegreeLon / gridSizeMeters);
        long gy = Math.round(latitude * metersPerDegreeLat / gridSizeMeters);
        return gx + "_" + gy;
    }
}
