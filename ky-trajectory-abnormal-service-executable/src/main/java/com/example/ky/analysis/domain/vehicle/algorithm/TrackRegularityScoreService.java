package com.example.ky.analysis.domain.vehicle.algorithm;

import com.example.ky.analysis.domain.common.AbnormalLevel;
import com.example.ky.analysis.domain.common.EvidenceItem;
import com.example.ky.analysis.domain.config.TrajectoryAnalysisConfig;
import com.example.ky.analysis.domain.vehicle.model.TrackAnalysisResult;
import com.example.ky.analysis.domain.vehicle.model.TrajectoryCluster;
import com.example.ky.analysis.domain.vehicle.model.TrajectoryFeature;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class TrackRegularityScoreService {
    public TrackAnalysisResult score(List<TrajectoryCluster> clusters,
                                     List<TrajectoryFeature> features,
                                     double[][] simMatrix,
                                     TrajectoryAnalysisConfig config) {
        int validTripCount = features.size();
        if (validTripCount < config.getMinValidTripCount()) {
            return new TrackAnalysisResult(validTripCount, 0, 0, 0, 1, 0, 0, 0,
                    0, AbnormalLevel.DATA_INSUFFICIENT,
                    List.of(new EvidenceItem("有效出行次数", String.valueOf(validTripCount),
                            ">=" + config.getMinValidTripCount(), "不足以支撑轨迹规律性判断")));
        }

        int nonNoiseClusterCount = (int) clusters.stream().filter(c -> !c.noise()).count();
        int maxClusterSize = clusters.stream().mapToInt(c -> c.tripIds().size()).max().orElse(0);
        int noiseCount = clusters.stream().filter(TrajectoryCluster::noise).mapToInt(c -> c.tripIds().size()).sum();

        double mainPatternRatio = ratio(maxClusterSize, validTripCount);
        double noiseTripRatio = ratio(noiseCount, validTripCount);
        double routeEntropy = normalizedEntropy(clusters, validTripCount);
        double avgSimilarity = averageSimilarity(simMatrix);
        double odStability = calcMaxRatio(features.stream().map(TrajectoryFeature::odPair).toList());
        double bindStationTripRatio = ratio((int) features.stream().filter(f -> !f.hitStationSet().isEmpty()).count(), validTripCount);

        double score = 100.0 * (
                0.25 * mainPatternRatio
                        + 0.20 * (1 - routeEntropy)
                        + 0.15 * avgSimilarity
                        + 0.15 * odStability
                        + 0.15 * bindStationTripRatio
                        + 0.10 * (1 - noiseTripRatio)
        );
        AbnormalLevel level = judge(score, config);

        List<EvidenceItem> evidence = List.of(
                new EvidenceItem("主运行模式占比", pct(mainPatternRatio), "正常建议≥50%", mainPatternRatio < 0.25 ? "主路线不明显" : "主路线具有一定支撑"),
                new EvidenceItem("零散轨迹占比", pct(noiseTripRatio), "高度异常参考>50%", noiseTripRatio > 0.5 ? "零散轨迹较多" : "零散轨迹可控"),
                new EvidenceItem("OD稳定度", pct(odStability), "正常建议≥50%", odStability < 0.3 ? "起终点组合分散" : "起终点具有一定稳定性"),
                new EvidenceItem("绑定站点邻近出行占比", pct(bindStationTripRatio), "正常建议≥60%", bindStationTripRatio < 0.4 ? "轨迹与绑定线路站点关联较弱" : "轨迹与绑定线路站点有关联")
        );

        return new TrackAnalysisResult(validTripCount, nonNoiseClusterCount, mainPatternRatio,
                noiseTripRatio, routeEntropy, avgSimilarity, odStability, bindStationTripRatio,
                round(score), level, evidence);
    }

    private AbnormalLevel judge(double score, TrajectoryAnalysisConfig config) {
        if (score < config.getHighRiskScoreThreshold()) {
            return AbnormalLevel.HIGH_RISK;
        }
        if (score < config.getSuspectScoreThreshold()) {
            return AbnormalLevel.SUSPECT;
        }
        return AbnormalLevel.NORMAL;
    }

    private double normalizedEntropy(List<TrajectoryCluster> clusters, int total) {
        if (clusters.size() <= 1 || total == 0) {
            return 0.0;
        }
        double h = 0.0;
        for (TrajectoryCluster c : clusters) {
            double p = (double) c.tripIds().size() / total;
            h += -p * Math.log(p);
        }
        return h / Math.log(clusters.size());
    }

    private double averageSimilarity(double[][] matrix) {
        int n = matrix.length;
        if (n <= 1) {
            return 1.0;
        }
        double sum = 0;
        int count = 0;
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                sum += matrix[i][j];
                count++;
            }
        }
        return count == 0 ? 0.0 : sum / count;
    }

    private double calcMaxRatio(List<String> values) {
        if (values.isEmpty()) {
            return 0.0;
        }
        Map<String, Long> counts = values.stream().collect(Collectors.groupingBy(v -> v, Collectors.counting()));
        long max = counts.values().stream().mapToLong(Long::longValue).max().orElse(0);
        return (double) max / values.size();
    }

    private double ratio(int a, int b) { return b == 0 ? 0.0 : (double) a / b; }
    private String pct(double v) { return String.format(Locale.ROOT, "%.1f%%", v * 100); }
    private double round(double v) { return Math.round(v * 10.0) / 10.0; }
}
