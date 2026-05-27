package com.example.ky.analysis.infrastructure.persistence.prod;

import com.example.ky.analysis.domain.common.AbnormalLevel;
import com.example.ky.analysis.domain.result.model.VehicleAbnormalResult;
import com.example.ky.analysis.domain.result.port.VehicleAbnormalResultPort;
import com.example.ky.analysis.infrastructure.persistence.repository.VehicleAbnormalResultJpaRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
@Profile("prod")
public class JpaVehicleAbnormalResultRepository implements VehicleAbnormalResultPort {
    private final VehicleAbnormalResultJpaRepository repository;

    public JpaVehicleAbnormalResultRepository(VehicleAbnormalResultJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public void save(VehicleAbnormalResult result) {
        repository.save(result);
    }

    @Override
    public void batchSave(List<VehicleAbnormalResult> results) {
        repository.batchSave(results);
    }

    @Override
    public List<VehicleAbnormalResult> query(LocalDate statDate, AbnormalLevel level) {
        return repository.query(statDate == null ? null : Date.valueOf(statDate), level);
    }

    @Override
    public Optional<VehicleAbnormalResult> findByVehicleAndDate(String vehicleId, LocalDate statDate) {
        if (statDate == null) {
            return Optional.empty();
        }
        return repository.findByVehicleAndDate(vehicleId, Date.valueOf(statDate));
    }

    @Override
    public List<VehicleAbnormalResult> findByLevelAndDate(AbnormalLevel abnormalLevel, LocalDate statDate) {
        if (statDate == null) {
            return List.of();
        }
        return repository.findByLevelAndDate(abnormalLevel, Date.valueOf(statDate));
    }
}
