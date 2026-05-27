package com.example.ky.analysis.domain.station.port;

import com.example.ky.analysis.domain.station.model.StationEvent;

import java.time.LocalDateTime;
import java.util.List;

public interface StationEventDataPort {
    List<StationEvent> queryStationEvents(String vehicleId, LocalDateTime startTime, LocalDateTime endTime);
}
