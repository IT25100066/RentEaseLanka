package com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Model.Vehicle_State;

import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Model.Vehicle;
import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Model.VehicleStatus;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PendingState implements VehicleState{
    @Override
    public void reserve(Vehicle vehicle) {
        log.error("Validation Error : Vehicle Cannot be reserved as it is in Pending");
        throw new IllegalStateException("Validation Error : Vehicle Cannot be reserved as it is in Pending");
    }

    @Override
    public void rent(Vehicle vehicle) {
        log.error("Validation Error : Vehicle Cannot be Rented as it is in Pending");
        throw new IllegalStateException("Validation Error : Vehicle Cannot be rented as it is in Pending");

    }

    @Override
    public void returnVehicle(Vehicle vehicle) {
        log.error("Validation Error : Vehicle Cannot be returned as it is in Pending");
        throw new IllegalStateException("Validation Error : Vehicle Cannot be returned as it is in Pending");
    }

    @Override
    public void scheduleMaintenance(Vehicle vehicle) {
        log.error("Validation Error : Vehicle Cannot be scheduled as it is in Pending");
        throw new IllegalStateException("Validation Error : Vehicle Cannot be scheduled as it is in Pending");
    }

    @Override
    public void makeAvailable(Vehicle vehicle) {
        log.info(" Vehicle is cleared for use.");
        vehicle.changeState(VehicleStatus.AVAILABLE, new AvailableState());
    }

    @Override
    public void outOfService(Vehicle vehicle) {
        log.error("Validation Error : Vehicle Cannot be out of service because it  is in Pending");
        throw new IllegalStateException("Validation Error : Vehicle  is in Pending");

    }

    @Override
    public void pendingState(Vehicle vehicle) {
        throw new IllegalStateException("Validation Error : Vehicle  is already in Pending");
    }
}
