package com.fjkhy.abnormal.domain.business.line;

import com.fjkhy.abnormal.domain.common.ObjectType;
import com.fjkhy.abnormal.domain.detection.AbstractDemoDetector;
import org.springframework.stereotype.Component;

@Component
public class LineStationOrderExecutionDetector extends AbstractDemoDetector {
    @Override public String detectorCode() { return "LINE_STATION_ORDER_EXECUTION"; }
    @Override public String detectorName() { return "线路站点顺序执行异常检测"; }
    @Override public String objectTypeName() { return "线路"; }
    @Override public String abnormalCategory() { return "线路配置与执行合规异常"; }
    @Override public String abnormalSubtype() { return "线路站点顺序执行异常"; }
    @Override protected ObjectType objectType() { return ObjectType.LINE; }
}
