package com.example.ky.analysis.domain.geo;

import java.io.Serializable;

/**
 * 经纬度点。
 * 注意：lng 表示经度，lat 表示纬度。
 */
public class GeoPoint implements Serializable {
    private static final long serialVersionUID = 1L;

    private double lng;
    private double lat;
    private CoordSystem coordSystem;

    public GeoPoint() {
    }

    public GeoPoint(double lng, double lat) {
        this.lng = lng;
        this.lat = lat;
    }

    public GeoPoint(double lng, double lat, CoordSystem coordSystem) {
        this.lng = lng;
        this.lat = lat;
        this.coordSystem = coordSystem;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public CoordSystem getCoordSystem() {
        return coordSystem;
    }

    public void setCoordSystem(CoordSystem coordSystem) {
        this.coordSystem = coordSystem;
    }
}
