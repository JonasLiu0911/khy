package com.example.ky.analysis.infrastructure.external.dto;

public class VehicleHistoryRawPoint {
    private String lng;
    private String lat;
    private String glng;
    private String glat;
    private String speed;
    private String exts;

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

    public String getSpeed() {
        return speed;
    }

    public void setSpeed(String speed) {
        this.speed = speed;
    }

    public String getExts() {
        return exts;
    }

    public void setExts(String exts) {
        this.exts = exts;
    }
}
