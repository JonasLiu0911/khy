package com.example.ky.analysis.infrastructure.persistence.prod;

import com.example.ky.analysis.domain.config.TrajectoryAnalysisConfig;
import com.example.ky.analysis.domain.config.port.AnalysisConfigPort;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("prod")
public class JpaAnalysisConfigRepository implements AnalysisConfigPort {
    private TrajectoryAnalysisConfig config = new TrajectoryAnalysisConfig();

    @Override
    public TrajectoryAnalysisConfig loadTrajectoryConfig() {
        return config;
    }

    @Override
    public void saveTrajectoryConfig(TrajectoryAnalysisConfig config) {
        this.config = config;
    }
}

