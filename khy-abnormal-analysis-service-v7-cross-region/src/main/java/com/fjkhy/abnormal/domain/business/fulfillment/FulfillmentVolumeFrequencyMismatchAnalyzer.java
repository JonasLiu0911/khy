package com.fjkhy.abnormal.domain.business.fulfillment;

import com.fjkhy.abnormal.domain.common.ObjectType;
import com.fjkhy.abnormal.domain.detection.AbstractDemoDetector;
import org.springframework.stereotype.Component;

@Component
public class FulfillmentVolumeFrequencyMismatchAnalyzer extends AbstractDemoDetector {
    @Override public String detectorCode() { return "FULFILLMENT_VOLUME_FREQUENCY_MISMATCH"; }
    @Override public String detectorName() { return "件量频次不匹配辅助分析"; }
    @Override public String objectTypeName() { return "跨对象履约"; }
    @Override public String abnormalCategory() { return "跨对象服务履约辅助分析"; }
    @Override public String abnormalSubtype() { return "件量频次不匹配"; }
    @Override protected ObjectType objectType() { return ObjectType.FULFILLMENT; }
}
