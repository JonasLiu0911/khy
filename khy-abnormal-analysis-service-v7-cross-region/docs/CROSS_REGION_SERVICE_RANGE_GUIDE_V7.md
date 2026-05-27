# V7.1 车辆跨乡镇/跨县运行辅助检测实现说明

## 1. 功能定位

当前系统尚未接入乡镇、区县行政边界 polygon，因此 V7 不输出确定性“跨乡镇运行”“跨区县运行”结论。V7 按一期可落地边界实现：

> 基于车辆绑定线路站点集合、车辆轨迹、全量站点点位和过站记录，识别“疑似线路服务范围外运行”。

如果车辆轨迹长期偏离其所有绑定线路服务站点和简化服务走廊，并且接近非绑定线路站点、非服务乡镇站点或其他区县站点，则输出跨乡镇/跨县运行的辅助提示。

## 2. 业务前提

客货邮线路是镇级融合站出发，向村级站点或村级融合服务点延伸。`khy_line.county_code` 只表示线路管理归属区县，不表示线路从区县级站点出发。

因此，算法基准为：

1. 车辆当前绑定线路集合；
2. 绑定线路站点序列；
3. 绑定线路服务站点坐标；
4. 全量站点点位；
5. 车辆 GPS 轨迹；
6. 车辆实际过站事件。

## 3. 需要建立的视图和表

执行 `sql/cross-region-v7.sql`，建立或替换以下对象：

- `v_khy_algo_vehicle_line_station`：车辆绑定线路及线路站点序列增强视图；
- `v_khy_algo_all_station_point`：全量站点点位视图；
- `v_khy_algo_vehicle_station_pass`：车辆实际过站事件增强视图；
- `vehicle_gps_points`：沿用既有轨迹表，并建议增加 `(vehicle_id, gps_time)` 组合索引。

## 4. 后端检测器

新增检测器：

- 检测器编码：`VEHICLE_SERVICE_RANGE_OUT_OF_SCOPE`
- 检测器名称：车辆跨乡镇/跨县运行辅助检测（服务范围外运行）
- 异常大类：车辆资质与运行合规异常
- 异常子类：疑似线路服务范围外运行

检测器路径：

```text
src/main/java/com/fjkhy/abnormal/domain/business/vehicle/servicerange/
```

主要类：

- `VehicleServiceRangeOutOfScopeDetector`
- `VehicleServiceRangeRepository`
- `VehicleServiceRangeConfig`

## 5. 流程集成

V7 已将该检测器加入以下流程：

- `VEHICLE_MONTHLY_COMPLIANCE_FLOW`
- `COUNTY_MONTHLY_INTEGRATED_FLOW`

节点编码：

- `SERVICE_RANGE_OUT_OF_SCOPE`
- `VEHICLE_SERVICE_RANGE_OUT_OF_SCOPE`

## 6. 数据库连接配置

默认仍可内存演示启动。真实库检测需配置：

```yaml
khy:
  abnormal:
    datasource:
      url: jdbc:mysql://127.0.0.1:3306/khy?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false
      username: root
      password: root
```

## 7. 阈值与坐标系配置

```yaml
khy:
  abnormal:
    cross-region:
      # 站点坐标为火星坐标 GCJ-02；车辆轨迹 lng/lat 为 WGS84。
      # 算法统一使用 GCJ-02：优先读取 vehicle_gps_points.glng/glat，
      # 若 glng/glat 为空，则由后端将 lng/lat 从 WGS84 转为 GCJ-02 后再计算距离。
      coordinate-mode: GCJ02
      station-coordinate-mode: GCJ02
      gps-raw-coordinate-mode: WGS84
      station-radius-m: 500
      corridor-radius-m: 1000
      other-station-radius-m: 500
      min-valid-points: 30
      gps-gap-threshold-min: 10
      min-outside-duration-min: 15
      outside-point-ratio-threshold: 0.60
      near-unbound-station-count-threshold: 3
      gps-drift-speed-threshold-kmh: 120
      event-merge-window-min: 10
```

## 8. 坐标系要求与 V7.1 更新

算法计算时必须保证车辆轨迹和站点坐标处于同一坐标系。本项目当前数据口径为：

- `vehicle_gps_points.lng/lat`：GPS 原始轨迹坐标，WGS84；
- `vehicle_gps_points.glng/glat`：火星坐标，GCJ-02；
- 站点视图中的 `longitude/latitude`：来自高德/火星坐标，按 GCJ-02 使用。

因此 V7.1 后端统一采用 GCJ-02 进行距离计算：

1. 若 `glng/glat` 已存在，直接使用 `glng/glat`；
2. 若 `glng/glat` 为空，则调用 `CoordinateTransformService.wgs84ToGcj02` 将 `lng/lat` 转为 GCJ-02；
3. 转换后的轨迹点再与站点视图中的 GCJ-02 坐标计算距离。

本次新增代码：

- `com.fjkhy.abnormal.common.geo.CoordinateTransformService`：提供 WGS84 → GCJ-02 转换；
- `VehicleServiceRangeRepository.findGpsPoints`：优先读取 `glng/glat`，缺失时自动转换 `lng/lat`。

不要在一次距离计算中混用 WGS84 与 GCJ-02。若后续站点坐标改为 WGS84，应将 `coordinate-mode` 和 `station-coordinate-mode` 同步改为 WGS84，并确保所有站点视图坐标完成转换。

## 9. 触发规则

- R1：车辆长期偏离所有绑定线路服务点；
- R2：车辆连续偏离绑定线路服务范围；
- R3：车辆频繁接近非绑定线路站点；
- R4：车辆接近非服务乡镇站点辅助提示；
- R5：车辆接近其他区县站点辅助提示；
- R6：非绑定站点过站记录增强证据。

## 10. 联调建议

坐标转换更新后，应抽取一辆有明确过站记录的车辆进行联调：

1. 找到该车辆某个过站事件的 `event_time` 和 `station_id`；
2. 取过站时间前后 5–10 分钟内的 GPS 点；
3. 确认后端实际用于计算的轨迹坐标为 GCJ-02；
4. 计算轨迹点到过站站点的距离。

若转换正确，车辆到站附近的距离通常应在几百米范围内；若仍出现整体偏移，应重点检查站点坐标来源、`glng/glat` 是否已正确生成，以及前端地图展示坐标系是否一致。

## 11. 输出口径

输出内容必须保留辅助性边界：

> 当前未接入乡镇、区县行政边界数据，系统基于线路站点邻近关系和过站记录输出疑似服务范围外运行提示，不作为正式跨乡镇/跨县认定，需结合临时调度、GPS 质量和人工复核确认。
