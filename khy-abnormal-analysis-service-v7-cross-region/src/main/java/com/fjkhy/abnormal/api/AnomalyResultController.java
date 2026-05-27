package com.fjkhy.abnormal.api;

import com.fjkhy.abnormal.application.result.AbnormalResultService;
import com.fjkhy.abnormal.domain.common.ObjectType;
import com.fjkhy.abnormal.domain.result.AbnormalResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/anomaly")
@CrossOrigin
public class AnomalyResultController {
    private final AbnormalResultService resultService;
    public AnomalyResultController(AbnormalResultService resultService) {
        this.resultService = resultService;
    }

    @GetMapping("/list") public Object list(
            @RequestParam(required = false) ObjectType objectType,
            @RequestParam(required = false) String countyName,
            @RequestParam(required = false) String subtype) {
        return resultService.list(objectType, countyName, subtype);
    }

    @GetMapping("/{abnormalId}") public Object detail(
            @PathVariable String abnormalId) {
        return resultService.detail(abnormalId);
    }

    @PostMapping public Object create(@RequestBody AbnormalResult result) {
        return resultService.create(result);
    }

    @PutMapping("/{abnormalId}") public Object update(
            @PathVariable String abnormalId,
            @RequestBody AbnormalResult result) {
        return resultService.update(abnormalId, result);
    }

    @DeleteMapping("/{abnormalId}") public void delete(
            @PathVariable String abnormalId) {
        resultService.delete(abnormalId);
    }

    @GetMapping("/evidence/track/vehicles/{vehicleId}") public Object trackEvidence(
            @PathVariable String vehicleId,
            @RequestParam(defaultValue = "2026-04") String period) {
        return resultService.trackEvidence(vehicleId, period);
    }
}
