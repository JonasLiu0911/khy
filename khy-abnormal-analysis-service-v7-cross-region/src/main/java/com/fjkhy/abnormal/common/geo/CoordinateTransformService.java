package com.fjkhy.abnormal.common.geo;

import org.springframework.stereotype.Component;

/**
 * 坐标转换服务。
 *
 * <p>当前客货邮车辆轨迹数据中的 lng/lat 通常为 GPS 原始 WGS84 坐标，
 * 而站点坐标多来自高德/火星坐标系 GCJ-02。距离计算前必须统一坐标系，
 * 否则会导致车辆到站距离整体偏移并引发误判。</p>
 */
@Component
public class CoordinateTransformService {
    private static final double PI = 3.1415926535897932384626;
    private static final double A = 6378245.0;
    private static final double EE = 0.00669342162296594323;

    /**
     * WGS84 转 GCJ-02。中国大陆以外区域原样返回。
     */
    public CoordinatePoint wgs84ToGcj02(double lng, double lat) {
        if (outOfChina(lng, lat)) {
            return new CoordinatePoint(lng, lat);
        }
        double dLat = transformLat(lng - 105.0, lat - 35.0);
        double dLng = transformLng(lng - 105.0, lat - 35.0);
        double radLat = lat / 180.0 * PI;
        double magic = Math.sin(radLat);
        magic = 1 - EE * magic * magic;
        double sqrtMagic = Math.sqrt(magic);
        dLat = (dLat * 180.0) / ((A * (1 - EE)) / (magic * sqrtMagic) * PI);
        dLng = (dLng * 180.0) / (A / sqrtMagic * Math.cos(radLat) * PI);
        return new CoordinatePoint(lng + dLng, lat + dLat);
    }

    /**
     * 根据算法坐标系选择计算坐标。
     *
     * <p>当算法坐标系为 GCJ02 时，优先使用轨迹表中已存储的 glng/glat；
     * 若 glng/glat 为空，则将原始 lng/lat 从 WGS84 转为 GCJ-02。
     * 当算法坐标系为 WGS84 时，直接使用原始 lng/lat。</p>
     */
    public CoordinatePoint toCalculationPoint(String coordinateMode,
                                              Double rawLng,
                                              Double rawLat,
                                              Double gcjLng,
                                              Double gcjLat) {
        if ("GCJ02".equalsIgnoreCase(coordinateMode)) {
            if (gcjLng != null && gcjLat != null) {
                return new CoordinatePoint(gcjLng, gcjLat);
            }
            if (rawLng == null || rawLat == null) {
                return null;
            }
            return wgs84ToGcj02(rawLng, rawLat);
        }
        if (rawLng == null || rawLat == null) {
            return null;
        }
        return new CoordinatePoint(rawLng, rawLat);
    }

    private boolean outOfChina(double lng, double lat) {
        return lng < 72.004 || lng > 137.8347 || lat < 0.8293 || lat > 55.8271;
    }

    private double transformLat(double x, double y) {
        double ret = -100.0 + 2.0 * x + 3.0 * y
                + 0.2 * y * y
                + 0.1 * x * y
                + 0.2 * Math.sqrt(Math.abs(x));
        ret += (20.0 * Math.sin(6.0 * x * PI)
                + 20.0 * Math.sin(2.0 * x * PI)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(y * PI)
                + 40.0 * Math.sin(y / 3.0 * PI)) * 2.0 / 3.0;
        ret += (160.0 * Math.sin(y / 12.0 * PI)
                + 320.0 * Math.sin(y * PI / 30.0)) * 2.0 / 3.0;
        return ret;
    }

    private double transformLng(double x, double y) {
        double ret = 300.0 + x + 2.0 * y
                + 0.1 * x * x
                + 0.1 * x * y
                + 0.1 * Math.sqrt(Math.abs(x));
        ret += (20.0 * Math.sin(6.0 * x * PI)
                + 20.0 * Math.sin(2.0 * x * PI)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(x * PI)
                + 40.0 * Math.sin(x / 3.0 * PI)) * 2.0 / 3.0;
        ret += (150.0 * Math.sin(x / 12.0 * PI)
                + 300.0 * Math.sin(x / 30.0 * PI)) * 2.0 / 3.0;
        return ret;
    }

    public record CoordinatePoint(double lng, double lat) {}
}
