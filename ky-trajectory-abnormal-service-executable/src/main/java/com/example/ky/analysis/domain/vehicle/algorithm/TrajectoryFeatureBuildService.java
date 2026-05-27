package com.example.ky.analysis.domain.vehicle.algorithm;

import com.example.ky.analysis.domain.common.port.SpatialCalculationPort;
import com.example.ky.analysis.domain.config.TrajectoryAnalysisConfig;
import com.example.ky.analysis.domain.line.model.LineConstraint;
import com.example.ky.analysis.domain.line.model.LineStation;
import com.example.ky.analysis.domain.vehicle.model.GpsPoint;
import com.example.ky.analysis.domain.vehicle.model.TrajectoryFeature;
import com.example.ky.analysis.domain.vehicle.model.TripSegment;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class TrajectoryFeatureBuildService {
    private final SpatialCalculationPort spatial;

    public TrajectoryFeatureBuildService(SpatialCalculationPort spatial) {
        this.spatial = spatial;
    }

    public List<TrajectoryFeature> build(List<TripSegment> trips, LineConstraint constraint, TrajectoryAnalysisConfig config) {
        List<TrajectoryFeature> list = new ArrayList<>();
        for (TripSegment trip : trips) {
            List<String> seq = new ArrayList<>();
            Set<String> set = new LinkedHashSet<>();
            for (GpsPoint p : trip.points()) {
                String code = spatial.gridCode(p.glongitude(), p.glatitude(), config.getGridSizeMeters());
                if (seq.isEmpty() || !seq.get(seq.size() - 1).equals(code)) {
                    seq.add(code);
                }
                set.add(code);
            }
            GpsPoint first = trip.points().get(0);
            GpsPoint last = trip.points().get(trip.points().size() - 1);
            String od = spatial.gridCode(first.glongitude(), first.glatitude(), config.getGridSizeMeters())
                    + "->" + spatial.gridCode(last.glongitude(), last.glatitude(), config.getGridSizeMeters());
            Set<String> hitStations = new LinkedHashSet<>();
            for (GpsPoint p : trip.points()) {
                for (LineStation station : constraint.stations()) {
                    if (spatial.distanceMeters(p.glongitude(), p.glatitude(), station.longitude(), station.latitude())
                            <= config.getStationRadiusMeters()) {
                        hitStations.add(station.stationId());
                    }
                }
            }
            list.add(new TrajectoryFeature(trip.tripId(), trip.vehicleId(), od, seq, set, hitStations));
        }
        return list;
    }
}
