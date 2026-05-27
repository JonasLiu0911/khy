# 客货邮运营合规性异常检测与分析模块 V7.1

本版本在 V6 架构基础上新增“车辆跨乡镇/跨县运行辅助检测”，一期实现口径为“疑似线路服务范围外运行”。V7.1 进一步补充坐标系统一处理：站点坐标按 GCJ-02 使用，车辆轨迹原始 `lng/lat` 为 WGS84 时，后端在距离计算前自动转换为 GCJ-02。

## 新增内容

- 新增检测器：`VEHICLE_SERVICE_RANGE_OUT_OF_SCOPE`
- 新增坐标转换服务：`CoordinateTransformService`
- 新增算法说明：`docs/CROSS_REGION_SERVICE_RANGE_GUIDE_V7.md`
- 新增 SQL：`sql/cross-region-v7.sql`
- 流程集成：车辆月度流程与区县综合流程均已加入服务范围外运行检测节点。

## 坐标系口径

当前建议配置：

```yaml
khy:
  abnormal:
    cross-region:
      coordinate-mode: GCJ02
      station-coordinate-mode: GCJ02
      gps-raw-coordinate-mode: WGS84
```

运行逻辑：

1. 优先使用 `vehicle_gps_points.glng/glat` 参与距离计算；
2. 若 `glng/glat` 为空，则由后端把 `lng/lat` 从 WGS84 转为 GCJ-02；
3. 转换后的车辆轨迹坐标再与站点视图中的 GCJ-02 坐标计算距离。

## 启动

```bash
mvn spring-boot:run
```

默认不配置数据库时仍可启动；真实检测需在 `application.yml` 中配置 `khy.abnormal.datasource`。

## 关键接口

- `POST /api/anomaly/tasks/run`
- `GET /api/anomaly/list?objectType=VEHICLE&subtype=疑似线路服务范围外运行`
- `GET /api/detection/detectors`
- `GET /api/detection/flows/{flowCode}/nodes`
