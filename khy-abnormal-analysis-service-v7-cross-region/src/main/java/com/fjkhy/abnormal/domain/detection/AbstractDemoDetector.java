package com.fjkhy.abnormal.domain.detection;

import com.fjkhy.abnormal.domain.common.CalcStatus;
import com.fjkhy.abnormal.domain.common.ObjectType;
import com.fjkhy.abnormal.domain.common.RiskLevel;
import com.fjkhy.abnormal.domain.result.AbnormalResult;

import java.util.List;
import java.util.UUID;

public abstract class AbstractDemoDetector implements AbnormalDetector {
    protected abstract ObjectType objectType();

    @Override
    public boolean supports(DetectionContext context) {
        return context.objectType() == ObjectType.ALL || context.objectType() == objectType();
    }

    @Override
    public List<AbnormalResult> detect(DetectionContext context) {
        if (!supports(context)) {
            return List.of();
        }
        String objectName = demoObjectName(context);
        String summary = demoEvidenceSummary(context);
        AbnormalResult result = AbnormalResult.demo(
                "ABN-" + UUID.randomUUID().toString().substring(0, 8),
                context.taskId(),
                objectType(),
                objectName,
                context.countyName(),
                abnormalCategory(),
                abnormalSubtype(),
                detectorCode(),
                context.flowCode(),
                context.currentNodeCode(),
                context.period(),
                defaultRiskLevel(),
                CalcStatus.CALCULABLE,
                summary
        );
        return List.of(result);
    }

    protected RiskLevel defaultRiskLevel() { return RiskLevel.MEDIUM; }
    protected String demoObjectName(DetectionContext context) { return objectTypeName() + "示例对象"; }
    protected String demoEvidenceSummary(DetectionContext context) { return detectorName() + "命中示例证据，实际项目中由对应数据适配器读取真实轨迹、过站、台账或材料数据。"; }
}
