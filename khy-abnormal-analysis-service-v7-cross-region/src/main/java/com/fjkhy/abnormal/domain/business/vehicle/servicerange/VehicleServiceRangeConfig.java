package com.fjkhy.abnormal.domain.business.vehicle.servicerange;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * 车辆疑似线路服务范围外运行检测阈值配置。
 *
 * 注意：当前版本未依赖乡镇/区县行政边界 polygon，不输出确定性“跨乡镇/跨县”结论，
 * 仅基于车辆绑定线路站点集合、全量站点邻近关系和过站记录输出辅助提示。
 */
@Component
public class VehicleServiceRangeConfig {
    private final Environment env;

    public VehicleServiceRangeConfig(Environment env) {
        this.env = env;
    }

    public int maxVehiclesPerRun() { return intVal("khy.abnormal.cross-region.max-vehicles-per-run", 200); }
    public int stationRadiusMeters() { return intVal("khy.abnormal.cross-region.station-radius-m", 500); }
    public int corridorRadiusMeters() { return intVal("khy.abnormal.cross-region.corridor-radius-m", 1000); }
    public int otherStationRadiusMeters() { return intVal("khy.abnormal.cross-region.other-station-radius-m", 500); }
    public int minValidPoints() { return intVal("khy.abnormal.cross-region.min-valid-points", 30); }
    public int gpsGapThresholdMinutes() { return intVal("khy.abnormal.cross-region.gps-gap-threshold-min", 10); }
    public int minOutsideDurationMinutes() { return intVal("khy.abnormal.cross-region.min-outside-duration-min", 15); }
    public double outsidePointRatioThreshold() { return doubleVal("khy.abnormal.cross-region.outside-point-ratio-threshold", 0.60); }
    public int nearUnboundStationCountThreshold() { return intVal("khy.abnormal.cross-region.near-unbound-station-count-threshold", 3); }
    public double gpsDriftSpeedThresholdKmh() { return doubleVal("khy.abnormal.cross-region.gps-drift-speed-threshold-kmh", 120.0); }
    public int eventMergeWindowMinutes() { return intVal("khy.abnormal.cross-region.event-merge-window-min", 10); }

    /**
     * 算法内部统一使用的计算坐标系。
     *
     * GCJ02：站点坐标保持火星坐标；车辆轨迹优先使用 vehicle_gps_points.glng/glat，
     *        glng/glat 为空时由后端将 lng/lat 从 WGS84 转为 GCJ-02。
     * WGS84：车辆轨迹使用 lng/lat；站点视图中的 longitude/latitude 也必须为 WGS84。
     */
    public String coordinateMode() { return env.getProperty("khy.abnormal.cross-region.coordinate-mode", "GCJ02"); }

    /** 站点视图坐标系，仅用于配置展示和联调提示。 */
    public String stationCoordinateMode() { return env.getProperty("khy.abnormal.cross-region.station-coordinate-mode", "GCJ02"); }

    /** GPS 原始 lng/lat 坐标系，仅用于配置展示和联调提示。 */
    public String gpsRawCoordinateMode() { return env.getProperty("khy.abnormal.cross-region.gps-raw-coordinate-mode", "WGS84"); }

    private int intVal(String key, int defaultValue) {
        String v = env.getProperty(key);
        if (v == null || v.isBlank()) return defaultValue;
        try { return Integer.parseInt(v.trim()); } catch (Exception e) { return defaultValue; }
    }

    private double doubleVal(String key, double defaultValue) {
        String v = env.getProperty(key);
        if (v == null || v.isBlank()) return defaultValue;
        try { return Double.parseDouble(v.trim()); } catch (Exception e) { return defaultValue; }
    }
}
