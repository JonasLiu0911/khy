-- V6 持久化表结构建议。当前演示工程默认使用内存存储，真实库接入时可参考本脚本。
CREATE TABLE khy_detection_flow (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    flow_code VARCHAR(64) NOT NULL UNIQUE,
    flow_name VARCHAR(128) NOT NULL,
    object_type VARCHAR(32),
    description TEXT,
    enabled TINYINT DEFAULT 1,
    version VARCHAR(32),
    created_time DATETIME,
    updated_time DATETIME
);

CREATE TABLE khy_detection_flow_node (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    flow_code VARCHAR(64) NOT NULL,
    node_code VARCHAR(64) NOT NULL,
    node_name VARCHAR(128) NOT NULL,
    node_type VARCHAR(64) NOT NULL,
    detector_code VARCHAR(64),
    rule_code VARCHAR(64),
    order_no INT NOT NULL,
    enabled TINYINT DEFAULT 1,
    required_flag TINYINT DEFAULT 1,
    fail_policy VARCHAR(32) DEFAULT 'CONTINUE',
    depends_on VARCHAR(512),
    condition_expr VARCHAR(512),
    created_time DATETIME,
    updated_time DATETIME
);

CREATE TABLE khy_abnormal_result (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    abnormal_id VARCHAR(64) NOT NULL UNIQUE,
    task_id VARCHAR(64),
    flow_code VARCHAR(64),
    node_code VARCHAR(64),
    detector_code VARCHAR(64),
    rule_code VARCHAR(64),
    object_type VARCHAR(32),
    object_id VARCHAR(64),
    object_name VARCHAR(255),
    county_name VARCHAR(64),
    abnormal_category VARCHAR(128),
    abnormal_subtype VARCHAR(128),
    period VARCHAR(32),
    risk_level VARCHAR(32),
    calc_status VARCHAR(32),
    evidence_summary TEXT,
    review_status VARCHAR(32),
    generated_time DATETIME
);

CREATE TABLE khy_detection_step_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    task_id VARCHAR(64) NOT NULL,
    flow_code VARCHAR(64) NOT NULL,
    node_code VARCHAR(64) NOT NULL,
    detector_code VARCHAR(64),
    start_time DATETIME,
    end_time DATETIME,
    status VARCHAR(32),
    processed_count INT,
    abnormal_count INT,
    skipped_count INT,
    error_message TEXT,
    created_time DATETIME
);
