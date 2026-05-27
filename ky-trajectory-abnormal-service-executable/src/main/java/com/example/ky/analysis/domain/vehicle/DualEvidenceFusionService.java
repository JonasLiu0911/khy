package com.example.ky.analysis.domain.vehicle;

import com.example.ky.analysis.domain.common.AbnormalLevel;
import com.example.ky.analysis.domain.common.EvidenceItem;
import com.example.ky.analysis.domain.common.EvidenceTextBuilder;
import com.example.ky.analysis.domain.config.TrajectoryAnalysisConfig;
import com.example.ky.analysis.domain.result.model.VehicleAbnormalResult;
import com.example.ky.analysis.domain.station.model.StationEventScoreResult;
import com.example.ky.analysis.domain.vehicle.model.TrackAnalysisResult;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DualEvidenceFusionService {
    private final EvidenceTextBuilder evidenceTextBuilder;

    public DualEvidenceFusionService(EvidenceTextBuilder evidenceTextBuilder) {
        this.evidenceTextBuilder = evidenceTextBuilder;
    }

    public void fillFusionResult(VehicleAbnormalResult result,
                                 TrackAnalysisResult track,
                                 StationEventScoreResult station,
                                 TrajectoryAnalysisConfig config) {
        result.setTrackScore(track.trackScore());
        result.setEventScore(station.eventScore());

        if (track.level() == AbnormalLevel.DATA_INSUFFICIENT && station.level() == AbnormalLevel.DATA_INSUFFICIENT) {
            result.setFinalScore(0);
            result.setAbnormalLevel(AbnormalLevel.DATA_INSUFFICIENT);
            result.setConflictFlag(false);
            result.setSuggestion("轨迹和过站数据均不足，建议先核查数据采集完整性。");
        } else if (Math.abs(track.trackScore() - station.eventScore()) >= 70) {
            result.setFinalScore(round(0.65 * track.trackScore() + 0.35 * station.eventScore()));
            result.setAbnormalLevel(AbnormalLevel.DATA_CONFLICT);
            result.setConflictFlag(true);
            result.setSuggestion("GPS轨迹证据与过站事件证据差异较大，建议复核GPS质量、过站事件和线路绑定配置。");
        } else {
            double finalScore = 0.65 * track.trackScore() + 0.35 * station.eventScore();
            result.setFinalScore(round(finalScore));
            result.setConflictFlag(false);
            if (track.trackScore() < config.getHighRiskScoreThreshold() && station.eventScore() < 50) {
                result.setAbnormalLevel(AbnormalLevel.HIGH_RISK);
                result.setSuggestion("GPS轨迹和过站事件均显示异常，建议列为重点人工复核车辆。");
            } else if (track.trackScore() < config.getSuspectScoreThreshold() || finalScore < config.getSuspectScoreThreshold()) {
                result.setAbnormalLevel(AbnormalLevel.SUSPECT);
                result.setSuggestion("存在轨迹无规律或绑定线路支撑不足现象，建议纳入疑似异常清单。");
            } else {
                result.setAbnormalLevel(AbnormalLevel.NORMAL);
                result.setSuggestion("当前窗口内未发现明显轨迹无规律异常。");
            }
        }

        List<EvidenceItem> all = new ArrayList<>();
        all.addAll(track.evidenceItems());
        all.addAll(station.evidenceItems());
        result.setEvidenceSummary(evidenceTextBuilder.build(all));
    }

    private double round(double v) {
        return Math.round(v * 10.0) / 10.0;
    }
}
