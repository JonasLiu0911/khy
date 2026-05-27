package com.fjkhy.abnormal.domain.business.vehicle;

import com.fjkhy.abnormal.domain.common.ObjectType;
import com.fjkhy.abnormal.domain.detection.AbstractDemoDetector;
import org.springframework.stereotype.Component;

@Component
public class VehicleCrossRegionDetector extends AbstractDemoDetector {
    @Override public String detectorCode() { return "VEHICLE_CROSS_REGION"; }
    @Override public String detectorName() { return "跨区域运营检测"; }
    @Override public String objectTypeName() { return "车辆"; }
    @Override public String abnormalCategory() { return "车辆资质与运行合规异常"; }
    @Override public String abnormalSubtype() { return "跨区域运营"; }
    @Override protected ObjectType objectType() { return ObjectType.VEHICLE; }
}
