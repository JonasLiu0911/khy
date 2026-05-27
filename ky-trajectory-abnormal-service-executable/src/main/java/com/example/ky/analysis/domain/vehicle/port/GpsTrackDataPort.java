package com.example.ky.analysis.domain.vehicle.port;

import com.example.ky.analysis.domain.vehicle.model.GpsPoint;

import java.time.LocalDateTime;
import java.util.List;

public interface GpsTrackDataPort {
    List<GpsPoint> queryGpsPoints(String vehicleId, LocalDateTime startTime, LocalDateTime endTime);
}
