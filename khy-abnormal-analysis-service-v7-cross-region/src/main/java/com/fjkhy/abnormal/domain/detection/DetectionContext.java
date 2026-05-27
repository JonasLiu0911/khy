package com.fjkhy.abnormal.domain.detection;

import com.fjkhy.abnormal.domain.common.ObjectType;
import com.fjkhy.abnormal.domain.task.DetectionTaskRequest;

import java.util.HashMap;
import java.util.Map;

public class DetectionContext {
    private final String taskId;
    private final DetectionTaskRequest request;
    private final String flowCode;
    private String currentNodeCode;
    private final Map<String, Object> attributes = new HashMap<>();

    public DetectionContext(String taskId, DetectionTaskRequest request, String flowCode) {
        this.taskId = taskId;
        this.request = request;
        this.flowCode = flowCode;
    }

    public String taskId() { return taskId; }
    public DetectionTaskRequest request() { return request; }
    public String flowCode() { return flowCode; }
    public ObjectType objectType() { return request.objectType() == null ? ObjectType.ALL : request.objectType(); }
    public String countyName() { return request.countyName() == null ? "全省" : request.countyName(); }
    public String period() { return request.period() == null ? "当前周期" : request.period(); }
    public String currentNodeCode() { return currentNodeCode; }
    public void currentNodeCode(String nodeCode) { this.currentNodeCode = nodeCode; }
    public Map<String, Object> attributes() { return attributes; }
}
