package com.example.ky.analysis.infrastructure.external.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class VehicleHistoryRawResponse {
    private String rspCode;
    private List<VehicleHistoryRawStopData> stopData;
    private VehicleHistoryRawCountData countData;
    @JsonProperty("list")
    private List<VehicleHistoryRawPoint> points;

    public String getRspCode() {
        return rspCode;
    }

    public void setRspCode(String rspCode) {
        this.rspCode = rspCode;
    }

    public List<VehicleHistoryRawStopData> getStopData() {
        return stopData;
    }

    public void setStopData(List<VehicleHistoryRawStopData> stopData) {
        this.stopData = stopData;
    }

    public VehicleHistoryRawCountData getCountData() {
        return countData;
    }

    public void setCountData(VehicleHistoryRawCountData countData) {
        this.countData = countData;
    }

    public List<VehicleHistoryRawPoint> getPoints() {
        return points;
    }

    public void setPoints(List<VehicleHistoryRawPoint> points) {
        this.points = points;
    }
}
