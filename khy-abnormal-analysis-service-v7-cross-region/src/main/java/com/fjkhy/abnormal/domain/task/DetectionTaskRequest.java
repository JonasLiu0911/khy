package com.fjkhy.abnormal.domain.task;

import com.fjkhy.abnormal.domain.common.ObjectType;

public record DetectionTaskRequest(
        String flowCode,
        String countyName,
        String period,
        ObjectType objectType,
        String operator
) {}
