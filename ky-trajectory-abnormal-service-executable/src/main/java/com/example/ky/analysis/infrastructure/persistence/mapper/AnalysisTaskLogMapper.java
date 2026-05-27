package com.example.ky.analysis.infrastructure.persistence.mapper;

import com.example.ky.analysis.domain.task.AnalysisTaskLog;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class AnalysisTaskLogMapper {
    public AnalysisTaskLog fromRow(ResultSet rs) throws SQLException {
        AnalysisTaskLog log = new AnalysisTaskLog();
        log.setTaskId(rs.getString("task_id"));
        log.setTaskType(rs.getString("task_type"));
        log.setTriggerType(rs.getString("trigger_type"));
        log.setStatus(rs.getString("status"));
        log.setWindowStart(toLocalDateTime(rs.getTimestamp("window_start")));
        log.setWindowEnd(toLocalDateTime(rs.getTimestamp("window_end")));
        log.setTotalCount(rs.getInt("total_count"));
        log.setSuccessCount(rs.getInt("success_count"));
        log.setFailCount(rs.getInt("fail_count"));
        log.setStartTime(toLocalDateTime(rs.getTimestamp("start_time")));
        log.setEndTime(toLocalDateTime(rs.getTimestamp("end_time")));
        log.setErrorMessage(rs.getString("error_message"));
        return log;
    }

    private java.time.LocalDateTime toLocalDateTime(Timestamp ts) {
        return ts == null ? null : ts.toLocalDateTime();
    }
}
