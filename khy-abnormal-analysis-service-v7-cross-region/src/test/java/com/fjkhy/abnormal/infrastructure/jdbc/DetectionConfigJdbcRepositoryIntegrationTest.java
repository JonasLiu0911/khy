package com.fjkhy.abnormal.infrastructure.jdbc;

import com.fjkhy.abnormal.domain.common.ObjectType;
import com.fjkhy.abnormal.domain.flow.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@EnabledIfEnvironmentVariable(named = "KHY_DB_IT", matches = "1")
class DetectionConfigJdbcRepositoryIntegrationTest {
    @Autowired
    private DetectionConfigJdbcRepository repository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void crudWorks() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS detection_flow (
                    flow_code VARCHAR(64) NOT NULL PRIMARY KEY,
                    flow_name VARCHAR(128) NOT NULL,
                    object_type VARCHAR(32),
                    description TEXT,
                    enabled TINYINT(1) NOT NULL DEFAULT 1,
                    version VARCHAR(32)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                """);
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS detection_flow_node (
                    flow_code VARCHAR(64) NOT NULL,
                    node_code VARCHAR(64) NOT NULL,
                    node_name VARCHAR(128) NOT NULL,
                    node_type VARCHAR(64),
                    detector_code VARCHAR(64),
                    rule_code VARCHAR(64),
                    order_no INT NOT NULL,
                    enabled TINYINT(1) NOT NULL DEFAULT 1,
                    required TINYINT(1) NOT NULL DEFAULT 1,
                    fail_policy VARCHAR(32),
                    depends_on_json LONGTEXT,
                    PRIMARY KEY (flow_code, node_code)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                """);
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS detection_threshold (
                    threshold_code VARCHAR(64) NOT NULL PRIMARY KEY,
                    threshold_name VARCHAR(128) NOT NULL,
                    group_name VARCHAR(128),
                    threshold_value VARCHAR(64),
                    unit VARCHAR(32),
                    description TEXT
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                """);
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS detection_rule (
                    rule_code VARCHAR(64) NOT NULL PRIMARY KEY,
                    rule_name VARCHAR(128) NOT NULL,
                    object_type VARCHAR(32),
                    abnormal_category VARCHAR(64),
                    abnormal_subtype VARCHAR(64),
                    detector_code VARCHAR(64),
                    enabled TINYINT(1) NOT NULL DEFAULT 1,
                    version VARCHAR(32),
                    data_source VARCHAR(128)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                """);

        DetectionFlow flow = new DetectionFlow("UT_FLOW", "UT Flow", ObjectType.VEHICLE, "desc", true, "V1");
        repository.upsertFlow(flow);
        assertEquals("UT_FLOW", repository.getFlow("UT_FLOW").getFlowCode());

        DetectionFlowNode node = new DetectionFlowNode("UT_FLOW", "NODE1", "Node1", DetectionFlowNodeType.BUSINESS_DETECTION,
                "DET", "RULE", 1, true, true, DetectionFailPolicy.CONTINUE);
        node.setDependsOn(List.of("A", "B"));
        repository.upsertNode(node);
        assertEquals(1, repository.listNodes("UT_FLOW").size());

        repository.updateNodeEnabled("UT_FLOW", "NODE1", false);
        repository.updateNodeOrder("UT_FLOW", "NODE1", 2);

        DetectionThreshold threshold = new DetectionThreshold("TH1", "阈值1", "组", "10", "个", "desc");
        repository.upsertThreshold(threshold);
        assertTrue(repository.listThresholds().size() >= 1);

        DetectionRuleView rule = new DetectionRuleView("RULE1", "规则1", "VEHICLE", "CAT", "SUB", "DET", true, "V1", "SRC");
        repository.upsertRule(rule);
        assertTrue(repository.listRules().size() >= 1);

        repository.deleteRule("RULE1");
        repository.deleteThreshold("TH1");
        repository.deleteNode("UT_FLOW", "NODE1");
        repository.deleteFlow("UT_FLOW");
    }
}

