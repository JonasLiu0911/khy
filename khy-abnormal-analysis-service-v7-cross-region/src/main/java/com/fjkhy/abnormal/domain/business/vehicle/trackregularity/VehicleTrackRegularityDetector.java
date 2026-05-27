package com.fjkhy.abnormal.domain.business.vehicle.trackregularity;

import com.fjkhy.abnormal.domain.common.ObjectType;
import com.fjkhy.abnormal.domain.detection.AbstractDemoDetector;
import org.springframework.stereotype.Component;

@Component
public class VehicleTrackRegularityDetector extends AbstractDemoDetector {
    @Override public String detectorCode() { return "VEHICLE_TRACK_REGULARITY"; }
    @Override public String detectorName() { return "车辆月度轨迹规律性异常检测"; }
    @Override public String objectTypeName() { return "车辆"; }
    @Override public String abnormalCategory() { return "车辆资质与运行合规异常"; }
    @Override public String abnormalSubtype() { return "运行轨迹无规律"; }
    @Override protected ObjectType objectType() { return ObjectType.VEHICLE; }
}
