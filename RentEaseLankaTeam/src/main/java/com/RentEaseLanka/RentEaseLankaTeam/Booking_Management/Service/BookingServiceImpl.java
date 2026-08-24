package com.RentEaseLanka.RentEaseLankaTeam.Booking_Management.Service;

import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Model.Vehicle;
import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Model.VehicleStatus;
import org.springframework.stereotype.Service;

@Service
public class BookingServiceImpl implements BookingService {
    @Override
    public boolean checkVehicleAvailability(Vehicle vehicle) {
        return vehicle.getStatus() == VehicleStatus.AVAILABLE;
    }
}
