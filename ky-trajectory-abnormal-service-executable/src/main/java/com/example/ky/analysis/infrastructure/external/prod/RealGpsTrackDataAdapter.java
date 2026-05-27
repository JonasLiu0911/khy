package com.example.ky.analysis.infrastructure.external.prod;

import com.example.ky.analysis.domain.vehicle.model.GpsPoint;
import com.example.ky.analysis.domain.vehicle.port.GpsTrackDataPort;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Component
@Profile("prod")
public class RealGpsTrackDataAdapter implements GpsTrackDataPort {
    private final JdbcTemplate jdbcTemplate;

    public RealGpsTrackDataAdapter(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<GpsPoint> queryGpsPoints(String vehicleId, LocalDateTime startTime, LocalDateTime endTime) {
        String sql = "select vehicle_id, gps_time, lng, lat, glng, glat, speed "
                + "from vehicle_gps_points_511 where vehicle_id = ? and gps_time between ? and ? "
                + "and HOUR(gps_time) >= 6 and HOUR(gps_time) < 22 "
                + "order by gps_time";
        return jdbcTemplate.query(sql, (rs, rowNum) -> new GpsPoint(
                rs.getString("vehicle_id"),
                rs.getTimestamp("gps_time").toLocalDateTime(),
                rs.getDouble("lng"),
                rs.getDouble("lat"),
                rs.getDouble("glng"),
                rs.getDouble("glat"),
                rs.getDouble("speed")
        ), vehicleId, Timestamp.valueOf(startTime), Timestamp.valueOf(endTime));
    }
}
