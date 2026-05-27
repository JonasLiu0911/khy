package com.example.ky.analysis.domain.task;

import com.example.ky.analysis.domain.config.AnalysisConfigService;
import com.example.ky.analysis.domain.config.TrajectoryAnalysisConfig;
import com.example.ky.analysis.domain.result.AbnormalResultService;
import com.example.ky.analysis.domain.result.model.VehicleAbnormalResult;
import com.example.ky.analysis.domain.task.port.AnalysisTaskLogPort;
import com.example.ky.analysis.domain.vehicle.VehicleAbnormalDetectionService;
import com.example.ky.analysis.domain.vehicle.port.VehicleDataPort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class AnalysisTaskService {
    private final AnalysisConfigService configService;
    private final VehicleDataPort vehicleDataPort;
    private final VehicleAbnormalDetectionService detectionService;
    private final AbnormalResultService resultService;
    private final AnalysisTaskLogPort taskLogPort;

    public AnalysisTaskService(AnalysisConfigService configService,
                               VehicleDataPort vehicleDataPort,
                               VehicleAbnormalDetectionService detectionService,
                               AbnormalResultService resultService,
                               AnalysisTaskLogPort taskLogPort) {
        this.configService = configService;
        this.vehicleDataPort = vehicleDataPort;
        this.detectionService = detectionService;
        this.resultService = resultService;
        this.taskLogPort = taskLogPort;
    }

    public AnalysisTaskLog runDailyTrajectoryAbnormalTask(LocalDate statDate, String triggerType) {
        TrajectoryAnalysisConfig config = configService.getTrajectoryConfig();
        AnalysisWindow window = buildWindow(statDate, config.getWindowDays());
        AnalysisTaskLog log = new AnalysisTaskLog();
        log.setTaskId(UUID.randomUUID().toString());
        log.setTaskType("TRAJECTORY_ABNORMAL_DAILY");
        log.setTriggerType(triggerType);
        log.setStatus("RUNNING");
        log.setWindowStart(window.startTime());
        log.setWindowEnd(window.endTime());
        log.setStartTime(LocalDateTime.now());
        taskLogPort.save(log);
        try {
            List<String> vehicles = vehicleDataPort.queryActiveVehicleIds();
            log.setTotalCount(vehicles.size());
            List<VehicleAbnormalResult> results = detectionService.detectBatch(vehicles, window, statDate, config);
            resultService.batchSaveDailyResult(results);
            log.setSuccessCount(results.size());
            log.setFailCount(0);
            log.setStatus("SUCCESS");
        } catch (Exception ex) {
            log.setStatus("FAILED");
            log.setErrorMessage(ex.getMessage());
        } finally {
            log.setEndTime(LocalDateTime.now());
            taskLogPort.update(log);
        }
        return log;
    }

    public AnalysisWindow buildWindow(LocalDate statDate, int windowDays) {
        LocalDateTime end = statDate.minusDays(1).atTime(LocalTime.MAX);
        LocalDateTime start = statDate.minusDays(windowDays).atStartOfDay();
        return new AnalysisWindow(start, end, windowDays);
    }

    public List<AnalysisTaskLog> queryTaskLogs() {
        return taskLogPort.queryAll();
    }

    public List<AnalysisVehicle> runAnalyzeVehicleStatistics() {
        List<AnalysisVehicle> results = new ArrayList<>(vehicleDataPort.queryAnalysisVehicles());
        vehicleDataPort.saveVehicles(results);
        return results;
    }
}
