package com.RentEaseLanka.RentEaseLankaTeam.FleetAndInventory_Management.Model.Vehicle_State;

import com.RentEaseLanka.RentEaseLankaTeam.FleetAndInventory_Management.Model.Vehicle;

public interface VehicleState {
    void reserve(Vehicle vehicle);
    void rent(Vehicle vehicle);
    void returnVehicle(Vehicle vehicle);
    void scheduleMaintenance(Vehicle vehicle);
    void makeAvailable(Vehicle vehicle);
}
