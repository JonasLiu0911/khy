package com.fjkhy.abnormal.domain.business.vehicle;

import com.fjkhy.abnormal.domain.common.ObjectType;
import com.fjkhy.abnormal.domain.detection.AbstractDemoDetector;
import org.springframework.stereotype.Component;

@Component
public class VehicleQualificationConsistencyDetector extends AbstractDemoDetector {
    @Override public String detectorCode() { return "VEHICLE_QUALIFICATION_CONSISTENCY"; }
    @Override public String detectorName() { return "车辆基础资质字段缺失或不一致检测"; }
    @Override public String objectTypeName() { return "车辆"; }
    @Override public String abnormalCategory() { return "车辆资质与运行合规异常"; }
    @Override public String abnormalSubtype() { return "车辆基础资质字段缺失或不一致"; }
    @Override protected ObjectType objectType() { return ObjectType.VEHICLE; }
}
