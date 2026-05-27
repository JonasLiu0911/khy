package com.example.ky.analysis.domain.geo;

import java.util.List;

/**
 * 坐标转换服务。
 *
 * <p>项目约定：</p>
 * <ul>
 *     <li>算法层优先使用 GPS 原始坐标，即 WGS84 或近似 WGS84 的 CGCS2000 经纬度坐标。</li>
 *     <li>展示层根据底图需要转换，使用高德/腾讯地图时一般输出 GCJ-02。</li>
 *     <li>GCJ-02 到 WGS84/CGCS2000 为近似反算，基础站点/线路数据建议使用 exact 方法。</li>
 * </ul>
 */
public interface CoordinateTransformService {

    GeoPoint wgs84ToGcj02(double lng, double lat);

    GeoPoint gcj02ToWgs84(double lng, double lat);

    GeoPoint gcj02ToWgs84Exact(double lng, double lat);

    GeoPoint cgcs2000ToGcj02(double lng, double lat);

    GeoPoint gcj02ToCgcs2000(double lng, double lat);

    GeoPoint wgs84ToCgcs2000(double lng, double lat);

    GeoPoint cgcs2000ToWgs84(double lng, double lat);

    GeoPoint transform(double lng, double lat, CoordSystem source, CoordSystem target);

    List<GeoPoint> transformBatch(List<GeoPoint> points, CoordSystem source, CoordSystem target);
}
