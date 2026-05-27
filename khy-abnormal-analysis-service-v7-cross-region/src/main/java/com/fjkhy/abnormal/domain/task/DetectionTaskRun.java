package com.fjkhy.abnormal.domain.task;

import java.time.LocalDateTime;

public record DetectionTaskRun(
        String taskId,
        String flowCode,
        String countyName,
        String period,
        String status,
        int abnormalCount,
        LocalDateTime startTime,
        LocalDateTime endTime
) {}
