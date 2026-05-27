package com.example.ky.analysis.api.task;

import com.example.ky.analysis.domain.task.AnalysisTaskLog;
import com.example.ky.analysis.domain.task.AnalysisTaskService;
import com.example.ky.analysis.domain.task.AnalysisVehicle;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/anomaly-api/analysis/tasks")
public class AnalysisTaskController {
    private final AnalysisTaskService taskService;

    public AnalysisTaskController(AnalysisTaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping("/run-daily")
    public AnalysisTaskLog runDaily(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate statDate) {
        LocalDate target = statDate == null ? LocalDate.now() : statDate;
        return taskService.runDailyTrajectoryAbnormalTask(target, "MANUAL");
    }

    @GetMapping("/statistics-vehicle")
    public List<AnalysisVehicle> analyzeVehicleStatistics() {
        return taskService.runAnalyzeVehicleStatistics();
    }

    @GetMapping("/logs")
    public List<AnalysisTaskLog> queryTaskLogs() {
        return taskService.queryTaskLogs();
    }
}
