package com.example.ky.analysis.domain.vehicle.model;

import java.time.LocalDate;

public record VehicleBindingLine(
        String vehicleId,
        String lineId,
        LocalDate bindStartDate,
        LocalDate bindEndDate
) {}
