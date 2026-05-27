package com.example.ky.analysis.domain.vehicle.model;

import java.time.LocalDateTime;

public record GpsPoint(
        String vehicleId,
        LocalDateTime gpsTime,
        double longitude,
        double latitude,
        double glongitude,
        double glatitude,
        double speed
) {}
