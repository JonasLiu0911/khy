package com.example.ky.analysis.domain.station.model;

import com.example.ky.analysis.domain.common.AbnormalLevel;
import com.example.ky.analysis.domain.common.EvidenceItem;

import java.util.List;

public record StationEventScoreResult(
        int eventCount,
        double bindStationEventRatio,
        double stationCoverRate,
        double setStability,
        double maxStationPatternRatio,
        double missingStationRatio,
        double eventScore,
        AbnormalLevel level,
        List<EvidenceItem> evidenceItems
) {}
