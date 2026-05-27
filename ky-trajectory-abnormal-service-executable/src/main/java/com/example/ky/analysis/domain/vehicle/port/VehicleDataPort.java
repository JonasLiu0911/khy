package com.example.ky.analysis.domain.vehicle.port;

import com.example.ky.analysis.domain.task.AnalysisVehicle;
import com.example.ky.analysis.domain.vehicle.model.VehicleBindingLine;

import java.util.List;

public interface VehicleDataPort {
    List<String> queryActiveVehicleIds();
    String queryPlateNo(String vehicleId);
    VehicleBindingLine queryBindingLine(String vehicleId);
    List<AnalysisVehicle> queryAnalysisVehicles();
    void saveVehicles(List<AnalysisVehicle> vehicles);
}
