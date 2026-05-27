package com.example.ky.analysis.domain.visualization.model;

/**
 * 车辆展示对象。
 */
public record VehicleView(
        String vehicleId,
        String plateNo,
        String lineId,
        String lineName
) {
}
