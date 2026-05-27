package com.fjkhy.abnormal.domain.flow;

import java.time.LocalDateTime;

public class DetectionStepLog {
    private String taskId;
    private String flowCode;
    private String nodeCode;
    private String nodeName;
    private String detectorCode;
    private String status;
    private int processedCount;
    private int abnormalCount;
    private String errorMessage;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    public DetectionStepLog() {}
    public DetectionStepLog(String taskId, String flowCode, String nodeCode, String nodeName, String detectorCode) {
        this.taskId = taskId;
        this.flowCode = flowCode;
        this.nodeCode = nodeCode;
        this.nodeName = nodeName;
        this.detectorCode = detectorCode;
        this.startTime = LocalDateTime.now();
        this.status = "RUNNING";
    }
    public void success(int processedCount, int abnormalCount) { this.status = "SUCCESS"; this.processedCount = processedCount; this.abnormalCount = abnormalCount; this.endTime = LocalDateTime.now(); }
    public void skipped(String message) { this.status = "SKIPPED"; this.errorMessage = message; this.endTime = LocalDateTime.now(); }
    public void failed(String message) { this.status = "FAILED"; this.errorMessage = message; this.endTime = LocalDateTime.now(); }
    public String getTaskId() { return taskId; }
    public String getFlowCode() { return flowCode; }
    public String getNodeCode() { return nodeCode; }
    public String getNodeName() { return nodeName; }
    public String getDetectorCode() { return detectorCode; }
    public String getStatus() { return status; }
    public int getProcessedCount() { return processedCount; }
    public int getAbnormalCount() { return abnormalCount; }
    public String getErrorMessage() { return errorMessage; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
}
