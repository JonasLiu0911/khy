package com.fjkhy.abnormal.domain.support.quality;

import com.fjkhy.abnormal.domain.common.ObjectType;
import com.fjkhy.abnormal.domain.detection.AbstractDemoDetector;
import org.springframework.stereotype.Component;

@Component
public class TrackDataQualityChecker extends AbstractDemoDetector {
    @Override public String detectorCode() { return "TRACK_DATA_QUALITY_CHECKER"; }
    @Override public String detectorName() { return "轨迹数据质量检查"; }
    @Override public String objectTypeName() { return "支撑能力"; }
    @Override public String abnormalCategory() { return "数据质量与证据可用性检查"; }
    @Override public String abnormalSubtype() { return "轨迹数据质量"; }
    @Override protected ObjectType objectType() { return ObjectType.SUPPORT; }
}
