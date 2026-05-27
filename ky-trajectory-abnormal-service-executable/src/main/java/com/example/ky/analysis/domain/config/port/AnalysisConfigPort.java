package com.example.ky.analysis.domain.config.port;

import com.example.ky.analysis.domain.config.TrajectoryAnalysisConfig;

public interface AnalysisConfigPort {
    TrajectoryAnalysisConfig loadTrajectoryConfig();
    void saveTrajectoryConfig(TrajectoryAnalysisConfig config);
}
