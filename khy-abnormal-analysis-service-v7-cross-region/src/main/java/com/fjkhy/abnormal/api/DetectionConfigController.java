package com.fjkhy.abnormal.api;

import com.fjkhy.abnormal.application.flow.DetectionFlowConfigService;
import com.fjkhy.abnormal.domain.detection.DetectorRegistry;
import com.fjkhy.abnormal.domain.flow.DetectionFlow;
import com.fjkhy.abnormal.domain.flow.DetectionFlowNode;
import com.fjkhy.abnormal.domain.flow.DetectionRuleView;
import com.fjkhy.abnormal.domain.flow.DetectionThreshold;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/detection")
@CrossOrigin
public class DetectionConfigController {
    private final DetectionFlowConfigService configService;
    private final DetectorRegistry detectorRegistry;
    public DetectionConfigController(DetectionFlowConfigService configService, DetectorRegistry detectorRegistry) {
        this.configService = configService;
        this.detectorRegistry = detectorRegistry;
    }
    @GetMapping("/flows") public Object flows() { return configService.listFlows(); }
    @PostMapping("/flows") public Object createFlow(@RequestBody DetectionFlow flow) { return configService.createFlow(flow); }
    @PutMapping("/flows/{flowCode}") public Object updateFlow(@PathVariable String flowCode, @RequestBody DetectionFlow flow) { flow.setFlowCode(flowCode); return configService.updateFlow(flow); }
    @DeleteMapping("/flows/{flowCode}") public Object deleteFlow(@PathVariable String flowCode) { configService.deleteFlow(flowCode); return Map.of("ok", true); }
    @GetMapping("/flows/{flowCode}") public Object flow(@PathVariable String flowCode) { return configService.getFlow(flowCode); }
    @GetMapping("/flows/{flowCode}/nodes") public Object nodes(@PathVariable String flowCode) { return configService.listNodes(flowCode); }
    @PostMapping("/flows/{flowCode}/nodes") public Object addNode(@PathVariable String flowCode, @RequestBody DetectionFlowNode node) { node.setFlowCode(flowCode); return configService.addNode(node); }
    @PutMapping("/flows/{flowCode}/nodes/{nodeCode}") public Object updateNode(@PathVariable String flowCode, @PathVariable String nodeCode, @RequestBody DetectionFlowNode node) { node.setFlowCode(flowCode); node.setNodeCode(nodeCode); return configService.updateNode(node); }
    @DeleteMapping("/flows/{flowCode}/nodes/{nodeCode}") public Object deleteNode(@PathVariable String flowCode, @PathVariable String nodeCode) { configService.deleteNode(flowCode, nodeCode); return Map.of("ok", true); }
    @PutMapping("/flows/{flowCode}/nodes/{nodeCode}/enabled") public Object enableNode(@PathVariable String flowCode, @PathVariable String nodeCode, @RequestBody Map<String, Boolean> body) { configService.setNodeEnabled(flowCode, nodeCode, Boolean.TRUE.equals(body.get("enabled"))); return Map.of("ok", true); }
    @PutMapping("/flows/{flowCode}/nodes/{nodeCode}/order") public Object reorder(@PathVariable String flowCode, @PathVariable String nodeCode, @RequestBody Map<String, Integer> body) { configService.reorderNode(flowCode, nodeCode, body.getOrDefault("orderNo", 999)); return Map.of("ok", true); }
    @GetMapping("/rules") public Object rules() { return configService.listRules(); }
    @PostMapping("/rules") public Object createRule(@RequestBody DetectionRuleView rule) { return configService.upsertRule(rule); }
    @PutMapping("/rules/{ruleCode}") public Object updateRule(@PathVariable String ruleCode, @RequestBody DetectionRuleView rule) { return configService.upsertRule(new DetectionRuleView(ruleCode, rule.ruleName(), rule.objectType(), rule.abnormalCategory(), rule.abnormalSubtype(), rule.detectorCode(), rule.enabled(), rule.version(), rule.dataSource())); }
    @DeleteMapping("/rules/{ruleCode}") public Object deleteRule(@PathVariable String ruleCode) { configService.deleteRule(ruleCode); return Map.of("ok", true); }
    @GetMapping("/thresholds") public Object thresholds() { return configService.listThresholds(); }
    @PostMapping("/thresholds") public Object createThreshold(@RequestBody DetectionThreshold threshold) { return configService.upsertThreshold(threshold); }
    @PutMapping("/thresholds/{thresholdCode}") public Object updateThreshold(@PathVariable String thresholdCode, @RequestBody DetectionThreshold threshold) { threshold.setThresholdCode(thresholdCode); return configService.upsertThreshold(threshold); }
    @DeleteMapping("/thresholds/{thresholdCode}") public Object deleteThreshold(@PathVariable String thresholdCode) { configService.deleteThreshold(thresholdCode); return Map.of("ok", true); }
    @GetMapping("/detectors") public Object detectors() { return detectorRegistry.descriptors(); }
}
