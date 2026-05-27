package com.example.ky.analysis.domain.config;

import com.example.ky.analysis.domain.config.port.AnalysisConfigPort;
import org.springframework.stereotype.Service;

@Service
public class AnalysisConfigService {
    private final AnalysisConfigPort configPort;

    public AnalysisConfigService(AnalysisConfigPort configPort) {
        this.configPort = configPort;
    }

    public TrajectoryAnalysisConfig getTrajectoryConfig() {
        return configPort.loadTrajectoryConfig();
    }

    public TrajectoryAnalysisConfig updateTrajectoryConfig(TrajectoryAnalysisConfig config) {
        configPort.saveTrajectoryConfig(config);
        return config;
    }
}
