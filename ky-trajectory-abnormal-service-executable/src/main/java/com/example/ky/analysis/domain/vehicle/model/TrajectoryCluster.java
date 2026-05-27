package com.example.ky.analysis.domain.vehicle.model;

import java.util.List;

public record TrajectoryCluster(
        String clusterId,
        List<String> tripIds,
        boolean noise
) {}
