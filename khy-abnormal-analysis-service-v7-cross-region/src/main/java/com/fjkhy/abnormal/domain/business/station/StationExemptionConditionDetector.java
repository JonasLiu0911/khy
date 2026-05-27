package com.fjkhy.abnormal.domain.business.station;

import com.fjkhy.abnormal.domain.common.ObjectType;
import com.fjkhy.abnormal.domain.detection.AbstractDemoDetector;
import org.springframework.stereotype.Component;

@Component
public class StationExemptionConditionDetector extends AbstractDemoDetector {
    @Override public String detectorCode() { return "STATION_EXEMPTION_CONDITION"; }
    @Override public String detectorName() { return "免设点设置条件异常检测"; }
    @Override public String objectTypeName() { return "站点"; }
    @Override public String abnormalCategory() { return "站点设置与服务支撑合规异常"; }
    @Override public String abnormalSubtype() { return "免设点设置条件异常"; }
    @Override protected ObjectType objectType() { return ObjectType.STATION; }
}
