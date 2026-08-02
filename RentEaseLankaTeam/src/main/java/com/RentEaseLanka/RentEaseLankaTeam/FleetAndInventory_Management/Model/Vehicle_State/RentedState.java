package com.RentEaseLanka.RentEaseLankaTeam.FleetAndInventory_Management.Model.Vehicle_State;

import com.RentEaseLanka.RentEaseLankaTeam.FleetAndInventory_Management.Model.Vehicle;
import com.RentEaseLanka.RentEaseLankaTeam.FleetAndInventory_Management.Model.VehicleStatus;

public class RentedState implements VehicleState{
    @Override
    public void reserve(Vehicle vehicle) {
        throw new IllegalStateException("Validation Error: Cannot reserve a vehicle that is currently rented out.");
    }

    @Override
    public void rent(Vehicle vehicle) {
        throw new IllegalStateException("Validation Error: Vehicle is already rented out.");
    }

    @Override
    public void returnVehicle(Vehicle vehicle) {
        System.out.println("Vehicle returned by customer. Processing mileage and billing...");
        // Transitions back to Available (or Maintenance depending on your business rules)
        vehicle.changeState(VehicleStatus.AVAILABLE, new AvailableState());
    }

    @Override
    public void scheduleMaintenance(Vehicle vehicle) {
        throw new IllegalStateException("Validation Error: Cannot service a vehicle while it is in the customer's possession.");
    }

    @Override
    public void makeAvailable(Vehicle vehicle) {
        throw new IllegalStateException("Validation Error: Vehicle must be returned through the proper workflow before it can be made available.");
    }
}
