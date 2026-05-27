package com.example.ky.analysis.domain.line.model;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public record LineConstraint(
        String lineId,
        String lineName,
        List<LineStation> stations
) {
    public Set<String> stationIds() {
        return stations.stream().map(LineStation::stationId).collect(Collectors.toSet());
    }
}
