package com.example.ky.analysis.domain.common;

public record EvidenceItem(
        String name,
        String value,
        String threshold,
        String conclusion
) {}
