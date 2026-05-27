package com.example.ky.analysis.api.visualization;

import com.example.ky.analysis.domain.common.AbnormalLevel;
import com.example.ky.analysis.domain.visualization.TrackVisualizationService;
import com.example.ky.analysis.domain.visualization.model.TrackVisualizationResult;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * 车辆轨迹异常检测结果可视化接口。
 *
 * 说明：
 * 1. 本接口不重新计算轨迹异常，仅基于已沉淀的异常检测结果组织地图展示数据。
 * 2. 后端返回检测窗口内“按天分组”的轨迹点、绑定线路站点和异常证据摘要。
 * 3. 前端可调用高德地图、天地图等外部地图服务叠加每日轨迹。
 */
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/anomaly-api/analysis/visualization")
public class TrackVisualizationController {

    private final TrackVisualizationService visualizationService;

    public TrackVisualizationController(TrackVisualizationService visualizationService) {
        this.visualizationService = visualizationService;
    }

    /**
     * 查询单车检测周期内的轨迹可视化数据。
     *
     * 示例：
     * GET /api/analysis/visualization/vehicles/V001/tracks?statDate=2026-05-01&includeStations=true&maxPointsPerDay=1200
     *
     * @param vehicleId 车辆ID
     * @param statDate 统计日期。与异常检测结果表中的 statDate 保持一致。
     * @param includeStations 是否返回绑定线路站点
     * @param maxPointsPerDay 每天最多返回轨迹点数量，用于控制前端渲染压力
     * @return 前端地图展示所需的车辆、检测窗口、异常结果、站点和每日轨迹数据
     */
    @GetMapping("/vehicles/{vehicleId}/track")
    public TrackVisualizationResult getVehicleTracks(
            @PathVariable String vehicleId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate statDate,
            @RequestParam(defaultValue = "true") boolean includeStations,
            @RequestParam(defaultValue = "1200") int maxPointsPerDay
    ) {
        return visualizationService.buildVehicleTrackVisualization(vehicleId, statDate, includeStations, maxPointsPerDay);
    }

    @GetMapping("/vehicles/byAbnormalLevel")
    public List<TrackVisualizationResult> getResultsByAbnormalLevel(
            @RequestParam AbnormalLevel abnormalLevel,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate statDate
    ) {
        return visualizationService.getAbnormalLevelVehiclesDetail(abnormalLevel, statDate);
    }
}
