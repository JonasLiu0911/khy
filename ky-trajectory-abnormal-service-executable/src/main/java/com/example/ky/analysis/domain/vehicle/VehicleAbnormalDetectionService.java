package com.example.ky.analysis.domain.vehicle;

import com.example.ky.analysis.domain.config.TrajectoryAnalysisConfig;
import com.example.ky.analysis.domain.line.LineConstraintService;
import com.example.ky.analysis.domain.line.model.LineConstraint;
import com.example.ky.analysis.domain.result.model.VehicleAbnormalResult;
import com.example.ky.analysis.domain.station.StationEventConsistencyService;
import com.example.ky.analysis.domain.station.model.StationEventScoreResult;
import com.example.ky.analysis.domain.task.AnalysisWindow;
import com.example.ky.analysis.domain.vehicle.model.TrackAnalysisResult;
import com.example.ky.analysis.domain.vehicle.port.VehicleDataPort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class VehicleAbnormalDetectionService {
    private final VehicleDataPort vehicleDataPort;
    private final LineConstraintService lineConstraintService;
    private final VehicleTrackAnalysisService vehicleTrackAnalysisService;
    private final StationEventConsistencyService stationEventConsistencyService;
    private final DualEvidenceFusionService dualEvidenceFusionService;

    public VehicleAbnormalDetectionService(VehicleDataPort vehicleDataPort,
                                           LineConstraintService lineConstraintService,
                                           VehicleTrackAnalysisService vehicleTrackAnalysisService,
                                           StationEventConsistencyService stationEventConsistencyService,
                                           DualEvidenceFusionService dualEvidenceFusionService) {
        this.vehicleDataPort = vehicleDataPort;
        this.lineConstraintService = lineConstraintService;
        this.vehicleTrackAnalysisService = vehicleTrackAnalysisService;
        this.stationEventConsistencyService = stationEventConsistencyService;
        this.dualEvidenceFusionService = dualEvidenceFusionService;
    }

    public List<VehicleAbnormalResult> detectBatch(List<String> vehicleIds,
                                                   AnalysisWindow window,
                                                   LocalDate statDate,
                                                   TrajectoryAnalysisConfig config) {
        List<VehicleAbnormalResult> results = new ArrayList<>();
        for (String vehicleId : vehicleIds) {
            results.add(detect(vehicleId, window, statDate, config));
        }
        return results;
    }

    public VehicleAbnormalResult detect(String vehicleId,
                                        AnalysisWindow window,
                                        LocalDate statDate,
                                        TrajectoryAnalysisConfig config) {
        LineConstraint constraint = lineConstraintService.buildConstraint(vehicleId);
        TrackAnalysisResult track = vehicleTrackAnalysisService.analyze(vehicleId, window, constraint, config);
        StationEventScoreResult station = stationEventConsistencyService.check(vehicleId, window, constraint, config);

        VehicleAbnormalResult result = new VehicleAbnormalResult();
        result.setStatDate(statDate);
        result.setWindowStart(window.startTime());
        result.setWindowEnd(window.endTime());
        result.setVehicleId(vehicleId);
        result.setPlateNo(vehicleDataPort.queryPlateNo(vehicleId));
        result.setLineId(constraint.lineId());
        result.setLineName(constraint.lineName());
        result.setAlgorithmVersion(config.getAlgorithmVersion());
        result.setTrackAnalysis(track);
        result.setStationEventAnalysis(station);

        dualEvidenceFusionService.fillFusionResult(result, track, station, config);
        return result;
    }
}
