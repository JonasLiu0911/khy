package com.example.ky.analysis.domain.visualization.model;

import java.time.LocalDateTime;

/**
 * 前端地图展示用轨迹点。
 * 默认返回原始坐标；若前端使用高德地图，需要在前端转换为 GCJ-02 或由真实适配器统一转换。
 */
public record TrackPointView(
        LocalDateTime gpsTime,
        double longitude,
        double latitude,
        double glongitude,
        double glatitude,
        double speed
) {
}
