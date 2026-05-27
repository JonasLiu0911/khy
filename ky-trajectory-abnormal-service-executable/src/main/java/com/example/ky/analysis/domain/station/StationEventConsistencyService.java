package com.example.ky.analysis.domain.station;

import com.example.ky.analysis.domain.config.TrajectoryAnalysisConfig;
import com.example.ky.analysis.domain.line.model.LineConstraint;
import com.example.ky.analysis.domain.station.algorithm.StationConsistencyScoreService;
import com.example.ky.analysis.domain.station.algorithm.StationEventCleanAlgorithm;
import com.example.ky.analysis.domain.station.algorithm.StationPatternAnalysisService;
import com.example.ky.analysis.domain.station.model.DailyStationSet;
import com.example.ky.analysis.domain.station.model.StationEvent;
import com.example.ky.analysis.domain.station.model.StationEventScoreResult;
import com.example.ky.analysis.domain.station.port.StationEventDataPort;
import com.example.ky.analysis.domain.task.AnalysisWindow;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class StationEventConsistencyService {
    private final StationEventDataPort stationEventDataPort;
    private final StationEventCleanAlgorithm cleanAlgorithm;
    private final StationPatternAnalysisService patternAnalysisService;
    private final StationConsistencyScoreService scoreService;

    public StationEventConsistencyService(StationEventDataPort stationEventDataPort,
                                          StationEventCleanAlgorithm cleanAlgorithm,
                                          StationPatternAnalysisService patternAnalysisService,
                                          StationConsistencyScoreService scoreService) {
        this.stationEventDataPort = stationEventDataPort;
        this.cleanAlgorithm = cleanAlgorithm;
        this.patternAnalysisService = patternAnalysisService;
        this.scoreService = scoreService;
    }

    public StationEventScoreResult check(String vehicleId,
                                         AnalysisWindow window,
                                         LineConstraint constraint,
                                         TrajectoryAnalysisConfig config) {
        List<StationEvent> raw = stationEventDataPort.queryStationEvents(vehicleId, window.startTime(), window.endTime());
        List<StationEvent> events = cleanAlgorithm.mergeDuplicated(raw);
        Set<String> bindStationIds = constraint.stationIds();
        List<StationEvent> bindingEvents = events.stream()
                .filter(e -> bindStationIds.contains(e.stationId()))
                .toList();
        List<DailyStationSet> dailySets = patternAnalysisService.buildDailyStationSets(vehicleId, bindingEvents);
        double setStability = patternAnalysisService.calcSetStability(dailySets);
        double patternRatio = patternAnalysisService.calcPatternRatio(dailySets);
        return scoreService.score(events, bindingEvents, dailySets, constraint, setStability, patternRatio, config);
    }
}
