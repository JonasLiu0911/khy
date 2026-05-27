# V6 需求-架构-代码追踪矩阵

| 需求理解文档内容 | V6 架构归类 | 后端代码位置 | 前端页面 |
|---|---|---|---|
| 站点设置条件异常 | 站点设置与服务支撑合规异常 | `domain/business/station/StationSettingConditionDetector.java` | 站点异常 |
| 照片水印规范性异常 | 站点设置与服务支撑合规异常 | `StationPhotoWatermarkDetector.java` | 站点异常 |
| 长期无运行证据或无运营记录 | 车辆资质与运行合规异常 | `VehicleLongTermNoOperationDetector.java` | 车辆异常 |
| 车辆月度轨迹规律性检测 | 车辆运行合规异常-运行轨迹无规律 | `trackregularity/VehicleTrackRegularityDetector.java` | 轨迹规律性异常 |
| 轨迹与过站记录一致性核验 | 车辆运行证据一致性异常 | `trackstationconsistency/VehicleTrackStationEventConsistencyDetector.java` | 轨迹与过站一致性 |
| 轨迹地图展示 | 异常证据展示 | `/api/anomaly/evidence/track/vehicles/{vehicleId}` | 轨迹证据展示 |
| 有异常/无异常车辆对比 | 异常分析结果展示 | `/api/anomaly/list?objectType=VEHICLE` | 有异常/无异常车辆对比 |
| 线路站点数量异常 | 线路配置与执行合规异常 | `LineStationCountDetector.java` | 线路异常 |
| 有件无车 | 跨对象服务履约辅助分析 | `FulfillmentPackageNoVehicleAnalyzer.java` | 跨对象履约 |
| 数据质量提示 | 支撑能力 | `domain/support/quality/*Checker.java` | 数据质量提示配置 |
| 任务流程配置 | 轻量化检测流程服务 | `application/flow/*` | 检测流程配置 |
