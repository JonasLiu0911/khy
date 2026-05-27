package com.example.ky.analysis.infrastructure.persistence.repository;

import com.example.ky.analysis.domain.common.AbnormalLevel;
import com.example.ky.analysis.domain.result.model.VehicleAbnormalResult;
import com.example.ky.analysis.infrastructure.persistence.mapper.VehicleAbnormalResultMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class VehicleAbnormalResultJpaRepository {
    private final JdbcTemplate jdbcTemplate;
    private final VehicleAbnormalResultMapper mapper;

    public VehicleAbnormalResultJpaRepository(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.mapper = new VehicleAbnormalResultMapper(objectMapper);
    }

    public void save(VehicleAbnormalResult result) {
        Optional<Long> id = findId(result.getVehicleId(), result.getStatDate());
        if (id.isPresent()) {
            update(id.get(), result);
            return;
        }
        insert(generateId(), result);
    }

    public void batchSave(List<VehicleAbnormalResult> results) {
        for (VehicleAbnormalResult result : results) {
            save(result);
        }
    }

    public List<VehicleAbnormalResult> query(Date statDate, AbnormalLevel level) {
        StringBuilder sql = new StringBuilder("select * from vehicle_abnormal_daily_result where 1=1");
        List<Object> args = new ArrayList<>();
        if (statDate != null) {
            sql.append(" and stat_date = ?");
            args.add(statDate);
        }
        if (level != null) {
            sql.append(" and abnormal_level = ?");
            args.add(level.name());
        }
        sql.append(" order by final_score");
        return jdbcTemplate.query(sql.toString(), args.toArray(), (rs, rowNum) -> mapper.fromRow(rs));
    }

    public Optional<VehicleAbnormalResult> findByVehicleAndDate(String vehicleId, Date statDate) {
        String sql = "select * from vehicle_abnormal_daily_result where vehicle_id = ? and stat_date = ? limit 1";
        List<VehicleAbnormalResult> result = jdbcTemplate.query(sql, (rs, rowNum) -> mapper.fromRow(rs), vehicleId, statDate);
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    public List<VehicleAbnormalResult> findByLevelAndDate(AbnormalLevel abnormalLevel, Date statDate) {
        String sql = "select * from vehicle_abnormal_daily_result where abnormal_level = ? and stat_date = ?";
        return jdbcTemplate.query(sql, (rs, rowNum) -> mapper.fromRow(rs), abnormalLevel.name(), statDate);
    }

    private Optional<Long> findId(String vehicleId, java.time.LocalDate statDate) {
        String sql = "select id from vehicle_abnormal_daily_result where vehicle_id = ? and stat_date = ? limit 1";
        List<Long> result = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getLong("id"), vehicleId, Date.valueOf(statDate));
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    private void insert(long id, VehicleAbnormalResult result) {
        String sql = "insert into vehicle_abnormal_daily_result (id, stat_date, window_start, window_end, vehicle_id, plate_no, "
                + "line_id, line_name, track_score, event_score, final_score, abnormal_level, conflict_flag, evidence_summary, "
                + "evidence_json, suggestion, algorithm_version) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql,
                id,
                Date.valueOf(result.getStatDate()),
                Timestamp.valueOf(result.getWindowStart()),
                Timestamp.valueOf(result.getWindowEnd()),
                result.getVehicleId(),
                result.getPlateNo(),
                result.getLineId(),
                result.getLineName(),
                result.getTrackScore(),
                result.getEventScore(),
                result.getFinalScore(),
                result.getAbnormalLevel() == null ? null : result.getAbnormalLevel().name(),
                result.isConflictFlag(),
                result.getEvidenceSummary(),
                mapper.toEvidenceJson(result),
                result.getSuggestion(),
                result.getAlgorithmVersion()
        );
    }

    private void update(long id, VehicleAbnormalResult result) {
        String sql = "update vehicle_abnormal_daily_result set window_start = ?, window_end = ?, plate_no = ?, line_id = ?, "
                + "line_name = ?, track_score = ?, event_score = ?, final_score = ?, abnormal_level = ?, conflict_flag = ?, "
                + "evidence_summary = ?, evidence_json = ?, suggestion = ?, algorithm_version = ? where id = ?";
        jdbcTemplate.update(sql,
                Timestamp.valueOf(result.getWindowStart()),
                Timestamp.valueOf(result.getWindowEnd()),
                result.getPlateNo(),
                result.getLineId(),
                result.getLineName(),
                result.getTrackScore(),
                result.getEventScore(),
                result.getFinalScore(),
                result.getAbnormalLevel() == null ? null : result.getAbnormalLevel().name(),
                result.isConflictFlag(),
                result.getEvidenceSummary(),
                mapper.toEvidenceJson(result),
                result.getSuggestion(),
                result.getAlgorithmVersion(),
                id
        );
    }

    private long generateId() {
        return System.currentTimeMillis();
    }
}
