-- Abnormal result storage (MySQL 8+)
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

