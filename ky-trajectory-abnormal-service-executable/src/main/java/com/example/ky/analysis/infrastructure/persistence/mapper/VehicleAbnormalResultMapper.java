package com.example.ky.analysis.infrastructure.persistence.mapper;

import com.example.ky.analysis.domain.common.AbnormalLevel;
import com.example.ky.analysis.domain.result.model.VehicleAbnormalResult;
import com.example.ky.analysis.domain.station.model.StationEventScoreResult;
import com.example.ky.analysis.domain.vehicle.model.TrackAnalysisResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class VehicleAbnormalResultMapper {
    private final ObjectMapper objectMapper;

    public VehicleAbnormalResultMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String toEvidenceJson(VehicleAbnormalResult result) {
        if (result == null) {
            return null;
        }
        Map<String, Object> payload = new HashMap<>();
        payload.put("trackAnalysis", result.getTrackAnalysis());
        payload.put("stationEventAnalysis", result.getStationEventAnalysis());
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (Exception ex) {
            return null;
        }
    }

    public VehicleAbnormalResult fromRow(ResultSet rs) throws SQLException {
        VehicleAbnormalResult result = new VehicleAbnormalResult();
        result.setStatDate(toLocalDate(rs.getDate("stat_date")));
        result.setWindowStart(toLocalDateTime(rs.getTimestamp("window_start")));
        result.setWindowEnd(toLocalDateTime(rs.getTimestamp("window_end")));
        result.setVehicleId(rs.getString("vehicle_id"));
        result.setPlateNo(rs.getString("plate_no"));
        result.setLineId(rs.getString("line_id"));
        result.setLineName(rs.getString("line_name"));
        result.setTrackScore(rs.getDouble("track_score"));
        result.setEventScore(rs.getDouble("event_score"));
        result.setFinalScore(rs.getDouble("final_score"));
        String level = rs.getString("abnormal_level");
        result.setAbnormalLevel(level == null ? null : AbnormalLevel.valueOf(level));
        result.setConflictFlag(rs.getBoolean("conflict_flag"));
        result.setEvidenceSummary(rs.getString("evidence_summary"));
        result.setSuggestion(rs.getString("suggestion"));
        result.setAlgorithmVersion(rs.getString("algorithm_version"));
        applyEvidenceJson(result, rs.getString("evidence_json"));
        return result;
    }

    private void applyEvidenceJson(VehicleAbnormalResult result, String json) {
        if (json == null || json.isBlank()) {
            return;
        }
        try {
            JsonNode node = objectMapper.readTree(json);
            JsonNode trackNode = node.get("trackAnalysis");
            if (trackNode != null && !trackNode.isNull()) {
                TrackAnalysisResult track = objectMapper.treeToValue(trackNode, TrackAnalysisResult.class);
                result.setTrackAnalysis(track);
            }
            JsonNode stationNode = node.get("stationEventAnalysis");
            if (stationNode != null && !stationNode.isNull()) {
                StationEventScoreResult station = objectMapper.treeToValue(stationNode, StationEventScoreResult.class);
                result.setStationEventAnalysis(station);
            }
        } catch (Exception ex) {
            // ignore malformed evidence payload
        }
    }

    private LocalDate toLocalDate(java.sql.Date date) {
        return date == null ? null : date.toLocalDate();
    }

    private java.time.LocalDateTime toLocalDateTime(Timestamp ts) {
        return ts == null ? null : ts.toLocalDateTime();
    }
}
