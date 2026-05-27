package com.example.ky.analysis.domain.station.algorithm;

import com.example.ky.analysis.domain.station.model.StationEvent;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Component
public class StationEventCleanAlgorithm {
    public List<StationEvent> mergeDuplicated(List<StationEvent> events) {
        if (events == null || events.isEmpty()) {
            return List.of();
        }
        List<StationEvent> sorted = events.stream()
                .sorted(Comparator.comparing(StationEvent::vehicleId)
                        .thenComparing(StationEvent::stationId)
                        .thenComparing(StationEvent::eventTime))
                .toList();
        List<StationEvent> result = new ArrayList<>();
        StationEvent lastKept = null;
        for (StationEvent e : sorted) {
            if (lastKept == null
                    || !lastKept.vehicleId().equals(e.vehicleId())
                    || !lastKept.stationId().equals(e.stationId())
                    || Math.abs(Duration.between(lastKept.eventTime(), e.eventTime()).toMinutes()) > 30) {
                result.add(e);
                lastKept = e;
            }
        }
        return result;
    }
}
