package com.fjkhy.abnormal.domain.business.vehicle;

import com.fjkhy.abnormal.domain.common.ObjectType;
import com.fjkhy.abnormal.domain.detection.AbstractDemoDetector;
import org.springframework.stereotype.Component;

@Component
public class VehicleSignTimeMismatchDetector extends AbstractDemoDetector {
    @Override public String detectorCode() { return "VEHICLE_SIGN_TIME_MISMATCH"; }
    @Override public String detectorName() { return "站点到达时刻与标识牌时刻不匹配检测"; }
    @Override public String objectTypeName() { return "车辆"; }
    @Override public String abnormalCategory() { return "车辆运行证据一致性异常"; }
    @Override public String abnormalSubtype() { return "站点到达时刻与标识牌时刻不匹配"; }
    @Override protected ObjectType objectType() { return ObjectType.VEHICLE; }
}
