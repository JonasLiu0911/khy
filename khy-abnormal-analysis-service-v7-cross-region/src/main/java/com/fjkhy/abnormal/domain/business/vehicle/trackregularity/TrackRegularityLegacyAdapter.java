package com.fjkhy.abnormal.domain.business.vehicle.trackregularity;

import java.util.List;
import java.util.Map;

public interface TrackRegularityLegacyAdapter {
    Map<String, Object> analyzeMonthlyTrackRegularity(String vehicleId, String period, List<Map<String, Object>> trackPoints);
}
