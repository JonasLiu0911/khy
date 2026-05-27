package com.fjkhy.abnormal.api;

import com.fjkhy.abnormal.application.flow.DetectionFlowOrchestrator;
import com.fjkhy.abnormal.application.flow.DetectionStepLogService;
import com.fjkhy.abnormal.domain.task.DetectionTaskRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/anomaly/tasks")
@CrossOrigin
public class DetectionTaskController {
    private final DetectionFlowOrchestrator orchestrator;
    private final DetectionStepLogService stepLogService;
    public DetectionTaskController(DetectionFlowOrchestrator orchestrator, DetectionStepLogService stepLogService) {
        this.orchestrator = orchestrator;
        this.stepLogService = stepLogService;
    }
    @PostMapping("/run") public Object run(@RequestBody DetectionTaskRequest request) {
        return orchestrator.run(request);
    }
    @GetMapping("/{taskId}/steps") public Object steps(@PathVariable String taskId) { return stepLogService.listByTask(taskId); }
    @GetMapping("/steps") public Object allSteps() { return stepLogService.all(); }
}
