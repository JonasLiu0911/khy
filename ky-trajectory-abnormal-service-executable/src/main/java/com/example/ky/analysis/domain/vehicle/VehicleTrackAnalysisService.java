package com.example.ky.analysis.domain.vehicle;

import com.example.ky.analysis.domain.config.TrajectoryAnalysisConfig;
import com.example.ky.analysis.domain.line.model.LineConstraint;
import com.example.ky.analysis.domain.vehicle.algorithm.*;
import com.example.ky.analysis.domain.vehicle.model.*;
import com.example.ky.analysis.domain.vehicle.port.GpsTrackDataPort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VehicleTrackAnalysisService {
    private final GpsTrackDataPort gpsTrackDataPort;
    private final GpsCleanAlgorithm gpsCleanAlgorithm;
    private final TripSplitAlgorithm tripSplitAlgorithm;
    private final TrajectoryFeatureBuildService featureBuildService;
    private final TrajectorySimilarityAlgorithm similarityAlgorithm;
    private final TrajectoryClusterAlgorithm clusterAlgorithm;
    private final TrackRegularityScoreService scoreService;

    public VehicleTrackAnalysisService(GpsTrackDataPort gpsTrackDataPort,
                                       GpsCleanAlgorithm gpsCleanAlgorithm,
                                       TripSplitAlgorithm tripSplitAlgorithm,
                                       TrajectoryFeatureBuildService featureBuildService,
                                       TrajectorySimilarityAlgorithm similarityAlgorithm,
                                       TrajectoryClusterAlgorithm clusterAlgorithm,
                                       TrackRegularityScoreService scoreService) {
        this.gpsTrackDataPort = gpsTrackDataPort;
        this.gpsCleanAlgorithm = gpsCleanAlgorithm;
        this.tripSplitAlgorithm = tripSplitAlgorithm;
        this.featureBuildService = featureBuildService;
        this.similarityAlgorithm = similarityAlgorithm;
        this.clusterAlgorithm = clusterAlgorithm;
        this.scoreService = scoreService;
    }

    public TrackAnalysisResult analyze(String vehicleId,
                                       com.example.ky.analysis.domain.task.AnalysisWindow window,
                                       LineConstraint constraint,
                                       TrajectoryAnalysisConfig config) {
        List<GpsPoint> raw = gpsTrackDataPort.queryGpsPoints(vehicleId, window.startTime(), window.endTime());
        List<GpsPoint> cleaned = gpsCleanAlgorithm.clean(raw);
        List<TripSegment> trips = tripSplitAlgorithm.split(cleaned, config);
        List<TrajectoryFeature> features = featureBuildService.build(trips, constraint, config);
        double[][] simMatrix = similarityAlgorithm.buildMatrix(features);
        List<TrajectoryCluster> clusters = clusterAlgorithm.cluster(simMatrix, features, config);
        return scoreService.score(clusters, features, simMatrix, config);
    }
}
