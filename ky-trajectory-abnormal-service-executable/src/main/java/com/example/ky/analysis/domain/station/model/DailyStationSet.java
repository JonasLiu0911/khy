package com.example.ky.analysis.domain.station.model;

import java.time.LocalDate;
import java.util.Set;

public record DailyStationSet(
        String vehicleId,
        LocalDate eventDate,
        Set<String> stationIds
) {}
