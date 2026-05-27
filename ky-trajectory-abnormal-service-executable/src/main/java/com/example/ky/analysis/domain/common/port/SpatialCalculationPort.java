package com.example.ky.analysis.domain.common.port;

import com.example.ky.analysis.domain.vehicle.model.GpsPoint;

public interface SpatialCalculationPort {
    double distanceMeters(double lon1, double lat1, double lon2, double lat2);

    default double distanceMeters(GpsPoint p1, GpsPoint p2) {
        return distanceMeters(p1.glongitude(), p1.glatitude(), p2.glongitude(), p2.glatitude());
    }

    String gridCode(double longitude, double latitude, int gridSizeMeters);
}
