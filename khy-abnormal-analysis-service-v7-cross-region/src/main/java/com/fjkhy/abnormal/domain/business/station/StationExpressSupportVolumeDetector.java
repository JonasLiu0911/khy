package com.fjkhy.abnormal.domain.business.station;

import com.fjkhy.abnormal.domain.common.ObjectType;
import com.fjkhy.abnormal.domain.detection.AbstractDemoDetector;
import org.springframework.stereotype.Component;

@Component
public class StationExpressSupportVolumeDetector extends AbstractDemoDetector {
    @Override public String detectorCode() { return "STATION_EXPRESS_SUPPORT_VOLUME"; }
    @Override public String detectorName() { return "社会快递网点日均件量支撑提示检测"; }
    @Override public String objectTypeName() { return "站点"; }
    @Override public String abnormalCategory() { return "站点设置与服务支撑合规异常"; }
    @Override public String abnormalSubtype() { return "社会快递网点日均件量支撑性提示"; }
    @Override protected ObjectType objectType() { return ObjectType.STATION; }
}
