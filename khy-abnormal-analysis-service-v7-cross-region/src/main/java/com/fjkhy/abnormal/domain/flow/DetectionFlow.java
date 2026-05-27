package com.fjkhy.abnormal.domain.flow;

import com.fjkhy.abnormal.domain.common.ObjectType;

public class DetectionFlow {
    private String flowCode;
    private String flowName;
    private ObjectType objectType;
    private String description;
    private boolean enabled;
    private String version;

    public DetectionFlow() {}
    public DetectionFlow(String flowCode, String flowName, ObjectType objectType, String description, boolean enabled, String version) {
        this.flowCode = flowCode;
        this.flowName = flowName;
        this.objectType = objectType;
        this.description = description;
        this.enabled = enabled;
        this.version = version;
    }
    public String getFlowCode() { return flowCode; }
    public void setFlowCode(String flowCode) { this.flowCode = flowCode; }
    public String getFlowName() { return flowName; }
    public void setFlowName(String flowName) { this.flowName = flowName; }
    public ObjectType getObjectType() { return objectType; }
    public void setObjectType(ObjectType objectType) { this.objectType = objectType; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
}
