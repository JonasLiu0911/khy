package com.fjkhy.abnormal.domain.business.line;

import com.fjkhy.abnormal.domain.common.ObjectType;
import com.fjkhy.abnormal.domain.detection.AbstractDemoDetector;
import org.springframework.stereotype.Component;

@Component
public class LineFrequencyInsufficientDetector extends AbstractDemoDetector {
    @Override public String detectorCode() { return "LINE_FREQUENCY_INSUFFICIENT"; }
    @Override public String detectorName() { return "线路周频次或班次执行不足检测"; }
    @Override public String objectTypeName() { return "线路"; }
    @Override public String abnormalCategory() { return "线路配置与执行合规异常"; }
    @Override public String abnormalSubtype() { return "线路周频次或班次执行不足"; }
    @Override protected ObjectType objectType() { return ObjectType.LINE; }
}
