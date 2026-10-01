package com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Model.Vehicle_State;

import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Model.Vehicle;
import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Model.VehicleStatus;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ReservedState implements VehicleState {
    @Override
    public void reserve(Vehicle vehicle) {
        throw new IllegalStateException("Validation Error: Vehicle is already reserved by a customer.");
    }

    @Override
    public void rent(Vehicle vehicle) {
        log.info("Processing rental for the reserved vehicle...");
        // Transitions to Rented
        vehicle.changeState(VehicleStatus.RENTED, new RentedState());
    }

    @Override
    public void returnVehicle(Vehicle vehicle) {
        throw new IllegalStateException("Validation Error: Cannot return a vehicle that is only reserved, not rented.");
    }

    @Override
    public void scheduleMaintenance(Vehicle vehicle) {
        throw new IllegalStateException(
                "Validation Error: Cannot schedule maintenance for a reserved vehicle. Cancel reservation first.");
    }

    @Override
    public void makeAvailable(Vehicle vehicle) {
        log.info("Reservation cancelled. Vehicle is now available.");
        // Transitions back to Available
        vehicle.changeState(VehicleStatus.AVAILABLE, new AvailableState());
    }

    @Override
    public void outOfService(Vehicle vehicle) {
        throw new IllegalStateException("Validation Error: Cannot set to out of service as the vehicle is reserved ");

    }

    @Override
    public void pendingState(Vehicle vehicle) {
        throw new IllegalStateException("Validation Error: Cannot set to pending as the vehicle is reserved ");

    }
}
