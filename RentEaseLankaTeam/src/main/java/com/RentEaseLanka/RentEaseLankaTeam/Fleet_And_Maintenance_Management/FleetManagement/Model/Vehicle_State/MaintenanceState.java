package com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Model.Vehicle_State;

import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Model.Vehicle;
import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Model.VehicleStatus;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MaintenanceState implements VehicleState{
    @Override
    public void reserve(Vehicle vehicle) {
        log.error("Validation Error: Vehicle is currently under maintenance and cannot be reserved.");
        throw new IllegalStateException("Validation Error: Vehicle is currently under maintenance and cannot be reserved.");
    }

    @Override
    public void rent(Vehicle vehicle) {
        throw new IllegalStateException("Validation Error: Vehicle is currently under maintenance and cannot be rented.");
    }

    @Override
    public void returnVehicle(Vehicle vehicle) {
        throw new IllegalStateException("Validation Error: Vehicle is not rented out.");
    }

    @Override
    public void scheduleMaintenance(Vehicle vehicle) {
        throw new IllegalStateException("Validation Error: Vehicle is already undergoing maintenance.");
    }

    @Override
    public void makeAvailable(Vehicle vehicle) {
        log.info("Maintenance complete. Vehicle is cleared for use.");
        vehicle.changeState(VehicleStatus.AVAILABLE, new AvailableState());
    }
}
