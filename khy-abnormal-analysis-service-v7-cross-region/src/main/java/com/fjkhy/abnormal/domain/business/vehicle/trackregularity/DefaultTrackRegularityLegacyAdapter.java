package com.fjkhy.abnormal.domain.business.vehicle.trackregularity;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class DefaultTrackRegularityLegacyAdapter implements TrackRegularityLegacyAdapter {
    @Override
    public Map<String, Object> analyzeMonthlyTrackRegularity(String vehicleId, String period, List<Map<String, Object>> trackPoints) {
        return Map.of(
                "vehicleId", vehicleId,
                "period", period,
                "regularityScore", 0.42,
                "conclusion", "示例：月度轨迹重复性偏低。学生应在此接入原轨迹清洗、行程切分、特征构建和规律性评分代码。"
        );
    }
}
