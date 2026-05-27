package com.example.ky.analysis.domain.station.algorithm;

import com.example.ky.analysis.domain.station.model.DailyStationSet;
import com.example.ky.analysis.domain.station.model.StationEvent;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StationPatternAnalysisService {
    public List<DailyStationSet> buildDailyStationSets(String vehicleId, List<StationEvent> bindingEvents) {
        Map<LocalDate, Set<String>> byDay = new TreeMap<>();
        for (StationEvent event : bindingEvents) {
            byDay.computeIfAbsent(event.eventTime().toLocalDate(), d -> new TreeSet<>()).add(event.stationId());
        }
        return byDay.entrySet().stream()
                .map(e -> new DailyStationSet(vehicleId, e.getKey(), e.getValue()))
                .toList();
    }

    public double calcSetStability(List<DailyStationSet> dailySets) {
        if (dailySets.size() <= 1) {
            return dailySets.isEmpty() ? 0.0 : 1.0;
        }
        double sum = 0;
        int count = 0;
        for (int i = 0; i < dailySets.size(); i++) {
            for (int j = i + 1; j < dailySets.size(); j++) {
                sum += jaccard(dailySets.get(i).stationIds(), dailySets.get(j).stationIds());
                count++;
            }
        }
        return count == 0 ? 0.0 : sum / count;
    }

    public double calcPatternRatio(List<DailyStationSet> dailySets) {
        if (dailySets.isEmpty()) {
            return 0.0;
        }
        Map<String, Long> counts = dailySets.stream()
                .map(d -> String.join(",", d.stationIds()))
                .collect(Collectors.groupingBy(s -> s, Collectors.counting()));
        long max = counts.values().stream().mapToLong(Long::longValue).max().orElse(0);
        return (double) max / dailySets.size();
    }

    private double jaccard(Set<String> a, Set<String> b) {
        Set<String> union = new HashSet<>(a);
        union.addAll(b);
        Set<String> inter = new HashSet<>(a);
        inter.retainAll(b);
        return union.isEmpty() ? 0.0 : (double) inter.size() / union.size();
    }
}
