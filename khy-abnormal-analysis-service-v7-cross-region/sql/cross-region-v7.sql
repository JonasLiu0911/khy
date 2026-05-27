-- V7 车辆疑似线路服务范围外运行/跨乡镇跨县辅助检测所需视图与索引
-- 说明：当前未接入乡镇、区县行政边界 polygon，本脚本支撑基于线路站点邻近关系的辅助判断。

-- 1. 车辆绑定线路及线路站点序列增强视图
CREATE OR REPLACE VIEW v_khy_algo_vehicle_line_station AS
SELECT
    lrs.line_id,
    lrs.line_name,

    kl.county_code AS line_county_code,
    kl.start_station_id AS line_start_station_id,
    kl.start_station_name AS line_start_station_name,

    lrc.car_id AS vehicle_id,
    c.license_plate_num AS plate_no,
    lrc.create_time AS bind_start_date,
    NULL AS bind_end_date,

    lrs.station_id AS station_id,
    lrs.station_name AS station_name,

    CASE
        WHEN lrs.station_type = 5 THEN es.longitude
        ELSE ss.lon
    END AS longitude,

    CASE
        WHEN lrs.station_type = 5 THEN es.latitude
        ELSE ss.lat
    END AS latitude,

    lrs.sort AS station_sort,
    lrs.station_type,

    lrs.parent_area_code AS station_parent_area_code,
    lrs.parent_area_name AS station_parent_area_name,
    lrs.area_code AS station_area_code,
    lrs.area_name AS station_area_name,

    CASE
        WHEN kl.start_station_id = lrs.station_id THEN 1
        ELSE 0
    END AS is_start_station,

    ss.level AS service_station_level,

    CASE
        WHEN kl.start_station_id = lrs.station_id AND ss.level = 1 THEN 1
        ELSE 0
    END AS is_town_start_station,

    NULL AS key_station
FROM khy_line_relate_station lrs
JOIN khy_line_relate_car lrc ON lrc.line_id = lrs.line_id
JOIN khy_car c ON lrc.car_id = c.id
LEFT JOIN khy_line kl ON kl.id = lrs.line_id
LEFT JOIN khy_service_station ss ON ss.id = lrs.station_id
LEFT JOIN khy_exemption_station es ON es.id = lrs.station_id
WHERE
    (lrs.deleted = 0 OR lrs.deleted IS NULL)
    AND lrs.station_id IS NOT NULL;

-- 2. 全量站点点位视图，用于识别车辆是否接近非绑定线路站点
CREATE OR REPLACE VIEW v_khy_algo_all_station_point AS
SELECT
    ss.id AS station_id,
    ss.name AS station_name,
    2 AS station_type,
    ss.lon AS longitude,
    ss.lat AS latitude,
    ss.county_code AS county_code,
    ss.parent_area_code AS parent_area_code,
    ss.parent_area_name AS parent_area_name,
    ss.area_code AS area_code,
    ss.area_name AS area_name,
    ss.level AS station_level
FROM khy_service_station ss
WHERE ss.id IS NOT NULL
  AND ss.lon IS NOT NULL
  AND ss.lat IS NOT NULL
UNION ALL
SELECT
    es.id AS station_id,
    es.stop_station_name AS station_name,
    5 AS station_type,
    es.longitude AS longitude,
    es.latitude AS latitude,
    NULL AS county_code,
    NULL AS parent_area_code,
    NULL AS parent_area_name,
    NULL AS area_code,
    NULL AS area_name,
    NULL AS station_level
FROM khy_exemption_station es
WHERE es.id IS NOT NULL
  AND es.longitude IS NOT NULL
  AND es.latitude IS NOT NULL;

-- 3. 车辆实际过站事件增强视图
CREATE OR REPLACE VIEW v_khy_algo_vehicle_station_pass AS
SELECT
    c.id AS vehicle_id,
    syso.car_plate AS plate_no,
    syso.station_id,
    COALESCE(ss.station_name, es.station_name) AS station_name,
    syso.station_type,
    syso.start_time AS event_time,
    CASE
        WHEN syso.station_type = 5 THEN es.longitude
        ELSE ss.lon
    END AS longitude,
    CASE
        WHEN syso.station_type = 5 THEN es.latitude
        ELSE ss.lat
    END AS latitude,
    ss.county_code AS county_code,
    ss.parent_area_code AS parent_area_code,
    ss.parent_area_name AS parent_area_name,
    ss.area_code AS area_code,
    ss.area_name AS area_name,
    NULL AS source_system
FROM khy_sy_station_operate syso
LEFT JOIN khy_car c ON syso.car_plate = c.license_plate_num
LEFT JOIN khy_service_station ss ON ss.id = syso.station_id
LEFT JOIN khy_exemption_station es ON es.id = syso.station_id
WHERE syso.start_time IS NOT NULL;

-- 4. GPS轨迹表推荐索引
ALTER TABLE vehicle_gps_points
ADD INDEX idx_vehicle_time (vehicle_id, gps_time);

-- 如已存在 idx_vehicle_time，执行 ALTER TABLE 可能报重复索引错误，可忽略或先 SHOW INDEX 检查。
