package com.fjkhy.abnormal.domain.business.fulfillment;

import com.fjkhy.abnormal.domain.common.ObjectType;
import com.fjkhy.abnormal.domain.detection.AbstractDemoDetector;
import org.springframework.stereotype.Component;

@Component
public class FulfillmentFrequencyInsufficientAnalyzer extends AbstractDemoDetector {
    @Override public String detectorCode() { return "FULFILLMENT_FREQUENCY_INSUFFICIENT"; }
    @Override public String detectorName() { return "跨对象周频次不足辅助分析"; }
    @Override public String objectTypeName() { return "跨对象履约"; }
    @Override public String abnormalCategory() { return "跨对象服务履约辅助分析"; }
    @Override public String abnormalSubtype() { return "周频次不足辅助分析"; }
    @Override protected ObjectType objectType() { return ObjectType.FULFILLMENT; }
}
