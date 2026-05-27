package com.example.ky.analysis.domain.vehicle.algorithm;

import com.example.ky.analysis.domain.config.TrajectoryAnalysisConfig;
import com.example.ky.analysis.domain.vehicle.model.TrajectoryCluster;
import com.example.ky.analysis.domain.vehicle.model.TrajectoryFeature;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

@Component
public class TrajectoryClusterAlgorithm {
    public List<TrajectoryCluster> cluster(double[][] similarityMatrix, List<TrajectoryFeature> features, TrajectoryAnalysisConfig config) {
        int n = features.size();
        boolean[] visited = new boolean[n];
        List<TrajectoryCluster> clusters = new ArrayList<>();
        int clusterNo = 1;
        for (int i = 0; i < n; i++) {
            if (visited[i]) {
                continue;
            }
            List<Integer> members = new ArrayList<>();
            Queue<Integer> queue = new LinkedList<>();
            queue.add(i);
            visited[i] = true;
            while (!queue.isEmpty()) {
                int idx = queue.poll();
                members.add(idx);
                for (int j = 0; j < n; j++) {
                    if (!visited[j] && similarityMatrix[idx][j] >= config.getClusterSimilarityThreshold()) {
                        visited[j] = true;
                        queue.add(j);
                    }
                }
            }
            boolean noise = members.size() == 1;
            List<String> tripIds = members.stream().map(k -> features.get(k).tripId()).toList();
            clusters.add(new TrajectoryCluster((noise ? "N" : "C") + clusterNo++, tripIds, noise));
        }
        return clusters;
    }
}
