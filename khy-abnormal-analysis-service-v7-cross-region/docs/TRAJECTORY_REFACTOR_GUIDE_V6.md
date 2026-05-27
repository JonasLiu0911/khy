# 原轨迹异常代码按 V6 架构重构指南

原“轨迹异常”已可使用，不建议推翻。V6 要求拆成两个业务能力，并通过适配器接入。

## 1. 轨迹规律性异常

需求归类：车辆资质与运行合规异常 -> 运行轨迹无规律。

接入位置：

```text
domain/business/vehicle/trackregularity/
├─ VehicleTrackRegularityDetector.java
├─ TrackRegularityLegacyAdapter.java
└─ DefaultTrackRegularityLegacyAdapter.java
```

修改方式：将原轨迹清洗、行程切分、轨迹特征构建、规律性评分等代码迁入 `DefaultTrackRegularityLegacyAdapter`，再由 `VehicleTrackRegularityDetector` 统一生成异常结果。

## 2. 轨迹与过站记录一致性异常

需求归类：车辆运行证据一致性异常 -> 轨迹与过站事件一致性异常。

接入位置：

```text
domain/business/vehicle/trackstationconsistency/
├─ VehicleTrackStationEventConsistencyDetector.java
├─ TrackStationConsistencyLegacyAdapter.java
└─ DefaultTrackStationConsistencyLegacyAdapter.java
```

修改方式：将原过站事件比对、轨迹到站识别、双证据融合判定等代码迁入 `DefaultTrackStationConsistencyLegacyAdapter`。

## 3. 不应迁入数据质量模块

轨迹与过站一致性核验不是数据质量问题。数据质量模块只负责判断轨迹点、过站事件、站点坐标是否具备计算条件。
