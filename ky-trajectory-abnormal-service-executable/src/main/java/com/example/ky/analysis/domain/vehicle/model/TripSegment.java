package com.example.ky.analysis.domain.vehicle.model;

import java.time.LocalDateTime;
import java.util.List;

public record TripSegment(
        String tripId,
        String vehicleId,
        LocalDateTime startTime,
        LocalDateTime endTime,
        List<GpsPoint> points,
        double distanceKm,
        long durationMinutes
) {}
