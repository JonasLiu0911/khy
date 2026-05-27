package com.fjkhy.abnormal.application.flow;

import com.fjkhy.abnormal.application.result.AbnormalResultService;
import com.fjkhy.abnormal.domain.detection.AbnormalDetector;
import com.fjkhy.abnormal.domain.detection.DetectionContext;
import com.fjkhy.abnormal.domain.detection.DetectorRegistry;
import com.fjkhy.abnormal.domain.flow.*;
import com.fjkhy.abnormal.domain.result.AbnormalResult;
import com.fjkhy.abnormal.domain.task.DetectionTaskRequest;
import com.fjkhy.abnormal.domain.task.DetectionTaskRun;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class DetectionFlowOrchestrator {
    private final DetectionFlowPlanBuilder planBuilder;
    private final DetectorRegistry detectorRegistry;
    private final DetectionStepLogService stepLogService;
    private final AbnormalResultService resultService;

    public DetectionFlowOrchestrator(DetectionFlowPlanBuilder planBuilder,
                                     DetectorRegistry detectorRegistry,
                                     DetectionStepLogService stepLogService,
                                     AbnormalResultService resultService) {
        this.planBuilder = planBuilder;
        this.detectorRegistry = detectorRegistry;
        this.stepLogService = stepLogService;
        this.resultService = resultService;
    }

    public DetectionFlowRunOutput run(DetectionTaskRequest request) {
        String taskId = "TASK-" + UUID.randomUUID().toString().substring(0, 8);
        LocalDateTime start = LocalDateTime.now();
        String flowCode = request.flowCode() == null || request.flowCode().isBlank() ? "VEHICLE_MONTHLY_COMPLIANCE_FLOW" : request.flowCode();
        DetectionFlowPlan plan = planBuilder.build(flowCode);
        DetectionContext context = new DetectionContext(taskId, request, flowCode);
        List<AbnormalResult> allResults = new ArrayList<>();

        for (DetectionFlowNode node : plan.nodes()) {
            context.currentNodeCode(node.getNodeCode());
            DetectionStepLog log = new DetectionStepLog(taskId, flowCode, node.getNodeCode(), node.getNodeName(), node.getDetectorCode());
            try {
                List<AbnormalResult> nodeResults = executeNode(node, context);
                allResults.addAll(nodeResults);
                log.success(1, nodeResults.size());
            } catch (Exception ex) {
                log.failed(ex.getMessage());
                if (node.getFailPolicy() == DetectionFailPolicy.STOP_FLOW) {
                    stepLogService.save(log);
                    break;
                }
            }
            stepLogService.save(log);
        }
        resultService.saveAll(allResults);
        DetectionTaskRun task = new DetectionTaskRun(taskId, flowCode, request.countyName(), request.period(), "SUCCESS", allResults.size(), start, LocalDateTime.now());
        return new DetectionFlowRunOutput(task, stepLogService.listByTask(taskId), allResults);
    }

    private List<AbnormalResult> executeNode(DetectionFlowNode node, DetectionContext context) {
        if (node.getDetectorCode() == null || node.getDetectorCode().isBlank()) {
            return List.of();
        }
        AbnormalDetector detector = detectorRegistry.getRequired(node.getDetectorCode());
        if (!detector.supports(context)) {
            return List.of();
        }
        return detector.detect(context);
    }
}
