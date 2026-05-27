package com.fjkhy.abnormal.domain.flow;

import java.util.List;

public record DetectionFlowPlan(DetectionFlow flow, List<DetectionFlowNode> nodes) {}
