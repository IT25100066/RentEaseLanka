package com.RentEaseLanka.RentEaseLankaTeam.Booking_Management.Service;

import java.math.BigDecimal;
import java.util.List;

import com.RentEaseLanka.RentEaseLankaTeam.Booking_Management.Model.DTOs.BookingDetailsDTO;
import com.RentEaseLanka.RentEaseLankaTeam.Booking_Management.Model.DTOs.BookingRequestDTO;
import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Model.Vehicle;
import com.RentEaseLanka.RentEaseLankaTeam.User_Management.Model.User;

public interface BookingService {

     boolean checkVehicleAvailability(Vehicle vehicle);

     BookingDetailsDTO createBooking(BookingRequestDTO bookingRequestDTO, User user);

     boolean cancelBooking(Long bookingId);

     BookingDetailsDTO viewBookingDetails(Long bookingId);

     boolean makeConfirmed(Long bookingId);

     boolean makeActive(Long bookingId);

     boolean makeCompleted(Long bookingId);

     List<BookingDetailsDTO> getAllBookings();

     BigDecimal calculateTotalPrice(BookingRequestDTO dto);

     List<BookingDetailsDTO> getBookingForCustomer(String email);

     List<BookingDetailsDTO> getBookingsForCustomer(Long customerId);

     boolean checkVehicleAvailabilityForBooking(BookingRequestDTO dto);

}
