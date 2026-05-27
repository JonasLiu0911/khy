package com.fjkhy.abnormal.application.result;

import com.fjkhy.abnormal.domain.common.ObjectType;
import com.fjkhy.abnormal.domain.result.AbnormalResult;
import com.fjkhy.abnormal.infrastructure.jdbc.AbnormalResultJdbcRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class AbnormalResultService {
    private final AbnormalResultJdbcRepository repository;
    public AbnormalResultService(AbnormalResultJdbcRepository repository) {
        this.repository = repository;
    }

    public void saveAll(List<AbnormalResult> results) {
        repository.saveAll(results);
    }

    public List<AbnormalResult> list(ObjectType objectType, String countyName, String subtype) {
        return repository.list(objectType, countyName, subtype);
    }

    public AbnormalResult detail(String abnormalId) {
        return repository.findById(abnormalId);
    }

    public AbnormalResult create(AbnormalResult result) {
        repository.create(result);
        return result;
    }

    public AbnormalResult update(String abnormalId, AbnormalResult result) {
        result.setAbnormalId(abnormalId);
        repository.update(result);
        return result;
    }

    public void delete(String abnormalId) {
        repository.delete(abnormalId);
    }

    public Map<String, Object> trackEvidence(String vehicleId, String period) {
        List<AbnormalResult> results = repository.listByObjectAndPeriod(vehicleId, period);
        List<Object> evidences = results.stream()
                .flatMap(r -> r.getEvidenceRecords() == null ? List.of().stream() : r.getEvidenceRecords().stream())
                .toList();
        return Map.of(
                "vehicleId", vehicleId,
                "period", period,
                "results", results,
                "evidences", evidences
        );
    }
}
