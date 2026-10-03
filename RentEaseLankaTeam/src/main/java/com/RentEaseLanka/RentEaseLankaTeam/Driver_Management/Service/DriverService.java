package com.RentEaseLanka.RentEaseLankaTeam.Driver_Management.Service;

import com.RentEaseLanka.RentEaseLankaTeam.Booking_Management.Model.BookingStatus;
import com.RentEaseLanka.RentEaseLankaTeam.Booking_Management.Model.DTOs.BookingDetailsDTO;
import com.RentEaseLanka.RentEaseLankaTeam.Driver_Management.Model.DriverProfileDTO;
import com.RentEaseLanka.RentEaseLankaTeam.Driver_Management.Model.DriverTripDTO;
import com.RentEaseLanka.RentEaseLankaTeam.Driver_Management.Model.UserSummaryDTO;
import com.RentEaseLanka.RentEaseLankaTeam.User_Management.DTOs.CreateUserRequest;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public interface DriverService {
    UserSummaryDTO createDriver(CreateUserRequest request);

    List<UserSummaryDTO> getAvailableDrivers(LocalDateTime pickupDateTime, LocalDateTime returnDateTime);

    List<UserSummaryDTO> getAvailableDrivers(LocalDateTime pickupDateTime, LocalDateTime returnDateTime, Long bookingId);

    BookingDetailsDTO assignDriver(Long bookingId, Long driverId);

    DriverProfileDTO getDriverProfile(String email);

    List<DriverTripDTO> getDriverTrips(String email, BookingStatus status);

    List<DriverTripDTO> getAssignedBookingsForDriver(Long driverId);
}
