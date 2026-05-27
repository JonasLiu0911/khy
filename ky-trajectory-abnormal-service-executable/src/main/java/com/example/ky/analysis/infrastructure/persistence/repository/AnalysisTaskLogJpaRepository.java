package com.example.ky.analysis.infrastructure.persistence.repository;

import com.example.ky.analysis.domain.task.AnalysisTaskLog;
import com.example.ky.analysis.infrastructure.persistence.mapper.AnalysisTaskLogMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

@Repository
public class AnalysisTaskLogJpaRepository {
    private final JdbcTemplate jdbcTemplate;
    private final AnalysisTaskLogMapper mapper = new AnalysisTaskLogMapper();

    public AnalysisTaskLogJpaRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void save(AnalysisTaskLog log) {
        String sql = "insert into analysis_task_log (task_id, task_type, trigger_type, status, window_start, window_end, "
                + "total_count, success_count, fail_count, start_time, end_time, error_message) "
                + "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql,
                log.getTaskId(),
                log.getTaskType(),
                log.getTriggerType(),
                log.getStatus(),
                toTimestamp(log.getWindowStart()),
                toTimestamp(log.getWindowEnd()),
                log.getTotalCount(),
                log.getSuccessCount(),
                log.getFailCount(),
                toTimestamp(log.getStartTime()),
                toTimestamp(log.getEndTime()),
                log.getErrorMessage()
        );
    }

    public void update(AnalysisTaskLog log) {
        String sql = "update analysis_task_log set task_type = ?, trigger_type = ?, status = ?, window_start = ?, window_end = ?, "
                + "total_count = ?, success_count = ?, fail_count = ?, start_time = ?, end_time = ?, error_message = ? "
                + "where task_id = ?";
        jdbcTemplate.update(sql,
                log.getTaskType(),
                log.getTriggerType(),
                log.getStatus(),
                toTimestamp(log.getWindowStart()),
                toTimestamp(log.getWindowEnd()),
                log.getTotalCount(),
                log.getSuccessCount(),
                log.getFailCount(),
                toTimestamp(log.getStartTime()),
                toTimestamp(log.getEndTime()),
                log.getErrorMessage(),
                log.getTaskId()
        );
    }

    public List<AnalysisTaskLog> queryAll() {
        String sql = "select task_id, task_type, trigger_type, status, window_start, window_end, total_count, success_count, "
                + "fail_count, start_time, end_time, error_message from analysis_task_log order by start_time desc";
        return jdbcTemplate.query(sql, (rs, rowNum) -> mapper.fromRow(rs));
    }

    private Timestamp toTimestamp(java.time.LocalDateTime time) {
        return time == null ? null : Timestamp.valueOf(time);
    }
}
