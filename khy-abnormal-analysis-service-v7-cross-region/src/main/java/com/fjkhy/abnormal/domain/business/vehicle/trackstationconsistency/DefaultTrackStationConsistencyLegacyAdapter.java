package com.fjkhy.abnormal.domain.business.vehicle.trackstationconsistency;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class DefaultTrackStationConsistencyLegacyAdapter implements TrackStationConsistencyLegacyAdapter {
    @Override
    public Map<String, Object> compareTrackAndStationEvents(String vehicleId, String period,
                                                            List<Map<String, Object>> trackPoints,
                                                            List<Map<String, Object>> stationEvents) {
        return Map.of(
                "vehicleId", vehicleId,
                "period", period,
                "missingStationEventCount", 2,
                "trackWithoutEventCount", 1,
                "conclusion", "示例：轨迹到站证据与过站事件存在不一致。学生应在此接入原过站一致性与双证据融合逻辑。"
        );
    }
}
