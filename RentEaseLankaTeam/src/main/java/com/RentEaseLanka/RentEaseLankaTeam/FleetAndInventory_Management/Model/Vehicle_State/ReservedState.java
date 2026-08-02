package com.RentEaseLanka.RentEaseLankaTeam.FleetAndInventory_Management.Model.Vehicle_State;

import com.RentEaseLanka.RentEaseLankaTeam.FleetAndInventory_Management.Model.Vehicle;
import com.RentEaseLanka.RentEaseLankaTeam.FleetAndInventory_Management.Model.VehicleStatus;

public class ReservedState implements VehicleState{
    @Override
    public void reserve(Vehicle vehicle) {
        throw new IllegalStateException("Validation Error: Vehicle is already reserved by a customer.");
    }

    @Override
    public void rent(Vehicle vehicle) {
        System.out.println("Processing rental for the reserved vehicle...");
        // Transitions to Rented
        vehicle.changeState(VehicleStatus.RENTED, new RentedState());
    }

    @Override
    public void returnVehicle(Vehicle vehicle) {
        throw new IllegalStateException("Validation Error: Cannot return a vehicle that is only reserved, not rented.");
    }

    @Override
    public void scheduleMaintenance(Vehicle vehicle) {
        throw new IllegalStateException("Validation Error: Cannot schedule maintenance for a reserved vehicle. Cancel reservation first.");
    }

    @Override
    public void makeAvailable(Vehicle vehicle) {
        System.out.println("Reservation cancelled. Vehicle is now available.");
        // Transitions back to Available
        vehicle.changeState(VehicleStatus.AVAILABLE, new AvailableState());
    }
}
