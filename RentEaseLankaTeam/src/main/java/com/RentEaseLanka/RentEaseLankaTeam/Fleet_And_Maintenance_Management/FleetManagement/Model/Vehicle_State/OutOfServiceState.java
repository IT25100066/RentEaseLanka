package com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Model.Vehicle_State;

import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Model.Vehicle;
import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Model.VehicleStatus;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OutOfServiceState implements VehicleState{
    @Override
    public void reserve(Vehicle vehicle) {
        log.error("Validation Error : Vehicle Cannot be reserved as it is in Out of Service");
        throw new IllegalStateException("Validation Error : Vehicle Cannot be reserved as it is in Out of Service");
    }

    @Override
    public void rent(Vehicle vehicle) {
        log.error("Validation Error : Vehicle Cannot be Rent as it is in Out of Service");
        throw new IllegalStateException("Validation Error : Vehicle Cannot be Rent as it is in Out of Service");
    }

    @Override
    public void returnVehicle(Vehicle vehicle) {
        log.error("Validation Error : Vehicle Cannot be Return as it is in Out of Service");
        throw new IllegalStateException("Validation Error : Vehicle Cannot be return as it is in Out of Service");
    }

    @Override
    public void scheduleMaintenance(Vehicle vehicle) {
        log.error("Validation Error : Vehicle Cannot be Scheduled to maintenance as it is in Out of Service");
        throw new IllegalStateException("Validation Error : Vehicle Cannot be maintenance as it is in Out of Service");
    }

    @Override
    public void makeAvailable(Vehicle vehicle) {
        log.info("Vehicle is available now ");
        vehicle.changeState(VehicleStatus.AVAILABLE, new AvailableState());
    }

    @Override
    public void outOfService(Vehicle vehicle) {
        log.error("Validation Error : Vehicle is already  in Out of Service");
        throw new IllegalStateException("Validation Error : Vehicle is already in Out of Service");
    }

    @Override
    public void pendingState(Vehicle vehicle) {
        throw new IllegalStateException("Validation Error : Vehicle is in out of service");

    }
}
