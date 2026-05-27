package com.fjkhy.abnormal.domain.detection;

import com.fjkhy.abnormal.domain.result.AbnormalResult;

import java.util.List;

public interface AbnormalDetector {
    String detectorCode();
    String detectorName();
    String objectTypeName();
    String abnormalCategory();
    String abnormalSubtype();
    boolean supports(DetectionContext context);
    List<AbnormalResult> detect(DetectionContext context);
}
