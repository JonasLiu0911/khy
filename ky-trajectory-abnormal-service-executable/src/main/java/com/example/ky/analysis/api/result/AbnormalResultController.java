package com.example.ky.analysis.api.result;

import com.example.ky.analysis.domain.common.AbnormalLevel;
import com.example.ky.analysis.domain.result.AbnormalResultService;
import com.example.ky.analysis.domain.result.model.VehicleAbnormalResult;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/anomaly-api/analysis/results")
public class AbnormalResultController {
    private final AbnormalResultService resultService;

    public AbnormalResultController(AbnormalResultService resultService) {
        this.resultService = resultService;
    }

    @GetMapping("/daily")
    public List<VehicleAbnormalResult> queryDailyResult(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate statDate,
            @RequestParam(required = false) AbnormalLevel level) {
        return resultService.queryDailyResult(statDate, level);
    }

    @GetMapping("/month-end")
    public List<VehicleAbnormalResult> queryMonthEndResult(
            @RequestParam String month,
            @RequestParam(required = false) AbnormalLevel level) {
        YearMonth ym = YearMonth.parse(month);
        LocalDate lastDay = ym.atEndOfMonth();
        return resultService.queryDailyResult(lastDay, level);
    }
}
