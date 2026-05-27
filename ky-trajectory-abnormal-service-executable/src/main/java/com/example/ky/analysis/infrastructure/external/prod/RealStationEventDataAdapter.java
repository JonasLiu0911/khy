package com.example.ky.analysis.infrastructure.external.prod;

import com.example.ky.analysis.domain.station.model.StationEvent;
import com.example.ky.analysis.domain.station.port.StationEventDataPort;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Component
@Profile("prod")
public class RealStationEventDataAdapter implements StationEventDataPort {
    private final JdbcTemplate jdbcTemplate;

    public RealStationEventDataAdapter(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<StationEvent> queryStationEvents(String vehicleId, LocalDateTime startTime, LocalDateTime endTime) {
        String sql = "select vehicle_id, station_id, event_time, source_system "
                + "from v_khy_algo_vehicle_station_pass where vehicle_id = ? and event_time between ? and ? "
                + "order by event_time";
        return jdbcTemplate.query(sql, (rs, rowNum) -> new StationEvent(
                rs.getString("vehicle_id"),
                rs.getString("station_id"),
                rs.getTimestamp("event_time").toLocalDateTime(),
                rs.getString("source_system")
        ), vehicleId, Timestamp.valueOf(startTime), Timestamp.valueOf(endTime));
    }
}
