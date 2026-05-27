package com.fjkhy.abnormal.domain.business.vehicle;

import com.fjkhy.abnormal.domain.common.ObjectType;
import com.fjkhy.abnormal.domain.detection.AbstractDemoDetector;
import org.springframework.stereotype.Component;

@Component
public class VehicleArrivalEvidenceDetector extends AbstractDemoDetector {
    @Override public String detectorCode() { return "VEHICLE_ARRIVAL_EVIDENCE"; }
    @Override public String detectorName() { return "车辆到站证据异常检测"; }
    @Override public String objectTypeName() { return "车辆"; }
    @Override public String abnormalCategory() { return "车辆运行证据一致性异常"; }
    @Override public String abnormalSubtype() { return "车辆到站证据异常"; }
    @Override protected ObjectType objectType() { return ObjectType.VEHICLE; }
}
