package com.fjkhy.abnormal.domain.flow;

public class DetectionThreshold {
    private String thresholdCode;
    private String thresholdName;
    private String groupName;
    private String value;
    private String unit;
    private String description;

    public DetectionThreshold() {}
    public DetectionThreshold(String thresholdCode, String thresholdName, String groupName, String value, String unit, String description) {
        this.thresholdCode = thresholdCode;
        this.thresholdName = thresholdName;
        this.groupName = groupName;
        this.value = value;
        this.unit = unit;
        this.description = description;
    }
    public String getThresholdCode() { return thresholdCode; }
    public void setThresholdCode(String thresholdCode) { this.thresholdCode = thresholdCode; }
    public String getThresholdName() { return thresholdName; }
    public void setThresholdName(String thresholdName) { this.thresholdName = thresholdName; }
    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
