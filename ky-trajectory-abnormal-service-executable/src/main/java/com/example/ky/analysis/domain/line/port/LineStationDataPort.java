package com.example.ky.analysis.domain.line.port;

import com.example.ky.analysis.domain.line.model.LineStation;

import java.util.List;

public interface LineStationDataPort {
    List<LineStation> queryLineStationsForAlg(String lineId);
    List<LineStation> queryLineStationsForVisualize(String lineId);
}
