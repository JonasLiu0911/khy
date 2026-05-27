package com.example.ky.analysis.domain.visualization.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 单日轨迹展示对象。
 */
public record DailyTrackView(
        LocalDate date,
        int pointCount,
        double distanceKm,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String dayLevel,
        List<TrackPointView> points
) {
}
