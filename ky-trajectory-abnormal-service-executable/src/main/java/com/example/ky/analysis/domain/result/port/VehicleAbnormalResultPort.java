package com.example.ky.analysis.domain.result.port;

import com.example.ky.analysis.domain.common.AbnormalLevel;
import com.example.ky.analysis.domain.result.model.VehicleAbnormalResult;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface VehicleAbnormalResultPort {
    void save(VehicleAbnormalResult result);
    void batchSave(List<VehicleAbnormalResult> results);
    List<VehicleAbnormalResult> query(LocalDate statDate, AbnormalLevel level);
    Optional<VehicleAbnormalResult> findByVehicleAndDate(String vehicleId, LocalDate statDate);
    List<VehicleAbnormalResult> findByLevelAndDate(AbnormalLevel abnormalLevel, LocalDate statDate);
}
