package com.example.ky.analysis.infrastructure.scheduler;

import com.example.ky.analysis.domain.config.AnalysisConfigService;
import com.example.ky.analysis.domain.task.AnalysisTaskService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class TrajectoryAbnormalDailyScheduler {
    private final AnalysisTaskService taskService;
    private final AnalysisConfigService configService;

    public TrajectoryAbnormalDailyScheduler(AnalysisTaskService taskService, AnalysisConfigService configService) {
        this.taskService = taskService;
        this.configService = configService;
    }

    @Scheduled(cron = "${analysis.trajectory-abnormal.daily-cron:0 30 0 * * ?}")
    public void executeDaily() {
        if (!configService.getTrajectoryConfig().isEnabled()) {
            return;
        }
        if (!"DAILY".equalsIgnoreCase(configService.getTrajectoryConfig().getScheduleMode())) {
            return;
        }
        taskService.runDailyTrajectoryAbnormalTask(LocalDate.now(), "SCHEDULED");
    }
}
