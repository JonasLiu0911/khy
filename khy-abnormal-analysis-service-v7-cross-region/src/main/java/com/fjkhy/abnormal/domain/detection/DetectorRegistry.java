package com.fjkhy.abnormal.domain.detection;

import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class DetectorRegistry {
    private final Map<String, AbnormalDetector> detectorMap;

    public DetectorRegistry(List<AbnormalDetector> detectors) {
        this.detectorMap = detectors.stream().collect(Collectors.toMap(AbnormalDetector::detectorCode, Function.identity()));
    }

    public AbnormalDetector getRequired(String detectorCode) {
        AbnormalDetector detector = detectorMap.get(detectorCode);
        if (detector == null) {
            throw new IllegalArgumentException("未找到检测器：" + detectorCode);
        }
        return detector;
    }

    public List<DetectorDescriptor> descriptors() {
        return detectorMap.values().stream()
                .map(d -> new DetectorDescriptor(d.detectorCode(), d.detectorName(), d.objectTypeName(), d.abnormalCategory(), d.abnormalSubtype(), "已注册"))
                .sorted(Comparator.comparing(DetectorDescriptor::detectorCode))
                .toList();
    }
}
