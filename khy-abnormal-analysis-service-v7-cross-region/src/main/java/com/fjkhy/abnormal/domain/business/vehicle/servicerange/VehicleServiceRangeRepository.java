package com.fjkhy.abnormal.domain.business.vehicle.servicerange;

import com.fjkhy.abnormal.common.geo.CoordinateTransformService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * 轻量 JDBC 访问层。
 *
 * 保持“默认内存演示模式”可直接启动，仅在 khy.abnormal.datasource.url 配置存在时启用连接池与查询。
 */
@Repository
public class VehicleServiceRangeRepository {
    private final JdbcTemplate jdbcTemplate;
    private final CoordinateTransformService coordinateTransformService;

    public VehicleServiceRangeRepository(@Qualifier("khyJdbcTemplate") ObjectProvider<JdbcTemplate> jdbcTemplateProvider,
                                         CoordinateTransformService coordinateTransformService) {
        this.jdbcTemplate = jdbcTemplateProvider.getIfAvailable();
        this.coordinateTransformService = coordinateTransformService;
    }

    public boolean configured() {
        return jdbcTemplate != null;
    }

    private JdbcTemplate jdbc() {
        if (jdbcTemplate == null) {
            throw new IllegalStateException("未配置 khy.abnormal.datasource.url");
        }
        return jdbcTemplate;
    }

    public List<VehicleKey> findCandidateVehicles(int limit) {
        String sql = """
                SELECT DISTINCT vehicle_id, plate_no
                FROM v_khy_algo_vehicle_line_station
                WHERE vehicle_id IS NOT NULL
                ORDER BY vehicle_id
                LIMIT ?
                """;
        try {
            return jdbc().query(sql, (rs, rowNum) -> new VehicleKey(rs.getLong("vehicle_id"), rs.getString("plate_no")), limit);
        } catch (DataAccessException ex) {
            throw new IllegalStateException("查询候选车辆失败，请确认 v_khy_algo_vehicle_line_station 视图已建立：" + detail(ex), ex);
        }
    }

    public List<LineStationRow> findBoundStations(long vehicleId) {
        String sql = """
                SELECT line_id, line_name, line_county_code, line_start_station_id, line_start_station_name,
                       vehicle_id, plate_no, station_id, station_name, longitude, latitude,
                       station_order, station_type, station_parent_area_code, station_parent_area_name,
                       station_area_code, station_area_name, is_start_station, is_town_start_station
                FROM v_khy_algo_vehicle_line_station
                WHERE vehicle_id = ?
                  AND longitude IS NOT NULL
                  AND latitude IS NOT NULL
                ORDER BY line_id, station_order
                """;
        try {
            return jdbc().query(sql, ps -> ps.setLong(1, vehicleId), (rs, rowNum) -> new LineStationRow(
                    str(rs, "line_id"), rs.getString("line_name"), rs.getString("line_county_code"),
                    str(rs, "line_start_station_id"), rs.getString("line_start_station_name"),
                    rs.getLong("vehicle_id"), rs.getString("plate_no"), str(rs, "station_id"), rs.getString("station_name"),
                    dbl(rs, "longitude"), dbl(rs, "latitude"), intObj(rs, "station_order"), intObj(rs, "station_type"),
                    rs.getString("station_parent_area_code"), rs.getString("station_parent_area_name"),
                    rs.getString("station_area_code"), rs.getString("station_area_name"),
                    bool(rs, "is_start_station"), bool(rs, "is_town_start_station")
            ));
        } catch (DataAccessException ex) {
            throw new IllegalStateException("查询车辆绑定线路站点失败：" + detail(ex), ex);
        }
    }

    public List<StationPoint> findAllStations() {
        String sql = """
                SELECT station_id, station_name, station_type, longitude, latitude,
                       county_code, parent_area_code, parent_area_name, area_code, area_name, station_level
                FROM v_khy_algo_all_station_point
                WHERE longitude IS NOT NULL AND latitude IS NOT NULL
                """;
        try {
            return jdbc().query(sql, (rs, rowNum) -> new StationPoint(str(rs, "station_id"), rs.getString("station_name"), intObj(rs, "station_type"),
                    dbl(rs, "longitude"), dbl(rs, "latitude"), rs.getString("county_code"),
                    rs.getString("parent_area_code"), rs.getString("parent_area_name"),
                    rs.getString("area_code"), rs.getString("area_name"), intObj(rs, "station_level")));
        } catch (DataAccessException ex) {
            throw new IllegalStateException("查询全量站点点位失败，请确认 v_khy_algo_all_station_point 视图已建立：" + detail(ex), ex);
        }
    }

    public List<GpsPoint> findGpsPoints(long vehicleId, LocalDateTime start, LocalDateTime end, String coordinateMode) {
        String sql = """
                SELECT vehicle_id, gps_time, lng, lat, glng, glat, speed, direction
                FROM vehicle_gps_points
                WHERE vehicle_id = ? AND gps_time >= ? AND gps_time < ?
                ORDER BY gps_time
                """;
        try {
            List<GpsPoint> list = jdbc().query(sql, ps -> {
                ps.setLong(1, vehicleId);
                ps.setTimestamp(2, Timestamp.valueOf(start));
                ps.setTimestamp(3, Timestamp.valueOf(end));
            }, (rs, rowNum) -> {
                Double rawLng = dbl(rs, "lng");
                Double rawLat = dbl(rs, "lat");
                Double gcjLng = dbl(rs, "glng");
                Double gcjLat = dbl(rs, "glat");

                CoordinateTransformService.CoordinatePoint calcPoint = coordinateTransformService
                        .toCalculationPoint(coordinateMode, rawLng, rawLat, gcjLng, gcjLat);
                if (calcPoint == null) {
                    return null;
                }

                Timestamp ts = rs.getTimestamp("gps_time");
                return new GpsPoint(rs.getLong("vehicle_id"), ts == null ? null : ts.toLocalDateTime(),
                        calcPoint.lng(), calcPoint.lat(), dbl(rs, "speed"), dbl(rs, "direction"));
            });
            list.removeIf(Objects::isNull);
            return list;
        } catch (DataAccessException ex) {
            throw new IllegalStateException("查询车辆轨迹失败：" + detail(ex), ex);
        }
    }

    public List<PassEvent> findPassEvents(long vehicleId, LocalDateTime start, LocalDateTime end) {
        String sql = """
                SELECT vehicle_id, plate_no, station_id, station_name, station_type, event_time,
                       longitude, latitude, county_code, parent_area_code, parent_area_name, area_code, area_name
                FROM v_khy_algo_vehicle_station_pass
                WHERE vehicle_id = ? AND event_time >= ? AND event_time < ?
                ORDER BY event_time
                """;
        try {
            return jdbc().query(sql, ps -> {
                ps.setLong(1, vehicleId);
                ps.setTimestamp(2, Timestamp.valueOf(start));
                ps.setTimestamp(3, Timestamp.valueOf(end));
            }, (rs, rowNum) -> {
                Timestamp ts = rs.getTimestamp("event_time");
                return new PassEvent(rs.getLong("vehicle_id"), rs.getString("plate_no"), str(rs, "station_id"), rs.getString("station_name"),
                        intObj(rs, "station_type"), ts == null ? null : ts.toLocalDateTime(), dbl(rs, "longitude"), dbl(rs, "latitude"),
                        rs.getString("county_code"), rs.getString("parent_area_code"), rs.getString("parent_area_name"),
                        rs.getString("area_code"), rs.getString("area_name"));
            });
        } catch (DataAccessException ex) {
            throw new IllegalStateException("查询车辆过站事件失败：" + detail(ex), ex);
        }
    }

    private static String detail(DataAccessException ex) {
        Throwable most = ex.getMostSpecificCause();
        String msg = most == null ? ex.getMessage() : most.getMessage();
        return msg == null ? "" : msg;
    }

    private static String str(ResultSet rs, String column) throws SQLException {
        Object v = rs.getObject(column);
        return v == null ? null : String.valueOf(v);
    }
    private static Double dbl(ResultSet rs, String column) throws SQLException {
        Object v = rs.getObject(column);
        if (v == null) return null;
        if (v instanceof BigDecimal bd) return bd.doubleValue();
        if (v instanceof Number n) return n.doubleValue();
        try { return Double.parseDouble(String.valueOf(v)); } catch (Exception e) { return null; }
    }
    private static Integer intObj(ResultSet rs, String column) throws SQLException {
        Object v = rs.getObject(column);
        if (v == null) return null;
        if (v instanceof Number n) return n.intValue();
        try { return Integer.parseInt(String.valueOf(v)); } catch (Exception e) { return null; }
    }
    private static boolean bool(ResultSet rs, String column) throws SQLException {
        Object v = rs.getObject(column);
        if (v == null) return false;
        if (v instanceof Boolean b) return b;
        if (v instanceof Number n) return n.intValue() != 0;
        return "1".equals(String.valueOf(v)) || "true".equalsIgnoreCase(String.valueOf(v));
    }

    public record VehicleKey(long vehicleId, String plateNo) {}
    public record LineStationRow(String lineId, String lineName, String lineCountyCode,
                                 String lineStartStationId, String lineStartStationName,
                                 long vehicleId, String plateNo, String stationId, String stationName,
                                 Double longitude, Double latitude, Integer stationOrder, Integer stationType,
                                 String stationParentAreaCode, String stationParentAreaName,
                                 String stationAreaCode, String stationAreaName,
                                 boolean startStation, boolean townStartStation) {}
    public record StationPoint(String stationId, String stationName, Integer stationType,
                               Double longitude, Double latitude, String countyCode,
                               String parentAreaCode, String parentAreaName,
                               String areaCode, String areaName, Integer stationLevel) {}
    public record GpsPoint(long vehicleId, LocalDateTime gpsTime, Double lng, Double lat, Double speed, Double direction) {}
    public record PassEvent(long vehicleId, String plateNo, String stationId, String stationName, Integer stationType,
                            LocalDateTime eventTime, Double longitude, Double latitude, String countyCode,
                            String parentAreaCode, String parentAreaName, String areaCode, String areaName) {}
}
