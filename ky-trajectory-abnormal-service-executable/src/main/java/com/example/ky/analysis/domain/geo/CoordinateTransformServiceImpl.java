package com.example.ky.analysis.domain.geo;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Collections;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 坐标转换服务默认实现。
 *
 * <p>CGCS2000 与 WGS84 在本项目车辆轨迹可视化、站点匹配等普通业务精度场景中按经纬度近似等价处理。
 * 若后续接入 CGCS2000 高斯投影平面坐标，必须补充中央子午线、投影带号、地方转换参数等测绘参数，
 * 不应直接调用本服务进行平面坐标转换。</p>
 */
@Service
public class CoordinateTransformServiceImpl implements CoordinateTransformService {

    private static final double PI = Math.PI;
    private static final double A = 6378245.0;
    private static final double EE = 0.00669342162296594323;

    @Override
    public GeoPoint wgs84ToGcj02(double lng, double lat) {
        validateLngLat(lng, lat);
        if (outOfChina(lng, lat)) {
            return new GeoPoint(lng, lat, CoordSystem.WGS84);
        }

        double dLat = transformLat(lng - 105.0, lat - 35.0);
        double dLng = transformLng(lng - 105.0, lat - 35.0);

        double radLat = lat / 180.0 * PI;
        double magic = Math.sin(radLat);
        magic = 1 - EE * magic * magic;
        double sqrtMagic = Math.sqrt(magic);

        dLat = (dLat * 180.0) / ((A * (1 - EE)) / (magic * sqrtMagic) * PI);
        dLng = (dLng * 180.0) / (A / sqrtMagic * Math.cos(radLat) * PI);

        return new GeoPoint(lng + dLng, lat + dLat, CoordSystem.GCJ02);
    }

    @Override
    public GeoPoint gcj02ToWgs84(double lng, double lat) {
        validateLngLat(lng, lat);
        if (outOfChina(lng, lat)) {
            return new GeoPoint(lng, lat, CoordSystem.WGS84);
        }

        GeoPoint gcj = wgs84ToGcj02(lng, lat);
        double dLng = gcj.getLng() - lng;
        double dLat = gcj.getLat() - lat;
        return new GeoPoint(lng - dLng, lat - dLat, CoordSystem.WGS84);
    }

    @Override
    public GeoPoint gcj02ToWgs84Exact(double lng, double lat) {
        validateLngLat(lng, lat);
        if (outOfChina(lng, lat)) {
            return new GeoPoint(lng, lat, CoordSystem.WGS84);
        }

        double initDelta = 0.01;
        double threshold = 0.000001;

        double minLat = lat - initDelta;
        double minLng = lng - initDelta;
        double maxLat = lat + initDelta;
        double maxLng = lng + initDelta;

        double wgsLat = lat;
        double wgsLng = lng;

        for (int i = 0; i < 30; i++) {
            wgsLat = (minLat + maxLat) / 2;
            wgsLng = (minLng + maxLng) / 2;

            GeoPoint tmp = wgs84ToGcj02(wgsLng, wgsLat);
            double dLat = tmp.getLat() - lat;
            double dLng = tmp.getLng() - lng;

            if (Math.abs(dLat) < threshold && Math.abs(dLng) < threshold) {
                return new GeoPoint(wgsLng, wgsLat, CoordSystem.WGS84);
            }

            if (dLat > 0) {
                maxLat = wgsLat;
            } else {
                minLat = wgsLat;
            }

            if (dLng > 0) {
                maxLng = wgsLng;
            } else {
                minLng = wgsLng;
            }
        }

        return new GeoPoint(wgsLng, wgsLat, CoordSystem.WGS84);
    }

    @Override
    public GeoPoint cgcs2000ToGcj02(double lng, double lat) {
        // 本项目默认处理的是 CGCS2000 经纬度坐标，可近似按 WGS84 转 GCJ-02。
        GeoPoint point = wgs84ToGcj02(lng, lat);
        point.setCoordSystem(CoordSystem.GCJ02);
        return point;
    }

    @Override
    public GeoPoint gcj02ToCgcs2000(double lng, double lat) {
        GeoPoint point = gcj02ToWgs84Exact(lng, lat);
        point.setCoordSystem(CoordSystem.CGCS2000);
        return point;
    }

    @Override
    public GeoPoint wgs84ToCgcs2000(double lng, double lat) {
        validateLngLat(lng, lat);
        return new GeoPoint(lng, lat, CoordSystem.CGCS2000);
    }

    @Override
    public GeoPoint cgcs2000ToWgs84(double lng, double lat) {
        validateLngLat(lng, lat);
        return new GeoPoint(lng, lat, CoordSystem.WGS84);
    }

    @Override
    public GeoPoint transform(double lng, double lat, CoordSystem source, CoordSystem target) {
        validateLngLat(lng, lat);
        CoordSystem sourceSystem = source == null ? CoordSystem.WGS84 : source;
        CoordSystem targetSystem = target == null ? sourceSystem : target;

        if (sourceSystem == targetSystem) {
            return new GeoPoint(lng, lat, targetSystem);
        }

        if (sourceSystem == CoordSystem.WGS84 && targetSystem == CoordSystem.GCJ02) {
            return wgs84ToGcj02(lng, lat);
        }
        if (sourceSystem == CoordSystem.GCJ02 && targetSystem == CoordSystem.WGS84) {
            return gcj02ToWgs84Exact(lng, lat);
        }
        if (sourceSystem == CoordSystem.CGCS2000 && targetSystem == CoordSystem.GCJ02) {
            return cgcs2000ToGcj02(lng, lat);
        }
        if (sourceSystem == CoordSystem.GCJ02 && targetSystem == CoordSystem.CGCS2000) {
            return gcj02ToCgcs2000(lng, lat);
        }
        if (sourceSystem == CoordSystem.WGS84 && targetSystem == CoordSystem.CGCS2000) {
            return wgs84ToCgcs2000(lng, lat);
        }
        if (sourceSystem == CoordSystem.CGCS2000 && targetSystem == CoordSystem.WGS84) {
            return cgcs2000ToWgs84(lng, lat);
        }

        throw new IllegalArgumentException("Unsupported coordinate transform: " + sourceSystem + " -> " + targetSystem);
    }

    @Override
    public List<GeoPoint> transformBatch(List<GeoPoint> points, CoordSystem source, CoordSystem target) {
        if (points == null || points.isEmpty()) {
            return Collections.emptyList();
        }
        return points.stream()
                .filter(Objects::nonNull)
                .map(point -> transform(point.getLng(), point.getLat(),
                        point.getCoordSystem() == null ? source : point.getCoordSystem(), target))
                .collect(Collectors.toList());
    }

    private void validateLngLat(double lng, double lat) {
        if (Double.isNaN(lng) || Double.isNaN(lat) || Double.isInfinite(lng) || Double.isInfinite(lat)) {
            throw new IllegalArgumentException("Invalid coordinate: lng=" + lng + ", lat=" + lat);
        }
        if (lng < -180.0 || lng > 180.0 || lat < -90.0 || lat > 90.0) {
            throw new IllegalArgumentException("Coordinate out of range: lng=" + lng + ", lat=" + lat);
        }
    }

    private boolean outOfChina(double lng, double lat) {
        return lng < 72.004 || lng > 137.8347 || lat < 0.8293 || lat > 55.8271;
    }

    private double transformLat(double x, double y) {
        double ret = -100.0 + 2.0 * x + 3.0 * y
                + 0.2 * y * y + 0.1 * x * y
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
                + 0.1 * x * x + 0.1 * x * y
                + 0.1 * Math.sqrt(Math.abs(x));
        ret += (20.0 * Math.sin(6.0 * x * PI)
                + 20.0 * Math.sin(2.0 * x * PI)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(x * PI)
                + 40.0 * Math.sin(x / 3.0 * PI)) * 2.0 / 3.0;
        ret += (150.0 * Math.sin(x / 12.0 * PI)
                + 300.0 * Math.sin(x / 30.0 * PI)) * 2.0 / 3.0;
        return ret;
    }
}
