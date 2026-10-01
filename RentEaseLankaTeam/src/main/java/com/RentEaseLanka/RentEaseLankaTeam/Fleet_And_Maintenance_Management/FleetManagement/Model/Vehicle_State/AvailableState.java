package com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Model.Vehicle_State;

import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Model.Vehicle;
import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Model.VehicleStatus;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AvailableState implements VehicleState {

    @Override
    public void reserve(Vehicle vehicle) {
        log.info("Vehicle reserved successfully.");
        vehicle.changeState(VehicleStatus.RESERVED, new ReservedState());
    }

    @Override
    public void rent(Vehicle vehicle) {
        log.info("Vehicle rented successfully.");
        vehicle.changeState(VehicleStatus.RENTED, new RentedState());
    }

    @Override
    public void returnVehicle(Vehicle vehicle) {
        log.error("Validation Error: Cannot return a vehicle that is already available in the lot.");
        throw new IllegalStateException("Validation Error: Cannot return a vehicle that is already available in the lot.");
    }

    @Override
    public void scheduleMaintenance(Vehicle vehicle) {
        log.info("Dispatching vehicle to maintenance garage.");
        vehicle.changeState(VehicleStatus.UNDER_MAINTENANCE, new MaintenanceState());
    }

    @Override
    public void makeAvailable(Vehicle vehicle) {
        log.error("Validation Error: Vehicle is already marked as available.");
        throw new IllegalStateException("Validation Error: Vehicle is already marked as available.");
    }

    @Override
    public void outOfService(Vehicle vehicle) {
        log.info("Vehicle is set to Out Of Service");
        vehicle.changeState(VehicleStatus.OUT_OF_SERVICE, new OutOfServiceState());
    }

    @Override
    public void pendingState(Vehicle vehicle) {
        throw new IllegalStateException("Validation Error: Vehicle is already marked as available.");

    }
}
