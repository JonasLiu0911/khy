package com.example.ky.analysis.domain.vehicle.model;

import com.example.ky.analysis.domain.common.AbnormalLevel;
import com.example.ky.analysis.domain.common.EvidenceItem;

import java.util.List;

public record TrackAnalysisResult(
        int validTripCount,
        int clusterCount,
        double mainPatternRatio,
        double noiseTripRatio,
        double routeEntropy,
        double avgSimilarity,
        double odStability,
        double bindStationTripRatio,
        double trackScore,
        AbnormalLevel level,
        List<EvidenceItem> evidenceItems
) {}
