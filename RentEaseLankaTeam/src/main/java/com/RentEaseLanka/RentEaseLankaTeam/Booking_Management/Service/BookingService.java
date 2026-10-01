package com.RentEaseLanka.RentEaseLankaTeam.Booking_Management.Service;

import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Model.Vehicle;

public interface BookingService {

     boolean checkVehicleAvailability(Vehicle vehicle);
}
