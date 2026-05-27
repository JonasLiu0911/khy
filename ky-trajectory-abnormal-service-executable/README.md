# 客货邮车辆轨迹异常检测智能分析服务

这是一个可运行的 Spring Boot 示例工程，用于演示“GPS轨迹规律性算法 + 原系统过站事件一致性核验 + 双证据融合判定”的整体架构和主流程。

## 1. 架构分层

- `api`：对外接口层，提供任务触发、配置管理、结果查询接口。
- `domain`：领域层，包含车辆、线路、站点、配置、任务、结果等业务模块。
- `infrastructure`：基础层，包含外部数据适配、内存结果仓储、定时器、GIS计算等技术实现。

## 2. 当前版本说明

当前版本使用内置模拟数据，可直接运行并触发计算：

- V001：轨迹和过站均较规律，通常为正常；
- V002：轨迹分散且过站不支持绑定线路，通常为高度异常或疑似异常；
- V003：GPS轨迹分散但过站事件正常，用于演示数据冲突。

后续对接真实系统时，主要替换以下基础层适配器：

- `MockGpsTrackDataAdapter`
- `MockStationEventDataAdapter`
- `MockVehicleDataAdapter`
- `MockLineStationDataAdapter`

## 3. 运行方式

```bash
mvn spring-boot:run
```

默认端口：`8088`

## 4. 常用接口

### 触发每日近30天滚动分析

```bash
curl -X POST "http://localhost:8088/api/analysis/tasks/run-daily?statDate=2026-05-01"
```

统计窗口为：2026-04-01 至 2026-04-30。

### 查询全部结果

```bash
curl "http://localhost:8088/api/analysis/vehicles/abnormal?statDate=2026-05-01"
```

### 查询高度异常车辆

```bash
curl "http://localhost:8088/api/analysis/vehicles/abnormal?statDate=2026-05-01&level=HIGH_RISK"
```

### 查询单车详情

```bash
curl "http://localhost:8088/api/analysis/vehicles/V002/abnormal-detail?statDate=2026-05-01"
```

### 查询配置

```bash
curl "http://localhost:8088/api/analysis/config/trajectory"
```

## 5. 业务口径

系统每日滚动计算近30天轨迹异常结果。月末复核时，不合并每日结果，直接取当月最后一天对应的近30天结果作为月度预筛结果。

## 6. 后续建议

1. 将内存仓储替换为数据库仓储。
2. 将模拟外部数据适配器替换为真实原系统API或数据库查询。
3. 根据业主复核结果调整阈值。
4. 接入Spring AI Alibaba问答系统，由问答系统直接查询结果表。
