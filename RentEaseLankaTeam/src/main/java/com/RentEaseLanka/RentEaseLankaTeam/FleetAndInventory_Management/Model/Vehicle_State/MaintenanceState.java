package com.RentEaseLanka.RentEaseLankaTeam.FleetAndInventory_Management.Model.Vehicle_State;

import com.RentEaseLanka.RentEaseLankaTeam.FleetAndInventory_Management.Model.Vehicle;
import com.RentEaseLanka.RentEaseLankaTeam.FleetAndInventory_Management.Model.VehicleStatus;

public class MaintenanceState implements VehicleState{
    @Override
    public void reserve(Vehicle vehicle) {
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
        System.out.println("Maintenance complete. Vehicle is cleared for use.");
        vehicle.changeState(VehicleStatus.AVAILABLE, new AvailableState());
    }
}
