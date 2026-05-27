package com.example.ky.analysis.api.vehicle;

import com.example.ky.analysis.domain.common.AbnormalLevel;
import com.example.ky.analysis.domain.result.AbnormalResultService;
import com.example.ky.analysis.domain.result.model.VehicleAbnormalResult;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/anomaly-api/analysis/vehicles")
public class VehicleAbnormalController {
    private final AbnormalResultService resultService;

    public VehicleAbnormalController(AbnormalResultService resultService) {
        this.resultService = resultService;
    }

    @GetMapping("/abnormal")
    public List<VehicleAbnormalResult> listAbnormalVehicles(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate statDate,
            @RequestParam(required = false) AbnormalLevel level) {
        return resultService.queryDailyResult(statDate, level);
    }

    @GetMapping("/{vehicleId}/abnormal-detail")
    public VehicleAbnormalResult getVehicleAbnormalDetail(
            @PathVariable String vehicleId,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate statDate) {
        return resultService.getDetail(vehicleId, statDate)
                .orElseThrow(() -> new IllegalArgumentException("未找到该车辆在指定统计日的异常检测结果"));
    }
}
