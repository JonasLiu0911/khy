package com.fjkhy.abnormal.domain.result;

import com.fjkhy.abnormal.domain.common.CalcStatus;
import com.fjkhy.abnormal.domain.common.ObjectType;
import com.fjkhy.abnormal.domain.common.RiskLevel;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AbnormalResult {
    private String abnormalId;
    private String taskId;
    private ObjectType objectType;
    private String objectId;
    private String objectName;
    private String countyName;
    private String abnormalCategory;
    private String abnormalSubtype;
    private String ruleCode;
    private String detectorCode;
    private String flowCode;
    private String nodeCode;
    private String period;
    private RiskLevel riskLevel;
    private CalcStatus calcStatus;
    private String evidenceSummary;
    private String reviewStatus;
    private LocalDateTime generatedTime;
    private List<EvidenceRecord> evidenceRecords = new ArrayList<>();

    public AbnormalResult() {}

    public static AbnormalResult demo(String abnormalId, String taskId, ObjectType objectType, String objectName,
                                      String countyName, String category, String subtype, String detectorCode,
                                      String flowCode, String nodeCode, String period, RiskLevel riskLevel,
                                      CalcStatus calcStatus, String evidenceSummary) {
        AbnormalResult r = new AbnormalResult();
        r.abnormalId = abnormalId;
        r.taskId = taskId;
        r.objectType = objectType;
        r.objectId = objectType.name() + "-" + Math.abs(objectName.hashCode());
        r.objectName = objectName;
        r.countyName = countyName;
        r.abnormalCategory = category;
        r.abnormalSubtype = subtype;
        r.ruleCode = detectorCode + "_RULE";
        r.detectorCode = detectorCode;
        r.flowCode = flowCode;
        r.nodeCode = nodeCode;
        r.period = period;
        r.riskLevel = riskLevel;
        r.calcStatus = calcStatus;
        r.evidenceSummary = evidenceSummary;
        r.reviewStatus = "待复核";
        r.generatedTime = LocalDateTime.now();
        return r;
    }

    public String getAbnormalId() { return abnormalId; }
    public void setAbnormalId(String abnormalId) { this.abnormalId = abnormalId; }
    public String getTaskId() { return taskId; }
    public void setTaskId(String taskId) { this.taskId = taskId; }
    public ObjectType getObjectType() { return objectType; }
    public void setObjectType(ObjectType objectType) { this.objectType = objectType; }
    public String getObjectId() { return objectId; }
    public void setObjectId(String objectId) { this.objectId = objectId; }
    public String getObjectName() { return objectName; }
    public void setObjectName(String objectName) { this.objectName = objectName; }
    public String getCountyName() { return countyName; }
    public void setCountyName(String countyName) { this.countyName = countyName; }
    public String getAbnormalCategory() { return abnormalCategory; }
    public void setAbnormalCategory(String abnormalCategory) { this.abnormalCategory = abnormalCategory; }
    public String getAbnormalSubtype() { return abnormalSubtype; }
    public void setAbnormalSubtype(String abnormalSubtype) { this.abnormalSubtype = abnormalSubtype; }
    public String getRuleCode() { return ruleCode; }
    public void setRuleCode(String ruleCode) { this.ruleCode = ruleCode; }
    public String getDetectorCode() { return detectorCode; }
    public void setDetectorCode(String detectorCode) { this.detectorCode = detectorCode; }
    public String getFlowCode() { return flowCode; }
    public void setFlowCode(String flowCode) { this.flowCode = flowCode; }
    public String getNodeCode() { return nodeCode; }
    public void setNodeCode(String nodeCode) { this.nodeCode = nodeCode; }
    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }
    public RiskLevel getRiskLevel() { return riskLevel; }
    public void setRiskLevel(RiskLevel riskLevel) { this.riskLevel = riskLevel; }
    public CalcStatus getCalcStatus() { return calcStatus; }
    public void setCalcStatus(CalcStatus calcStatus) { this.calcStatus = calcStatus; }
    public String getEvidenceSummary() { return evidenceSummary; }
    public void setEvidenceSummary(String evidenceSummary) { this.evidenceSummary = evidenceSummary; }
    public String getReviewStatus() { return reviewStatus; }
    public void setReviewStatus(String reviewStatus) { this.reviewStatus = reviewStatus; }
    public LocalDateTime getGeneratedTime() { return generatedTime; }
    public void setGeneratedTime(LocalDateTime generatedTime) { this.generatedTime = generatedTime; }
    public List<EvidenceRecord> getEvidenceRecords() { return evidenceRecords; }
    public void setEvidenceRecords(List<EvidenceRecord> evidenceRecords) { this.evidenceRecords = evidenceRecords; }
}
