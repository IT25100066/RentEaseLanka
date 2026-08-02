package com.RentEaseLanka.RentEaseLankaTeam.FleetAndInventory_Management.Model.Vehicle_State;

import com.RentEaseLanka.RentEaseLankaTeam.FleetAndInventory_Management.Model.Vehicle;
import com.RentEaseLanka.RentEaseLankaTeam.FleetAndInventory_Management.Model.VehicleStatus;

public class AvailableState implements VehicleState {

    @Override
    public void reserve(Vehicle vehicle) {
        System.out.println("Vehicle reserved successfully.");
        vehicle.changeState(VehicleStatus.RESERVED, new ReservedState());
    }

    @Override
    public void rent(Vehicle vehicle) {
        System.out.println("Vehicle rented successfully.");
        vehicle.changeState(VehicleStatus.RENTED, new RentedState());
    }

    @Override
    public void returnVehicle(Vehicle vehicle) {
        throw new IllegalStateException("Validation Error: Cannot return a vehicle that is already available in the lot.");
    }

    @Override
    public void scheduleMaintenance(Vehicle vehicle) {
        System.out.println("Dispatching vehicle to maintenance garage.");
        vehicle.changeState(VehicleStatus.UNDER_MAINTENANCE, new MaintenanceState());
    }

    @Override
    public void makeAvailable(Vehicle vehicle) {
        throw new IllegalStateException("Validation Error: Vehicle is already marked as available.");
    }
}
