package com.example.ky.analysis.domain.visualization.model;

/**
 * 绑定线路站点展示对象。
 */
public record StationView(
        String lineId,
        String lineName,
        String stationId,
        String stationName,
        double longitude,
        double latitude,
        Integer stationOrder,
        boolean keyStation
) {
}
