package com.fjkhy.abnormal.application.flow;

import com.fjkhy.abnormal.domain.flow.DetectionFlow;
import com.fjkhy.abnormal.domain.flow.DetectionFlowNode;
import com.fjkhy.abnormal.domain.flow.DetectionFlowPlan;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DetectionFlowPlanBuilder {
    private final DetectionFlowConfigService configService;
    public DetectionFlowPlanBuilder(DetectionFlowConfigService configService) { this.configService = configService; }
    public DetectionFlowPlan build(String flowCode) {
        DetectionFlow flow = configService.getFlow(flowCode);
        List<DetectionFlowNode> nodes = configService.listNodes(flowCode).stream().filter(DetectionFlowNode::isEnabled).toList();
        return new DetectionFlowPlan(flow, nodes);
    }
}
