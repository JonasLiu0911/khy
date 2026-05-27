package com.fjkhy.abnormal.domain.flow;

public record DetectionRuleView(
        String ruleCode,
        String ruleName,
        String objectType,
        String abnormalCategory,
        String abnormalSubtype,
        String detectorCode,
        boolean enabled,
        String version,
        String dataSource
) {}
