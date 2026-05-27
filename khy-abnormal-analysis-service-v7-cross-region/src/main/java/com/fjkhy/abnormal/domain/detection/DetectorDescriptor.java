package com.fjkhy.abnormal.domain.detection;

public record DetectorDescriptor(
        String detectorCode,
        String detectorName,
        String objectTypeName,
        String abnormalCategory,
        String abnormalSubtype,
        String status
) {}
