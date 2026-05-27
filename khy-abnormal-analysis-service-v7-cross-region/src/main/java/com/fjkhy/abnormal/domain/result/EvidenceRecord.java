package com.fjkhy.abnormal.domain.result;

import java.time.LocalDateTime;
import java.util.Map;

public record EvidenceRecord(
        String evidenceId,
        String abnormalId,
        String evidenceType,
        String title,
        String summary,
        Map<String, Object> payload,
        LocalDateTime generatedTime
) {}
