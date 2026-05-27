package com.fjkhy.abnormal.domain.business.vehicle;

import com.fjkhy.abnormal.domain.common.ObjectType;
import com.fjkhy.abnormal.domain.detection.AbstractDemoDetector;
import org.springframework.stereotype.Component;

@Component
public class VehicleLongTermNoOperationDetector extends AbstractDemoDetector {
    @Override public String detectorCode() { return "VEHICLE_LONG_TERM_NO_OPERATION"; }
    @Override public String detectorName() { return "长期无运行证据或无运营记录检测"; }
    @Override public String objectTypeName() { return "车辆"; }
    @Override public String abnormalCategory() { return "车辆资质与运行合规异常"; }
    @Override public String abnormalSubtype() { return "长期无运行证据或无运营记录"; }
    @Override protected ObjectType objectType() { return ObjectType.VEHICLE; }
}
