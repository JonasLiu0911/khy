package com.example.ky.analysis.infrastructure.external.prod;

import com.example.ky.analysis.domain.task.AnalysisVehicle;
import com.example.ky.analysis.domain.vehicle.model.VehicleBindingLine;
import com.example.ky.analysis.domain.vehicle.port.VehicleDataPort;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

@Component
@Profile("prod")
public class RealVehicleDataAdapter implements VehicleDataPort {
    private final JdbcTemplate jdbcTemplate;

    public RealVehicleDataAdapter(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<String> queryActiveVehicleIds() {
        String sql = "select distinct vehicle_id from analysis_vehicle";
        return jdbcTemplate.query(sql, (rs, rowNum) -> rs.getString("vehicle_id"));
    } // v_khy_algo_vehicle_line_station

    @Override
    public String queryPlateNo(String vehicleId) {
        String sql = "select plate_no from v_khy_algo_vehicle_line_station where vehicle_id = ? "
                + "order by bind_start_date desc limit 1";
        List<String> result = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> rs.getString("plate_no"),
                vehicleId
        );

        if (!result.isEmpty() && result.get(0) != null && !result.get(0).trim().isEmpty()) {
            return result.get(0);
        }

        String carSql = "select license_plate_num from khy_car where id = ? limit 1";
        List<String> carResult = jdbcTemplate.query(
                carSql,
                (rs, rowNum) -> rs.getString("license_plate_num"),
                vehicleId
        );

        if (!carResult.isEmpty() && carResult.get(0) != null && !carResult.get(0).trim().isEmpty()) {
            return carResult.get(0);
        }

        return vehicleId;
    }

    @Override
    public VehicleBindingLine queryBindingLine(String vehicleId) {
        String sql = "select line_id, bind_start_date, bind_end_date "
                + "from v_khy_algo_vehicle_line_station where vehicle_id = ? "
                + "order by bind_start_date desc limit 1";
        List<VehicleBindingLine> result = jdbcTemplate.query(sql, (rs, rowNum) -> {
            String lineId = rs.getString("line_id");
            Date start = rs.getDate("bind_start_date");
            Date end = rs.getDate("bind_end_date");
            LocalDate startDate = start == null ? null : start.toLocalDate();
            LocalDate endDate = end == null ? null : end.toLocalDate();
            return new VehicleBindingLine(vehicleId, lineId, startDate, endDate);
        }, vehicleId);
        return result.isEmpty() ? null : result.get(0);
    }

    @Override
    public List<AnalysisVehicle> queryAnalysisVehicles() {
        String sql = "select p.vehicle_id, c.license_plate_num, count(*) as cnt "
                + "from vehicle_gps_points_511 p "
                + "left join khy_car c on c.id = p.vehicle_id "
                + "group by p.vehicle_id, c.license_plate_num";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            AnalysisVehicle v = new AnalysisVehicle();
            v.setVehicleId(rs.getString("vehicle_id"));
            v.setPlateNo(rs.getString("license_plate_num"));
            v.setVehicleCount(rs.getInt("cnt"));
            return v;
        });
    }

    @Override
    public void saveVehicles(List<AnalysisVehicle> vehicles) {
        jdbcTemplate.execute("truncate table analysis_vehicle");
        String sql = "insert into analysis_vehicle (vehicle_id, plate_no, vehicle_count) values (?, ?, ?)";
        for (AnalysisVehicle vehicle : vehicles) {
            jdbcTemplate.update(sql,
                    Long.valueOf(vehicle.getVehicleId()),
                    vehicle.getPlateNo(),
                    vehicle.getVehicleCount()
            );
        }
    }
}
