package com.example.ky.analysis.domain.visualization.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 检测窗口展示对象。
 */
public record AnalysisWindowView(
        LocalDate statDate,
        LocalDateTime windowStart,
        LocalDateTime windowEnd,
        int windowDays
) {
}
