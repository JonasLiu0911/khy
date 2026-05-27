package com.example.ky.analysis.api.config;

import com.example.ky.analysis.domain.config.AnalysisConfigService;
import com.example.ky.analysis.domain.config.TrajectoryAnalysisConfig;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/anomaly-api/analysis/config")
public class AnalysisConfigController {
    private final AnalysisConfigService configService;

    public AnalysisConfigController(AnalysisConfigService configService) {
        this.configService = configService;
    }

    @GetMapping("/trajectory")
    public TrajectoryAnalysisConfig getTrajectoryConfig() {
        return configService.getTrajectoryConfig();
    }

    @PutMapping("/trajectory")
    public TrajectoryAnalysisConfig updateTrajectoryConfig(@RequestBody TrajectoryAnalysisConfig config) {
        return configService.updateTrajectoryConfig(config);
    }
}
