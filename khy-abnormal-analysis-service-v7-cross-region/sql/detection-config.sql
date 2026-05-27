-- Detection flow config storage (MySQL 8+)
CREATE TABLE IF NOT EXISTS detection_flow (
    flow_code VARCHAR(64) NOT NULL PRIMARY KEY,
    flow_name VARCHAR(128) NOT NULL,
    object_type VARCHAR(32),
    description TEXT,
    enabled TINYINT(1) NOT NULL DEFAULT 1,
    version VARCHAR(32)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

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
    PRIMARY KEY (flow_code, node_code),
    INDEX idx_flow_node_order (flow_code, order_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS detection_threshold (
    threshold_code VARCHAR(64) NOT NULL PRIMARY KEY,
    threshold_name VARCHAR(128) NOT NULL,
    group_name VARCHAR(128),
    threshold_value VARCHAR(64),
    unit VARCHAR(32),
    description TEXT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

