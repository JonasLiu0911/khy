-- 结果表草案：真实落库时可按此结构扩展
create table vehicle_abnormal_daily_result (
    id bigint primary key,
    stat_date date not null,
    window_start timestamp not null,
    window_end timestamp not null,
    vehicle_id varchar(64) not null,
    plate_no varchar(64),
    line_id varchar(64),
    line_name varchar(255),
    track_score numeric(8,2),
    event_score numeric(8,2),
    final_score numeric(8,2),
    abnormal_level varchar(32),
    conflict_flag boolean,
    evidence_summary text,
    evidence_json text,
    suggestion text,
    algorithm_version varchar(64),
    create_time timestamp default current_timestamp
);

create table analysis_task_log (
    task_id varchar(64) primary key,
    task_type varchar(64),
    trigger_type varchar(32),
    status varchar(32),
    window_start timestamp,
    window_end timestamp,
    total_count int,
    success_count int,
    fail_count int,
    start_time timestamp,
    end_time timestamp,
    error_message text
);
