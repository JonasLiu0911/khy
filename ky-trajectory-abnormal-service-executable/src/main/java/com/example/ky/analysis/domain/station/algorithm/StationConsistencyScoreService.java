package com.example.ky.analysis.domain.station.algorithm;

import com.example.ky.analysis.domain.common.AbnormalLevel;
import com.example.ky.analysis.domain.common.EvidenceItem;
import com.example.ky.analysis.domain.config.TrajectoryAnalysisConfig;
import com.example.ky.analysis.domain.line.model.LineConstraint;
import com.example.ky.analysis.domain.station.model.DailyStationSet;
import com.example.ky.analysis.domain.station.model.StationEvent;
import com.example.ky.analysis.domain.station.model.StationEventScoreResult;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class StationConsistencyScoreService {
    public StationEventScoreResult score(List<StationEvent> events,
                                         List<StationEvent> bindingEvents,
                                         List<DailyStationSet> dailySets,
                                         LineConstraint constraint,
                                         double setStability,
                                         double patternRatio,
                                         TrajectoryAnalysisConfig config) {
        int eventCount = events.size();
        if (eventCount < 3 || constraint.stationIds().size() < 2) {
            return new StationEventScoreResult(eventCount, 0, 0, 0, 0, 1,
                    0, AbnormalLevel.DATA_INSUFFICIENT,
                    List.of(new EvidenceItem("过站事件数量", String.valueOf(eventCount), ">=3", "过站事件不足")));
        }
        Set<String> hitStationIds = new HashSet<>();
        for (StationEvent e : bindingEvents) {
            hitStationIds.add(e.stationId());
        }
        double bindStationEventRatio = (double) bindingEvents.size() / events.size();
        double stationCoverRate = constraint.stationIds().isEmpty() ? 0 : (double) hitStationIds.size() / constraint.stationIds().size();
        double missingStationRatio = 1 - stationCoverRate;
        double dayRate = Math.min((double) dailySets.size() / 10.0, 1.0);

        double score = 100.0 * (
                0.25 * bindStationEventRatio
                        + 0.25 * stationCoverRate
                        + 0.20 * setStability
                        + 0.15 * patternRatio
                        + 0.15 * dayRate
        );
        AbnormalLevel level;
        if (score < config.getHighRiskScoreThreshold()) {
            level = AbnormalLevel.HIGH_RISK;
        } else if (score < config.getSuspectScoreThreshold()) {
            level = AbnormalLevel.SUSPECT;
        } else {
            level = AbnormalLevel.NORMAL;
        }

        List<EvidenceItem> evidence = List.of(
                new EvidenceItem("绑定站点事件占比", pct(bindStationEventRatio), "正常建议≥70%", bindStationEventRatio < 0.4 ? "绑定线路过站支撑不足" : "绑定线路过站具有支撑"),
                new EvidenceItem("绑定站点覆盖率", pct(stationCoverRate), "正常建议≥60%", stationCoverRate < 0.3 ? "覆盖绑定站点较少" : "覆盖一定绑定站点"),
                new EvidenceItem("最大过站模式占比", pct(patternRatio), "正常建议≥40%", patternRatio < 0.25 ? "过站模式不稳定" : "存在一定过站模式")
        );
        return new StationEventScoreResult(eventCount, round(bindStationEventRatio), round(stationCoverRate),
                round(setStability), round(patternRatio), round(missingStationRatio), round(score), level, evidence);
    }

    private String pct(double v) { return String.format(Locale.ROOT, "%.1f%%", v * 100); }
    private double round(double v) { return Math.round(v * 1000.0) / 1000.0; }
}
