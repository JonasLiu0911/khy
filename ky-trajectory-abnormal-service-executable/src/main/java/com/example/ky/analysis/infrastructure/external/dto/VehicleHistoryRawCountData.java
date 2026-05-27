package com.example.ky.analysis.infrastructure.external.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class VehicleHistoryRawCountData {
    @JsonProperty("move_long")
    private String moveLong;
    @JsonProperty("avg_speed")
    private String avgSpeed;
    @JsonProperty("gps_size")
    private String gpsSize;
    private String mile;
    @JsonProperty("move_long_num")
    private String moveLongNum;
    @JsonProperty("max_speed")
    private String maxSpeed;
    @JsonProperty("stop_long_num")
    private String stopLongNum;
    @JsonProperty("avg_move")
    private String avgMove;
    @JsonProperty("stop_long")
    private String stopLong;

    public String getMoveLong() {
        return moveLong;
    }

    public void setMoveLong(String moveLong) {
        this.moveLong = moveLong;
    }

    public String getAvgSpeed() {
        return avgSpeed;
    }

    public void setAvgSpeed(String avgSpeed) {
        this.avgSpeed = avgSpeed;
    }

    public String getGpsSize() {
        return gpsSize;
    }

    public void setGpsSize(String gpsSize) {
        this.gpsSize = gpsSize;
    }

    public String getMile() {
        return mile;
    }

    public void setMile(String mile) {
        this.mile = mile;
    }

    public String getMoveLongNum() {
        return moveLongNum;
    }

    public void setMoveLongNum(String moveLongNum) {
        this.moveLongNum = moveLongNum;
    }

    public String getMaxSpeed() {
        return maxSpeed;
    }

    public void setMaxSpeed(String maxSpeed) {
        this.maxSpeed = maxSpeed;
    }

    public String getStopLongNum() {
        return stopLongNum;
    }

    public void setStopLongNum(String stopLongNum) {
        this.stopLongNum = stopLongNum;
    }

    public String getAvgMove() {
        return avgMove;
    }

    public void setAvgMove(String avgMove) {
        this.avgMove = avgMove;
    }

    public String getStopLong() {
        return stopLong;
    }

    public void setStopLong(String stopLong) {
        this.stopLong = stopLong;
    }
}
