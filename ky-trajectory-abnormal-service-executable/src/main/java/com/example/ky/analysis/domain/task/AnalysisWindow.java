package com.example.ky.analysis.domain.task;

import java.time.LocalDateTime;

public record AnalysisWindow(
        LocalDateTime startTime,
        LocalDateTime endTime,
        int windowDays
) {}
