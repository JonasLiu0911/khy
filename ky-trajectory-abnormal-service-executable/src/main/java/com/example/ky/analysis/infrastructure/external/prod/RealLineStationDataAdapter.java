package com.example.ky.analysis.infrastructure.external.prod;

import com.example.ky.analysis.domain.line.model.LineStation;
import com.example.ky.analysis.domain.line.port.LineStationDataPort;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Profile("prod")
public class RealLineStationDataAdapter implements LineStationDataPort {
    private final JdbcTemplate jdbcTemplate;

    public RealLineStationDataAdapter(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<LineStation> queryLineStationsForAlg(String lineId) {
        String sql = "select line_id, line_name, station_id, station_name, longitude, latitude, "
                + "station_sort, key_station from v_khy_algo_vehicle_line_station "
                + "where line_id = ? and station_type in (1,5) order by station_sort";
        return jdbcTemplate.query(sql, (rs, rowNum) -> new LineStation(
                rs.getString("line_id"),
                rs.getString("line_name"),
                rs.getString("station_id"),
                rs.getString("station_name"),
                rs.getDouble("longitude"),
                rs.getDouble("latitude"),
                rs.getInt("station_sort"),
                rs.getBoolean("key_station")
        ), lineId);
    }

    @Override
    public List<LineStation> queryLineStationsForVisualize(String lineId) {
        String sql = "select line_id, line_name, station_id, station_name, longitude, latitude, "
                + "station_sort, key_station from v_khy_algo_vehicle_line_station "
                + "where line_id = ? order by station_sort";
        return jdbcTemplate.query(sql, (rs, rowNum) -> new LineStation(
                rs.getString("line_id"),
                rs.getString("line_name"),
                rs.getString("station_id"),
                rs.getString("station_name"),
                rs.getDouble("longitude"),
                rs.getDouble("latitude"),
                rs.getInt("station_sort"),
                rs.getBoolean("key_station")
        ), lineId);
    }
}
