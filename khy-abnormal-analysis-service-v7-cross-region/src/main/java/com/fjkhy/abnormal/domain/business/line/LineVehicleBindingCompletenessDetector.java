package com.fjkhy.abnormal.domain.business.line;

import com.fjkhy.abnormal.domain.common.ObjectType;
import com.fjkhy.abnormal.domain.detection.AbstractDemoDetector;
import org.springframework.stereotype.Component;

@Component
public class LineVehicleBindingCompletenessDetector extends AbstractDemoDetector {
    @Override public String detectorCode() { return "LINE_VEHICLE_BINDING_COMPLETENESS"; }
    @Override public String detectorName() { return "线路车辆配置完整性异常检测"; }
    @Override public String objectTypeName() { return "线路"; }
    @Override public String abnormalCategory() { return "线路配置与执行合规异常"; }
    @Override public String abnormalSubtype() { return "线路车辆配置完整性异常"; }
    @Override protected ObjectType objectType() { return ObjectType.LINE; }
}
