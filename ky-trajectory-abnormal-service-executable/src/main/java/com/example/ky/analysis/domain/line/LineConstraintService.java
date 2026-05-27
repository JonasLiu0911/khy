package com.example.ky.analysis.domain.line;

import com.example.ky.analysis.domain.line.model.LineConstraint;
import com.example.ky.analysis.domain.line.model.LineStation;
import com.example.ky.analysis.domain.vehicle.model.VehicleBindingLine;
import com.example.ky.analysis.domain.vehicle.port.VehicleDataPort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LineConstraintService {
    private final VehicleDataPort vehicleDataPort;
    private final LineStationService lineStationService;

    public LineConstraintService(VehicleDataPort vehicleDataPort, LineStationService lineStationService) {
        this.vehicleDataPort = vehicleDataPort;
        this.lineStationService = lineStationService;
    }

    public LineConstraint buildConstraint(String vehicleId) {
        VehicleBindingLine binding = vehicleDataPort.queryBindingLine(vehicleId);
        if (binding == null) {
            return new LineConstraint(null, null, List.of());
        }
        List<LineStation> stations = lineStationService.queryStations(binding.lineId());
        String lineName = stations.isEmpty() ? binding.lineId() : stations.get(0).lineName();
        return new LineConstraint(binding.lineId(), lineName, stations);
    }
}
