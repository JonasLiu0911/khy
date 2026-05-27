package com.fjkhy.abnormal.infrastructure.jdbc;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fjkhy.abnormal.domain.common.ObjectType;
import com.fjkhy.abnormal.domain.flow.*;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class DetectionConfigJdbcRepository {
    private static final TypeReference<List<String>> DEPENDS_ON_LIST = new TypeReference<>() {};

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public DetectionConfigJdbcRepository(@Qualifier("khyJdbcTemplate") ObjectProvider<JdbcTemplate> jdbcTemplateProvider,
                                         ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplateProvider.getIfAvailable();
        this.objectMapper = objectMapper;
    }

    public boolean configured() {
        return jdbcTemplate != null;
    }

    public int countFlows() {
        try {
            Integer count = jdbc().queryForObject("SELECT COUNT(1) FROM detection_flow", Integer.class);
            return count == null ? 0 : count;
        } catch (DataAccessException ex) {
            throw new IllegalStateException("读取流程数量失败：" + detail(ex), ex);
        }
    }

    public List<DetectionFlow> listFlows() {
        try {
            return jdbc().query("SELECT * FROM detection_flow ORDER BY flow_code", this::mapFlow);
        } catch (DataAccessException ex) {
            throw new IllegalStateException("查询流程失败：" + detail(ex), ex);
        }
    }

    public DetectionFlow getFlow(String flowCode) {
        try {
            return jdbc().queryForObject("SELECT * FROM detection_flow WHERE flow_code = ?", this::mapFlow, flowCode);
        } catch (DataAccessException ex) {
            throw new IllegalStateException("查询流程失败：" + detail(ex), ex);
        }
    }

    public void upsertFlow(DetectionFlow flow) {
        String sql = """
                INSERT INTO detection_flow(flow_code, flow_name, object_type, description, enabled, version)
                VALUES (?,?,?,?,?,?)
                ON DUPLICATE KEY UPDATE
                    flow_name = VALUES(flow_name),
                    object_type = VALUES(object_type),
                    description = VALUES(description),
                    enabled = VALUES(enabled),
                    version = VALUES(version)
                """;
        try {
            jdbc().update(sql, flow.getFlowCode(), flow.getFlowName(), enumName(flow.getObjectType()), flow.getDescription(),
                    flow.isEnabled(), flow.getVersion());
        } catch (DataAccessException ex) {
            throw new IllegalStateException("保存流程失败：" + detail(ex), ex);
        }
    }

    public void deleteFlow(String flowCode) {
        try {
            jdbc().update("DELETE FROM detection_flow_node WHERE flow_code = ?", flowCode);
            jdbc().update("DELETE FROM detection_flow WHERE flow_code = ?", flowCode);
        } catch (DataAccessException ex) {
            throw new IllegalStateException("删除流程失败：" + detail(ex), ex);
        }
    }

    public List<DetectionFlowNode> listNodes(String flowCode) {
        try {
            return jdbc().query("SELECT * FROM detection_flow_node WHERE flow_code = ? ORDER BY order_no", this::mapNode, flowCode);
        } catch (DataAccessException ex) {
            throw new IllegalStateException("查询节点失败：" + detail(ex), ex);
        }
    }

    public void upsertNode(DetectionFlowNode node) {
        String sql = """
                INSERT INTO detection_flow_node(
                    flow_code, node_code, node_name, node_type, detector_code, rule_code, order_no,
                    enabled, required, fail_policy, depends_on_json
                ) VALUES (?,?,?,?,?,?,?,?,?,?,?)
                ON DUPLICATE KEY UPDATE
                    node_name = VALUES(node_name),
                    node_type = VALUES(node_type),
                    detector_code = VALUES(detector_code),
                    rule_code = VALUES(rule_code),
                    order_no = VALUES(order_no),
                    enabled = VALUES(enabled),
                    required = VALUES(required),
                    fail_policy = VALUES(fail_policy),
                    depends_on_json = VALUES(depends_on_json)
                """;
        try {
            jdbc().update(sql, node.getFlowCode(), node.getNodeCode(), node.getNodeName(), enumName(node.getNodeType()),
                    node.getDetectorCode(), node.getRuleCode(), node.getOrderNo(), node.isEnabled(), node.isRequired(),
                    enumName(node.getFailPolicy()), toDependsJson(node.getDependsOn()));
        } catch (DataAccessException ex) {
            throw new IllegalStateException("保存节点失败：" + detail(ex), ex);
        }
    }

    public void updateNodeEnabled(String flowCode, String nodeCode, boolean enabled) {
        try {
            jdbc().update("UPDATE detection_flow_node SET enabled = ? WHERE flow_code = ? AND node_code = ?",
                    enabled, flowCode, nodeCode);
        } catch (DataAccessException ex) {
            throw new IllegalStateException("更新节点状态失败：" + detail(ex), ex);
        }
    }

    public void updateNodeOrder(String flowCode, String nodeCode, int orderNo) {
        try {
            jdbc().update("UPDATE detection_flow_node SET order_no = ? WHERE flow_code = ? AND node_code = ?",
                    orderNo, flowCode, nodeCode);
        } catch (DataAccessException ex) {
            throw new IllegalStateException("更新节点顺序失败：" + detail(ex), ex);
        }
    }

    public void deleteNode(String flowCode, String nodeCode) {
        try {
            jdbc().update("DELETE FROM detection_flow_node WHERE flow_code = ? AND node_code = ?", flowCode, nodeCode);
        } catch (DataAccessException ex) {
            throw new IllegalStateException("删除节点失败：" + detail(ex), ex);
        }
    }

    public List<DetectionThreshold> listThresholds() {
        try {
            return jdbc().query("SELECT * FROM detection_threshold ORDER BY threshold_code", this::mapThreshold);
        } catch (DataAccessException ex) {
            throw new IllegalStateException("查询阈值失败：" + detail(ex), ex);
        }
    }

    public void upsertThreshold(DetectionThreshold threshold) {
        String sql = """
                INSERT INTO detection_threshold(threshold_code, threshold_name, group_name, threshold_value, unit, description)
                VALUES (?,?,?,?,?,?)
                ON DUPLICATE KEY UPDATE
                    threshold_name = VALUES(threshold_name),
                    group_name = VALUES(group_name),
                    threshold_value = VALUES(threshold_value),
                    unit = VALUES(unit),
                    description = VALUES(description)
                """;
        try {
            jdbc().update(sql, threshold.getThresholdCode(), threshold.getThresholdName(), threshold.getGroupName(),
                    threshold.getValue(), threshold.getUnit(), threshold.getDescription());
        } catch (DataAccessException ex) {
            throw new IllegalStateException("保存阈值失败：" + detail(ex), ex);
        }
    }

    public void deleteThreshold(String thresholdCode) {
        try {
            jdbc().update("DELETE FROM detection_threshold WHERE threshold_code = ?", thresholdCode);
        } catch (DataAccessException ex) {
            throw new IllegalStateException("删除阈值失败：" + detail(ex), ex);
        }
    }

    public List<DetectionRuleView> listRules() {
        try {
            return jdbc().query("SELECT * FROM detection_rule ORDER BY rule_code", this::mapRule);
        } catch (DataAccessException ex) {
            throw new IllegalStateException("查询规则失败：" + detail(ex), ex);
        }
    }

    public void upsertRule(DetectionRuleView rule) {
        String sql = """
                INSERT INTO detection_rule(rule_code, rule_name, object_type, abnormal_category, abnormal_subtype,
                    detector_code, enabled, version, data_source)
                VALUES (?,?,?,?,?,?,?,?,?)
                ON DUPLICATE KEY UPDATE
                    rule_name = VALUES(rule_name),
                    object_type = VALUES(object_type),
                    abnormal_category = VALUES(abnormal_category),
                    abnormal_subtype = VALUES(abnormal_subtype),
                    detector_code = VALUES(detector_code),
                    enabled = VALUES(enabled),
                    version = VALUES(version),
                    data_source = VALUES(data_source)
                """;
        try {
            jdbc().update(sql, rule.ruleCode(), rule.ruleName(), rule.objectType(), rule.abnormalCategory(),
                    rule.abnormalSubtype(), rule.detectorCode(), rule.enabled(), rule.version(), rule.dataSource());
        } catch (DataAccessException ex) {
            throw new IllegalStateException("保存规则失败：" + detail(ex), ex);
        }
    }

    public void deleteRule(String ruleCode) {
        try {
            jdbc().update("DELETE FROM detection_rule WHERE rule_code = ?", ruleCode);
        } catch (DataAccessException ex) {
            throw new IllegalStateException("删除规则失败：" + detail(ex), ex);
        }
    }

    private DetectionFlow mapFlow(ResultSet rs, int rowNum) throws SQLException {
        DetectionFlow flow = new DetectionFlow();
        flow.setFlowCode(rs.getString("flow_code"));
        flow.setFlowName(rs.getString("flow_name"));
        flow.setObjectType(enumValue(ObjectType.class, rs.getString("object_type")));
        flow.setDescription(rs.getString("description"));
        flow.setEnabled(rs.getBoolean("enabled"));
        flow.setVersion(rs.getString("version"));
        return flow;
    }

    private DetectionFlowNode mapNode(ResultSet rs, int rowNum) throws SQLException {
        DetectionFlowNode node = new DetectionFlowNode();
        node.setFlowCode(rs.getString("flow_code"));
        node.setNodeCode(rs.getString("node_code"));
        node.setNodeName(rs.getString("node_name"));
        node.setNodeType(enumValue(DetectionFlowNodeType.class, rs.getString("node_type")));
        node.setDetectorCode(rs.getString("detector_code"));
        node.setRuleCode(rs.getString("rule_code"));
        node.setOrderNo(rs.getInt("order_no"));
        node.setEnabled(rs.getBoolean("enabled"));
        node.setRequired(rs.getBoolean("required"));
        node.setFailPolicy(enumValue(DetectionFailPolicy.class, rs.getString("fail_policy")));
        node.setDependsOn(fromDependsJson(rs.getString("depends_on_json")));
        return node;
    }

    private DetectionThreshold mapThreshold(ResultSet rs, int rowNum) throws SQLException {
        DetectionThreshold threshold = new DetectionThreshold();
        threshold.setThresholdCode(rs.getString("threshold_code"));
        threshold.setThresholdName(rs.getString("threshold_name"));
        threshold.setGroupName(rs.getString("group_name"));
        threshold.setValue(rs.getString("threshold_value"));
        threshold.setUnit(rs.getString("unit"));
        threshold.setDescription(rs.getString("description"));
        return threshold;
    }

    private DetectionRuleView mapRule(ResultSet rs, int rowNum) throws SQLException {
        return new DetectionRuleView(
                rs.getString("rule_code"),
                rs.getString("rule_name"),
                rs.getString("object_type"),
                rs.getString("abnormal_category"),
                rs.getString("abnormal_subtype"),
                rs.getString("detector_code"),
                rs.getBoolean("enabled"),
                rs.getString("version"),
                rs.getString("data_source")
        );
    }

    private String toDependsJson(List<String> dependsOn) {
        if (dependsOn == null || dependsOn.isEmpty()) {
            return "[]";
        }
        try {
            return objectMapper.writeValueAsString(dependsOn);
        } catch (Exception ex) {
            throw new IllegalStateException("序列化节点依赖失败：" + ex.getMessage(), ex);
        }
    }

    private List<String> fromDependsJson(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, DEPENDS_ON_LIST);
        } catch (Exception ex) {
            throw new IllegalStateException("解析节点依赖失败：" + ex.getMessage(), ex);
        }
    }

    private JdbcTemplate jdbc() {
        if (jdbcTemplate == null) {
            throw new IllegalStateException("未配置 khy.abnormal.datasource.url");
        }
        return jdbcTemplate;
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

    private static String detail(DataAccessException ex) {
        Throwable most = ex.getMostSpecificCause();
        String msg = most == null ? ex.getMessage() : most.getMessage();
        return msg == null ? "" : msg;
    }
}

