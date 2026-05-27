package com.fjkhy.abnormal.domain.business.vehicle.trackstationconsistency;

import java.util.List;
import java.util.Map;

public interface TrackStationConsistencyLegacyAdapter {
    Map<String, Object> compareTrackAndStationEvents(String vehicleId, String period,
                                                      List<Map<String, Object>> trackPoints,
                                                      List<Map<String, Object>> stationEvents);
}
