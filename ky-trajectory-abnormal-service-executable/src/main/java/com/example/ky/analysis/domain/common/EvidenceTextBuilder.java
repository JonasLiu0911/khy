package com.example.ky.analysis.domain.common;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class EvidenceTextBuilder {
    public String build(List<EvidenceItem> items) {
        if (items == null || items.isEmpty()) {
            return "暂无明显证据。";
        }
        return items.stream()
                .map(i -> i.name() + "为" + i.value() + "，参考阈值为" + i.threshold() + "，" + i.conclusion())
                .collect(Collectors.joining("；"));
    }
}
