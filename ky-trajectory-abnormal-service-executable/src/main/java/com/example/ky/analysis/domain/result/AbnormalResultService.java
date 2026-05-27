package com.example.ky.analysis.domain.result;

import com.example.ky.analysis.domain.common.AbnormalLevel;
import com.example.ky.analysis.domain.result.model.VehicleAbnormalResult;
import com.example.ky.analysis.domain.result.port.VehicleAbnormalResultPort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class AbnormalResultService {
    private final VehicleAbnormalResultPort port;

    public AbnormalResultService(VehicleAbnormalResultPort port) {
        this.port = port;
    }

    public void saveDailyResult(VehicleAbnormalResult result) {
        port.save(result);
    }

    public void batchSaveDailyResult(List<VehicleAbnormalResult> results) {
        port.batchSave(results);
    }

    public List<VehicleAbnormalResult> queryDailyResult(LocalDate statDate, AbnormalLevel level) {
        return port.query(statDate, level);
    }

    public Optional<VehicleAbnormalResult> getDetail(String vehicleId, LocalDate statDate) {
        return port.findByVehicleAndDate(vehicleId, statDate);
    }

    public List<VehicleAbnormalResult> getDetailByLevel(AbnormalLevel abnormalLevel, LocalDate statDate) {
        return port.findByLevelAndDate(abnormalLevel, statDate);
    }
}
