package com.fjkhy.abnormal.infrastructure.jdbc;

import com.fjkhy.abnormal.domain.common.CalcStatus;
import com.fjkhy.abnormal.domain.common.ObjectType;
import com.fjkhy.abnormal.domain.common.RiskLevel;
import com.fjkhy.abnormal.domain.result.AbnormalResult;
import com.fjkhy.abnormal.domain.result.EvidenceRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@EnabledIfEnvironmentVariable(named = "KHY_DB_IT", matches = "1")
class AbnormalResultJdbcRepositoryIntegrationTest {
    @Autowired
    private AbnormalResultJdbcRepository repository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void crudWorks() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS abnormal_result (
                    abnormal_id VARCHAR(64) NOT NULL PRIMARY KEY,
                    task_id VARCHAR(64),
                    object_type VARCHAR(32),
                    object_id VARCHAR(64),
                    object_name VARCHAR(128),
                    county_name VARCHAR(64),
                    abnormal_category VARCHAR(64),
                    abnormal_subtype VARCHAR(64),
                    rule_code VARCHAR(64),
                    detector_code VARCHAR(64),
                    flow_code VARCHAR(64),
                    node_code VARCHAR(64),
                    period VARCHAR(32),
                    risk_level VARCHAR(32),
                    calc_status VARCHAR(32),
                    evidence_summary TEXT,
                    review_status VARCHAR(32),
                    generated_time DATETIME,
                    evidence_records_json LONGTEXT,
                    INDEX idx_abnormal_object (object_id, period),
                    INDEX idx_abnormal_generated (generated_time)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                """);

        AbnormalResult result = new AbnormalResult();
        result.setAbnormalId("UT-CRUD-1");
        result.setTaskId("UT-TASK");
        result.setObjectType(ObjectType.VEHICLE);
        result.setObjectId("1001");
        result.setObjectName("TestVehicle");
        result.setCountyName("TestCounty");
        result.setAbnormalCategory("CAT");
        result.setAbnormalSubtype("SUB");
        result.setRuleCode("RULE");
        result.setDetectorCode("DET");
        result.setFlowCode("FLOW");
        result.setNodeCode("NODE");
        result.setPeriod("2026-05");
        result.setRiskLevel(RiskLevel.HIGH);
        result.setCalcStatus(CalcStatus.CALCULABLE);
        result.setEvidenceSummary("summary");
        result.setReviewStatus("待复核");
        result.setGeneratedTime(LocalDateTime.now());
        result.setEvidenceRecords(List.of(new EvidenceRecord("EV-1", "UT-CRUD-1", "TYPE", "title", "summary", Map.of("k", "v"), LocalDateTime.now())));

        repository.create(result);
        AbnormalResult loaded = repository.findById("UT-CRUD-1");
        assertNotNull(loaded);
        assertEquals("1001", loaded.getObjectId());
        assertTrue(loaded.getEvidenceRecords().size() > 0);

        loaded.setEvidenceSummary("summary2");
        repository.update(loaded);
        AbnormalResult updated = repository.findById("UT-CRUD-1");
        assertEquals("summary2", updated.getEvidenceSummary());

        List<AbnormalResult> list = repository.list(ObjectType.VEHICLE, "TestCounty", "SUB");
        assertTrue(list.size() >= 1);

        repository.delete("UT-CRUD-1");
    }
}

