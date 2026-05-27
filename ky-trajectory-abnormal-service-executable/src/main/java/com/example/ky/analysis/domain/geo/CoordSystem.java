package com.example.ky.analysis.domain.geo;

import java.util.Locale;

/**
 * 坐标系枚举。
 *
 * <p>说明：</p>
 * <ul>
 *     <li>WGS84：GPS 设备原始经纬度坐标，作为轨迹异常检测算法默认坐标系。</li>
 *     <li>CGCS2000：国内测绘/政务数据常见经纬度坐标。本项目普通轨迹展示场景中可近似按 WGS84 经纬度处理。</li>
 *     <li>GCJ02：高德、腾讯等互联网地图常用火星坐标，主要用于前端展示。</li>
 *     <li>BD09：百度坐标，当前预留，暂未实现转换。</li>
 * </ul>
 */
public enum CoordSystem {
    WGS84,
    CGCS2000,
    GCJ02,
    BD09;

    public static CoordSystem fromNullable(String value, CoordSystem defaultValue) {
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        String normalized = value.trim()
                .replace("-", "")
                .replace("_", "")
                .toUpperCase(Locale.ROOT);
        if ("GCJ".equals(normalized) || "GCJ02".equals(normalized)) {
            return GCJ02;
        }
        if ("WGS".equals(normalized) || "WGS84".equals(normalized)) {
            return WGS84;
        }
        if ("CGCS".equals(normalized) || "CGCS2000".equals(normalized)) {
            return CGCS2000;
        }
        if ("BD".equals(normalized) || "BD09".equals(normalized)) {
            return BD09;
        }
        throw new IllegalArgumentException("Unsupported coordinate system: " + value);
    }
}
