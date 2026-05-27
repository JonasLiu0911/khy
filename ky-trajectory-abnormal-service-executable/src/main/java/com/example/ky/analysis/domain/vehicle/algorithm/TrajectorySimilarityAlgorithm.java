package com.example.ky.analysis.domain.vehicle.algorithm;

import com.example.ky.analysis.domain.vehicle.model.TrajectoryFeature;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class TrajectorySimilarityAlgorithm {
    public double[][] buildMatrix(List<TrajectoryFeature> features) {
        int n = features.size();
        double[][] matrix = new double[n][n];
        for (int i = 0; i < n; i++) {
            matrix[i][i] = 1.0;
            for (int j = i + 1; j < n; j++) {
                double sim = similarity(features.get(i), features.get(j));
                matrix[i][j] = sim;
                matrix[j][i] = sim;
            }
        }
        return matrix;
    }

    public double similarity(TrajectoryFeature a, TrajectoryFeature b) {
        double od = odSimilarity(a.odPair(), b.odPair());
        double gridSet = jaccard(a.gridSet(), b.gridSet());
        double gridSeq = lcsSimilarity(a.gridSequence(), b.gridSequence());
        double station = jaccard(a.hitStationSet(), b.hitStationSet());
        if (a.hitStationSet().isEmpty() && b.hitStationSet().isEmpty()) {
            return 0.30 * od + 0.45 * gridSet + 0.25 * gridSeq;
        }
        return 0.25 * od + 0.35 * gridSet + 0.20 * gridSeq + 0.20 * station;
    }

    private double odSimilarity(String a, String b) {
        if (a.equals(b)) {
            return 1.0;
        }
        String[] aa = a.split("->");
        String[] bb = b.split("->");
        if (aa.length == 2 && bb.length == 2 && aa[0].equals(bb[1]) && aa[1].equals(bb[0])) {
            return 0.7;
        }
        return 0.0;
    }

    private double jaccard(Set<String> a, Set<String> b) {
        if ((a == null || a.isEmpty()) && (b == null || b.isEmpty())) {
            return 0.0;
        }
        Set<String> union = new HashSet<>(a);
        union.addAll(b);
        Set<String> inter = new HashSet<>(a);
        inter.retainAll(b);
        return union.isEmpty() ? 0.0 : (double) inter.size() / union.size();
    }

    private double lcsSimilarity(List<String> a, List<String> b) {
        if (a.isEmpty() || b.isEmpty()) {
            return 0.0;
        }
        int[][] dp = new int[a.size() + 1][b.size() + 1];
        for (int i = 1; i <= a.size(); i++) {
            for (int j = 1; j <= b.size(); j++) {
                if (a.get(i - 1).equals(b.get(j - 1))) {
                    dp[i][j] = dp[i - 1][j - 1] + 1;
                } else {
                    dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]);
                }
            }
        }
        return (double) dp[a.size()][b.size()] / Math.min(a.size(), b.size());
    }
}
