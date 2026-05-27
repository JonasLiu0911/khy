package com.example.ky.analysis.domain.config;

public class TrajectoryAnalysisConfig {
    private boolean enabled = true;
    private String scheduleMode = "DAILY";
    private String dailyCron = "0 30 0 * * ?";
    private int windowDays = 30;
    private int minValidTripCount = 5;
    private int stationRadiusMeters = 500;
    private int gridSizeMeters = 1000;
    private int maxGapMinutes = 30;
    private int minTripPointCount = 8;
    private double minTripDistanceKm = 1.0;
    private double clusterSimilarityThreshold = 0.52;
    private double suspectScoreThreshold = 60.0;
    private double highRiskScoreThreshold = 40.0;
    private String algorithmVersion = "v1.0-demo";

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public String getScheduleMode() { return scheduleMode; }
    public void setScheduleMode(String scheduleMode) { this.scheduleMode = scheduleMode; }
    public String getDailyCron() { return dailyCron; }
    public void setDailyCron(String dailyCron) { this.dailyCron = dailyCron; }
    public int getWindowDays() { return windowDays; }
    public void setWindowDays(int windowDays) { this.windowDays = windowDays; }
    public int getMinValidTripCount() { return minValidTripCount; }
    public void setMinValidTripCount(int minValidTripCount) { this.minValidTripCount = minValidTripCount; }
    public int getStationRadiusMeters() { return stationRadiusMeters; }
    public void setStationRadiusMeters(int stationRadiusMeters) { this.stationRadiusMeters = stationRadiusMeters; }
    public int getGridSizeMeters() { return gridSizeMeters; }
    public void setGridSizeMeters(int gridSizeMeters) { this.gridSizeMeters = gridSizeMeters; }
    public int getMaxGapMinutes() { return maxGapMinutes; }
    public void setMaxGapMinutes(int maxGapMinutes) { this.maxGapMinutes = maxGapMinutes; }
    public int getMinTripPointCount() { return minTripPointCount; }
    public void setMinTripPointCount(int minTripPointCount) { this.minTripPointCount = minTripPointCount; }
    public double getMinTripDistanceKm() { return minTripDistanceKm; }
    public void setMinTripDistanceKm(double minTripDistanceKm) { this.minTripDistanceKm = minTripDistanceKm; }
    public double getClusterSimilarityThreshold() { return clusterSimilarityThreshold; }
    public void setClusterSimilarityThreshold(double clusterSimilarityThreshold) { this.clusterSimilarityThreshold = clusterSimilarityThreshold; }
    public double getSuspectScoreThreshold() { return suspectScoreThreshold; }
    public void setSuspectScoreThreshold(double suspectScoreThreshold) { this.suspectScoreThreshold = suspectScoreThreshold; }
    public double getHighRiskScoreThreshold() { return highRiskScoreThreshold; }
    public void setHighRiskScoreThreshold(double highRiskScoreThreshold) { this.highRiskScoreThreshold = highRiskScoreThreshold; }
    public String getAlgorithmVersion() { return algorithmVersion; }
    public void setAlgorithmVersion(String algorithmVersion) { this.algorithmVersion = algorithmVersion; }
}
