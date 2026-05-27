package com.example.ky.analysis.infrastructure.persistence.prod;

import com.example.ky.analysis.domain.task.AnalysisTaskLog;
import com.example.ky.analysis.domain.task.port.AnalysisTaskLogPort;
import com.example.ky.analysis.infrastructure.persistence.repository.AnalysisTaskLogJpaRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Profile("prod")
public class JpaAnalysisTaskLogRepository implements AnalysisTaskLogPort {
    private final AnalysisTaskLogJpaRepository repository;

    public JpaAnalysisTaskLogRepository(AnalysisTaskLogJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public void save(AnalysisTaskLog log) {
        repository.save(log);
    }

    @Override
    public void update(AnalysisTaskLog log) {
        repository.update(log);
    }

    @Override
    public List<AnalysisTaskLog> queryAll() {
        return repository.queryAll();
    }
}
