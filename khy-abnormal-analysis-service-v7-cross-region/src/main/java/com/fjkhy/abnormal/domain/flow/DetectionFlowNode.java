package com.fjkhy.abnormal.domain.flow;

import java.util.ArrayList;
import java.util.List;

public class DetectionFlowNode {
    private String flowCode;
    private String nodeCode;
    private String nodeName;
    private DetectionFlowNodeType nodeType;
    private String detectorCode;
    private String ruleCode;
    private int orderNo;
    private boolean enabled;
    private boolean required;
    private DetectionFailPolicy failPolicy;
    private List<String> dependsOn = new ArrayList<>();

    public DetectionFlowNode() {}
    public DetectionFlowNode(String flowCode, String nodeCode, String nodeName, DetectionFlowNodeType nodeType,
                             String detectorCode, String ruleCode, int orderNo, boolean enabled,
                             boolean required, DetectionFailPolicy failPolicy) {
        this.flowCode = flowCode;
        this.nodeCode = nodeCode;
        this.nodeName = nodeName;
        this.nodeType = nodeType;
        this.detectorCode = detectorCode;
        this.ruleCode = ruleCode;
        this.orderNo = orderNo;
        this.enabled = enabled;
        this.required = required;
        this.failPolicy = failPolicy;
    }
    public String getFlowCode() { return flowCode; }
    public void setFlowCode(String flowCode) { this.flowCode = flowCode; }
    public String getNodeCode() { return nodeCode; }
    public void setNodeCode(String nodeCode) { this.nodeCode = nodeCode; }
    public String getNodeName() { return nodeName; }
    public void setNodeName(String nodeName) { this.nodeName = nodeName; }
    public DetectionFlowNodeType getNodeType() { return nodeType; }
    public void setNodeType(DetectionFlowNodeType nodeType) { this.nodeType = nodeType; }
    public String getDetectorCode() { return detectorCode; }
    public void setDetectorCode(String detectorCode) { this.detectorCode = detectorCode; }
    public String getRuleCode() { return ruleCode; }
    public void setRuleCode(String ruleCode) { this.ruleCode = ruleCode; }
    public int getOrderNo() { return orderNo; }
    public void setOrderNo(int orderNo) { this.orderNo = orderNo; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public boolean isRequired() { return required; }
    public void setRequired(boolean required) { this.required = required; }
    public DetectionFailPolicy getFailPolicy() { return failPolicy; }
    public void setFailPolicy(DetectionFailPolicy failPolicy) { this.failPolicy = failPolicy; }
    public List<String> getDependsOn() { return dependsOn; }
    public void setDependsOn(List<String> dependsOn) { this.dependsOn = dependsOn; }
}
