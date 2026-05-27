package com.example.ky.analysis.domain.line.model;

public record LineStation(
        String lineId,
        String lineName,
        String stationId,
        String stationName,
        double longitude,
        double latitude,
        Integer stationOrder,
        boolean keyStation
) {}
