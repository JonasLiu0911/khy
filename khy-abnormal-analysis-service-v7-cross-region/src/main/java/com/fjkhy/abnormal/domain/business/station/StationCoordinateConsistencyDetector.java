package com.fjkhy.abnormal.domain.business.station;

import com.fjkhy.abnormal.domain.common.ObjectType;
import com.fjkhy.abnormal.domain.detection.AbstractDemoDetector;
import org.springframework.stereotype.Component;

@Component
public class StationCoordinateConsistencyDetector extends AbstractDemoDetector {
    @Override public String detectorCode() { return "STATION_COORDINATE_CONSISTENCY"; }
    @Override public String detectorName() { return "坐标一致性异常检测"; }
    @Override public String objectTypeName() { return "站点"; }
    @Override public String abnormalCategory() { return "站点设置与服务支撑合规异常"; }
    @Override public String abnormalSubtype() { return "坐标一致性异常"; }
    @Override protected ObjectType objectType() { return ObjectType.STATION; }
}
