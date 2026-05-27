package com.example.ky.analysis.domain.station.model;

import java.time.LocalDateTime;

public record StationEvent(
        String vehicleId,
        String stationId,
        LocalDateTime eventTime,
        String sourceSystem
) {}
