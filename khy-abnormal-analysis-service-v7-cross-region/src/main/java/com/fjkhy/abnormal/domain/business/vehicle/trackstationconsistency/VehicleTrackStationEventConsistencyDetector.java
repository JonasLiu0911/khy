package com.fjkhy.abnormal.domain.business.vehicle.trackstationconsistency;

import com.fjkhy.abnormal.domain.common.ObjectType;
import com.fjkhy.abnormal.domain.detection.AbstractDemoDetector;
import org.springframework.stereotype.Component;

@Component
public class VehicleTrackStationEventConsistencyDetector extends AbstractDemoDetector {
    @Override public String detectorCode() { return "VEHICLE_TRACK_STATION_EVENT_CONSISTENCY"; }
    @Override public String detectorName() { return "轨迹与过站事件一致性异常检测"; }
    @Override public String objectTypeName() { return "车辆"; }
    @Override public String abnormalCategory() { return "车辆运行证据一致性异常"; }
    @Override public String abnormalSubtype() { return "轨迹与过站事件一致性异常"; }
    @Override protected ObjectType objectType() { return ObjectType.VEHICLE; }
}
