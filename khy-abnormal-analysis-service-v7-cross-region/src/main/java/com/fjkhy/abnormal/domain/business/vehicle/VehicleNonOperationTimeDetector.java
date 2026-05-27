package com.fjkhy.abnormal.domain.business.vehicle;

import com.fjkhy.abnormal.domain.common.ObjectType;
import com.fjkhy.abnormal.domain.detection.AbstractDemoDetector;
import org.springframework.stereotype.Component;

@Component
public class VehicleNonOperationTimeDetector extends AbstractDemoDetector {
    @Override public String detectorCode() { return "VEHICLE_NON_OPERATION_TIME"; }
    @Override public String detectorName() { return "非运营时段运行检测"; }
    @Override public String objectTypeName() { return "车辆"; }
    @Override public String abnormalCategory() { return "车辆资质与运行合规异常"; }
    @Override public String abnormalSubtype() { return "非运营时段运行"; }
    @Override protected ObjectType objectType() { return ObjectType.VEHICLE; }
}
