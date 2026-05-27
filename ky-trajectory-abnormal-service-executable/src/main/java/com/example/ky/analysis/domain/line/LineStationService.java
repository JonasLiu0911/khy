package com.example.ky.analysis.domain.line;

import com.example.ky.analysis.domain.line.model.LineStation;
import com.example.ky.analysis.domain.line.port.LineStationDataPort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LineStationService {
    private final LineStationDataPort port;

    public LineStationService(LineStationDataPort port) {
        this.port = port;
    }

    public List<LineStation> queryStations(String lineId) {
        return port.queryLineStationsForAlg(lineId);
    }
}
