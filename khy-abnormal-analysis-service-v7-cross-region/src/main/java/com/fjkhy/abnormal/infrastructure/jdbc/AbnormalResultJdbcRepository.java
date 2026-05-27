package com.fjkhy.abnormal.infrastructure.jdbc;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fjkhy.abnormal.domain.common.CalcStatus;
import com.fjkhy.abnormal.domain.common.ObjectType;
import com.fjkhy.abnormal.domain.common.RiskLevel;
import com.fjkhy.abnormal.domain.result.AbnormalResult;
import com.fjkhy.abnormal.domain.result.EvidenceRecord;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Repository
public class AbnormalResultJdbcRepository {
    private static final TypeReference<List<EvidenceRecord>> EVIDENCE_LIST = new TypeReference<>() {};

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public AbnormalResultJdbcRepository(@Qualifier("khyJdbcTemplate") ObjectProvider<JdbcTemplate> jdbcTemplateProvider,
                                        ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplateProvider.getIfAvailable();
        this.objectMapper = objectMapper;
    }

    public boolean configured() {
        return jdbcTemplate != null;
    }

    public void saveAll(List<AbnormalResult> results) {
        if (results == null || results.isEmpty()) {
            return;
        }
        String sql = """
                INSERT INTO abnormal_result(
                    abnormal_id, task_id, object_type, object_id, object_name, county_name,
                    abnormal_category, abnormal_subtype, rule_code, detector_code, flow_code, node_code,
                    period_, risk_level, calc_status, evidence_summary, review_status, generated_time, evidence_records_json
                ) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
                ON DUPLICATE KEY UPDATE
                    task_id = VALUES(task_id),
                    object_type = VALUES(object_type),
                    object_id = VALUES(object_id),
                    object_name = VALUES(object_name),
                    county_name = VALUES(county_name),
                    abnormal_category = VALUES(abnormal_category),
                    abnormal_subtype = VALUES(abnormal_subtype),
                    rule_code = VALUES(rule_code),
                    detector_code = VALUES(detector_code),
                    flow_code = VALUES(flow_code),
                    node_code = VALUES(node_code),
                    period = VALUES(period),
                    risk_level = VALUES(risk_level),
                    calc_status = VALUES(calc_status),
                    evidence_summary = VALUES(evidence_summary),
                    review_status = VALUES(review_status),
                    generated_time = VALUES(generated_time),
                    evidence_records_json = VALUES(evidence_records_json)
                """;
        List<Object[]> batch = new ArrayList<>();
        for (AbnormalResult r : results) {
            batch.add(toParams(r));
        }
        try {
            jdbc().batchUpdate(sql, batch);
        } catch (DataAccessException ex) {
            throw new IllegalStateException("保存异常结果失败：" + detail(ex), ex);
        }
    }

    public void create(AbnormalResult result) {
        String sql = """
                INSERT INTO abnormal_result(
                    abnormal_id, task_id, object_type, object_id, object_name, county_name,
                    abnormal_category, abnormal_subtype, rule_code, detector_code, flow_code, node_code,
                    period, risk_level, calc_status, evidence_summary, review_status, generated_time, evidence_records_json
                ) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
                """;
        try {
            jdbc().update(sql, toParams(result));
        } catch (DataAccessException ex) {
            throw new IllegalStateException("新增异常结果失败：" + detail(ex), ex);
        }
    }

    public void update(AbnormalResult result) {
        String sql = """
                UPDATE abnormal_result
                SET task_id=?, object_type=?, object_id=?, object_name=?, county_name=?,
                    abnormal_category=?, abnormal_subtype=?, rule_code=?, detector_code=?, flow_code=?, node_code=?,
                    period=?, risk_level=?, calc_status=?, evidence_summary=?, review_status=?, generated_time=?, evidence_records_json=?
                WHERE abnormal_id=?
                """;
        Object[] params = toParams(result);
        Object[] updateParams = new Object[params.length];
        System.arraycopy(params, 1, updateParams, 0, params.length - 1);
        updateParams[params.length - 1] = params[0];
        try {
            jdbc().update(sql, updateParams);
        } catch (DataAccessException ex) {
            throw new IllegalStateException("更新异常结果失败：" + detail(ex), ex);
        }
    }

    public void delete(String abnormalId) {
        try {
            jdbc().update("DELETE FROM abnormal_result WHERE abnormal_id = ?", abnormalId);
        } catch (DataAccessException ex) {
            throw new IllegalStateException("删除异常结果失败：" + detail(ex), ex);
        }
    }

    public List<AbnormalResult> list(ObjectType objectType, String countyName, String subtype) {
        StringBuilder sql = new StringBuilder("SELECT * FROM abnormal_result WHERE 1=1");
        List<Object> params = new ArrayList<>();
        if (objectType != null && objectType != ObjectType.ALL) {
            sql.append(" AND object_type = ?");
            params.add(objectType.name());
        }
        if (countyName != null && !countyName.isBlank()) {
            sql.append(" AND county_name = ?");
            params.add(countyName);
        }
        if (subtype != null && !subtype.isBlank()) {
            sql.append(" AND abnormal_subtype LIKE ?");
            params.add("%" + subtype + "%");
        }
        sql.append(" ORDER BY generated_time DESC");
        try {
            return jdbc().query(sql.toString(), params.toArray(), this::map);
        } catch (DataAccessException ex) {
            throw new IllegalStateException("查询异常结果失败：" + detail(ex), ex);
        }
    }

    public AbnormalResult findById(String abnormalId) {
        try {
            return jdbc().queryForObject("SELECT * FROM abnormal_result WHERE abnormal_id = ?", this::map, abnormalId);
        } catch (DataAccessException ex) {
            throw new IllegalStateException("查询异常结果失败：" + detail(ex), ex);
        }
    }

    public List<AbnormalResult> listByObjectAndPeriod(String objectId, String period) {
        try {
            return jdbc().query("SELECT * FROM abnormal_result WHERE object_id = ? AND period_ = ? ORDER BY generated_time DESC",
                    this::map, objectId, period);
        } catch (DataAccessException ex) {
            throw new IllegalStateException("查询异常证据失败：" + detail(ex), ex);
        }
    }

    private JdbcTemplate jdbc() {
        if (jdbcTemplate == null) {
            throw new IllegalStateException("未配置 khy.abnormal.datasource.url");
        }
        return jdbcTemplate;
    }

    private Object[] toParams(AbnormalResult r) {
        return new Object[] {
                r.getAbnormalId(), r.getTaskId(), enumName(r.getObjectType()), r.getObjectId(), r.getObjectName(), r.getCountyName(),
                r.getAbnormalCategory(), r.getAbnormalSubtype(), r.getRuleCode(), r.getDetectorCode(), r.getFlowCode(), r.getNodeCode(),
                r.getPeriod(), enumName(r.getRiskLevel()), enumName(r.getCalcStatus()), r.getEvidenceSummary(), r.getReviewStatus(),
                toTimestamp(r.getGeneratedTime()), toEvidenceJson(r.getEvidenceRecords())
        };
    }

    private AbnormalResult map(ResultSet rs, int rowNum) throws SQLException {
        AbnormalResult r = new AbnormalResult();
        r.setAbnormalId(rs.getString("abnormal_id"));
        r.setTaskId(rs.getString("task_id"));
        r.setObjectType(enumValue(ObjectType.class, rs.getString("object_type")));
        r.setObjectId(rs.getString("object_id"));
        r.setObjectName(rs.getString("object_name"));
        r.setCountyName(rs.getString("county_name"));
        r.setAbnormalCategory(rs.getString("abnormal_category"));
        r.setAbnormalSubtype(rs.getString("abnormal_subtype"));
        r.setRuleCode(rs.getString("rule_code"));
        r.setDetectorCode(rs.getString("detector_code"));
        r.setFlowCode(rs.getString("flow_code"));
        r.setNodeCode(rs.getString("node_code"));
        r.setPeriod(rs.getString("period"));
        r.setRiskLevel(enumValue(RiskLevel.class, rs.getString("risk_level")));
        r.setCalcStatus(enumValue(CalcStatus.class, rs.getString("calc_status")));
        r.setEvidenceSummary(rs.getString("evidence_summary"));
        r.setReviewStatus(rs.getString("review_status"));
        Timestamp ts = rs.getTimestamp("generated_time");
        r.setGeneratedTime(ts == null ? null : ts.toLocalDateTime());
        r.setEvidenceRecords(fromEvidenceJson(rs.getString("evidence_records_json")));
        return r;
    }

    private String toEvidenceJson(List<EvidenceRecord> records) {
        if (records == null || records.isEmpty()) {
            return "[]";
        }
        try {
            return objectMapper.writeValueAsString(records);
        } catch (Exception ex) {
            throw new IllegalStateException("序列化证据失败：" + ex.getMessage(), ex);
        }
    }

    private List<EvidenceRecord> fromEvidenceJson(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            List<EvidenceRecord> list = objectMapper.readValue(json, EVIDENCE_LIST);
            list.removeIf(Objects::isNull);
            return list;
        } catch (Exception ex) {
            throw new IllegalStateException("解析证据失败：" + ex.getMessage(), ex);
        }
    }

    private static <T extends Enum<T>> T enumValue(Class<T> type, String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Enum.valueOf(type, value);
    }

    private static String enumName(Enum<?> value) {
        return value == null ? null : value.name();
    }

    private static Timestamp toTimestamp(LocalDateTime time) {
        return time == null ? null : Timestamp.valueOf(time);
    }

    private static String detail(DataAccessException ex) {
        Throwable most = ex.getMostSpecificCause();
        String msg = most == null ? ex.getMessage() : most.getMessage();
        return msg == null ? "" : msg;
    }
}

