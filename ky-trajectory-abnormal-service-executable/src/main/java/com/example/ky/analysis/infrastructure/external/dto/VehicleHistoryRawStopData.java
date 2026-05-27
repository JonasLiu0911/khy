package com.example.ky.analysis.infrastructure.external.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class VehicleHistoryRawStopData {
    private String add;
    private String lng;
    private String lat;
    private String glng;
    private String glat;
    private String blng;
    private String blat;
    @JsonProperty("startTime")
    private String startTime;
    @JsonProperty("endTime")
    private String endTime;

    public String getAdd() {
        return add;
    }

    public void setAdd(String add) {
        this.add = add;
    }

    public String getLng() {
        return lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getGlng() {
        return glng;
    }

    public void setGlng(String glng) {
        this.glng = glng;
    }

    public String getGlat() {
        return glat;
    }

    public void setGlat(String glat) {
        this.glat = glat;
    }

    public String getBlng() {
        return blng;
    }

    public void setBlng(String blng) {
        this.blng = blng;
    }

    public String getBlat() {
        return blat;
    }

    public void setBlat(String blat) {
        this.blat = blat;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }
}
