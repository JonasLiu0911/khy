package com.example.ky.analysis.domain.visualization.model;

import java.util.List;
import java.util.Map;

/**
 * 单车轨迹异常检测结果地图可视化响应对象。
 */
public record TrackVisualizationResult(
        VehicleView vehicle,
        AnalysisWindowView window,
        AbnormalResultView abnormalResult,
        List<StationView> boundStations,
        List<DailyTrackView> dailyTracks,
        Map<String, Object> evidence
) {
}
