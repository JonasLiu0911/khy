package com.example.ky.analysis.domain.result.model;

import com.example.ky.analysis.domain.common.AbnormalLevel;
import com.example.ky.analysis.domain.vehicle.model.TrackAnalysisResult;
import com.example.ky.analysis.domain.station.model.StationEventScoreResult;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class VehicleAbnormalResult {
    private LocalDate statDate;
    private LocalDateTime windowStart;
    private LocalDateTime windowEnd;
    private String vehicleId;
    private String plateNo;
    private String lineId;
    private String lineName;
    private double trackScore;
    private double eventScore;
    private double finalScore;
    private AbnormalLevel abnormalLevel;
    private boolean conflictFlag;
    private String evidenceSummary;
    private String suggestion;
    private String algorithmVersion;
    private TrackAnalysisResult trackAnalysis;
    private StationEventScoreResult stationEventAnalysis;

    public LocalDate getStatDate() { return statDate; }
    public void setStatDate(LocalDate statDate) { this.statDate = statDate; }
    public LocalDateTime getWindowStart() { return windowStart; }
    public void setWindowStart(LocalDateTime windowStart) { this.windowStart = windowStart; }
    public LocalDateTime getWindowEnd() { return windowEnd; }
    public void setWindowEnd(LocalDateTime windowEnd) { this.windowEnd = windowEnd; }
    public String getVehicleId() { return vehicleId; }
    public void setVehicleId(String vehicleId) { this.vehicleId = vehicleId; }
    public String getPlateNo() { return plateNo; }
    public void setPlateNo(String plateNo) { this.plateNo = plateNo; }
    public String getLineId() { return lineId; }
    public void setLineId(String lineId) { this.lineId = lineId; }
    public String getLineName() { return lineName; }
    public void setLineName(String lineName) { this.lineName = lineName; }
    public double getTrackScore() { return trackScore; }
    public void setTrackScore(double trackScore) { this.trackScore = trackScore; }
    public double getEventScore() { return eventScore; }
    public void setEventScore(double eventScore) { this.eventScore = eventScore; }
    public double getFinalScore() { return finalScore; }
    public void setFinalScore(double finalScore) { this.finalScore = finalScore; }
    public AbnormalLevel getAbnormalLevel() { return abnormalLevel; }
    public void setAbnormalLevel(AbnormalLevel abnormalLevel) { this.abnormalLevel = abnormalLevel; }
    public boolean isConflictFlag() { return conflictFlag; }
    public void setConflictFlag(boolean conflictFlag) { this.conflictFlag = conflictFlag; }
    public String getEvidenceSummary() { return evidenceSummary; }
    public void setEvidenceSummary(String evidenceSummary) { this.evidenceSummary = evidenceSummary; }
    public String getSuggestion() { return suggestion; }
    public void setSuggestion(String suggestion) { this.suggestion = suggestion; }
    public String getAlgorithmVersion() { return algorithmVersion; }
    public void setAlgorithmVersion(String algorithmVersion) { this.algorithmVersion = algorithmVersion; }
    public TrackAnalysisResult getTrackAnalysis() { return trackAnalysis; }
    public void setTrackAnalysis(TrackAnalysisResult trackAnalysis) { this.trackAnalysis = trackAnalysis; }
    public StationEventScoreResult getStationEventAnalysis() { return stationEventAnalysis; }
    public void setStationEventAnalysis(StationEventScoreResult stationEventAnalysis) { this.stationEventAnalysis = stationEventAnalysis; }
}
