# V6 API 说明

## 配置管理

- `GET /api/detection/flows`：查询检测流程列表。
- `POST /api/detection/flows`：新建检测流程。
- `GET /api/detection/flows/{flowCode}/nodes`：查询流程节点。
- `POST /api/detection/flows/{flowCode}/nodes`：向流程增加节点。
- `PUT /api/detection/flows/{flowCode}/nodes/{nodeCode}/enabled`：启停节点。
- `PUT /api/detection/flows/{flowCode}/nodes/{nodeCode}/order`：调整节点顺序。
- `GET /api/detection/rules`：查询规则视图。
- `GET /api/detection/thresholds`：查询阈值参数。
- `GET /api/detection/detectors`：查询已注册检测器。

## 任务运行

- `POST /api/anomaly/tasks/run`：执行检测任务。
- `GET /api/anomaly/tasks/{taskId}/steps`：查询任务步骤日志。
- `GET /api/anomaly/tasks/steps`：查询全部步骤日志。

## 异常结果

- `GET /api/anomaly/list`：查询异常结果。
- `GET /api/anomaly/{abnormalId}`：查询异常详情。
- `GET /api/anomaly/evidence/track/vehicles/{vehicleId}`：查询轨迹证据展示数据。
