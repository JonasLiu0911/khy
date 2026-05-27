package com.example.ky.analysis.domain.vehicle.model;

import java.util.List;
import java.util.Set;

public record TrajectoryFeature(
        String tripId,
        String vehicleId,
        String odPair,
        List<String> gridSequence,
        Set<String> gridSet,
        Set<String> hitStationSet
) {}
