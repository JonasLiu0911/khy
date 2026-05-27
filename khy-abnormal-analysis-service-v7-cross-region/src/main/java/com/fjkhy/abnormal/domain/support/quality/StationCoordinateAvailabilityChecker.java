package com.fjkhy.abnormal.domain.support.quality;

import com.fjkhy.abnormal.domain.common.ObjectType;
import com.fjkhy.abnormal.domain.detection.AbstractDemoDetector;
import org.springframework.stereotype.Component;

@Component
public class StationCoordinateAvailabilityChecker extends AbstractDemoDetector {
    @Override public String detectorCode() { return "STATION_COORDINATE_AVAILABILITY_CHECKER"; }
    @Override public String detectorName() { return "站点坐标可用性检查"; }
    @Override public String objectTypeName() { return "支撑能力"; }
    @Override public String abnormalCategory() { return "数据质量与证据可用性检查"; }
    @Override public String abnormalSubtype() { return "站点坐标可用性"; }
    @Override protected ObjectType objectType() { return ObjectType.SUPPORT; }
}
