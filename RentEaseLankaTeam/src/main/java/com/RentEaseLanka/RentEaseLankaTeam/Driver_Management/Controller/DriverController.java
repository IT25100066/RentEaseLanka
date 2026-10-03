package com.RentEaseLanka.RentEaseLankaTeam.Driver_Management.Controller;

import com.RentEaseLanka.RentEaseLankaTeam.Booking_Management.Model.BookingStatus;
import com.RentEaseLanka.RentEaseLankaTeam.Booking_Management.Model.DTOs.BookingDetailsDTO;
import com.RentEaseLanka.RentEaseLankaTeam.Driver_Management.Model.AssignDriverRequest;
import com.RentEaseLanka.RentEaseLankaTeam.Driver_Management.Model.DriverProfileDTO;
import com.RentEaseLanka.RentEaseLankaTeam.Driver_Management.Model.DriverTripDTO;
import com.RentEaseLanka.RentEaseLankaTeam.Driver_Management.Model.UserSummaryDTO;
import com.RentEaseLanka.RentEaseLankaTeam.Driver_Management.Service.DriverService;
import com.RentEaseLanka.RentEaseLankaTeam.User_Management.DTOs.CreateUserRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DriverController {
    private final DriverService driverService;

    @PostMapping("/manager/drivers")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<UserSummaryDTO> createDriver(@RequestBody CreateUserRequest request) {
        return ResponseEntity.ok(driverService.createDriver(request));
    }

    @GetMapping("/booking/available-drivers")
    @PreAuthorize("hasAnyRole('STAFF', 'MANAGER', 'ADMIN')")
    public ResponseEntity<List<UserSummaryDTO>> getAvailableDrivers(
            @RequestParam LocalDateTime pickupDateTime,
            @RequestParam LocalDateTime returnDateTime,
            @RequestParam(required = false) Long bookingId) {
        return ResponseEntity.ok(driverService.getAvailableDrivers(pickupDateTime, returnDateTime, bookingId));
    }

    @PostMapping("/booking/{id}/assign-driver")
    @PreAuthorize("hasAnyRole('STAFF', 'MANAGER', 'ADMIN')")
    public ResponseEntity<BookingDetailsDTO> assignDriver(@PathVariable Long id,
                                                         @Valid @RequestBody AssignDriverRequest request) {
        return ResponseEntity.ok(driverService.assignDriver(id, request.getDriverId()));
    }

    @GetMapping("/driver/profile")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<DriverProfileDTO> getDriverProfile(Principal principal) {
        return ResponseEntity.ok(driverService.getDriverProfile(principal.getName()));
    }

    @GetMapping("/driver/my-bookings")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<List<DriverTripDTO>> getMyBookings(
            Principal principal,
            @RequestParam(required = false) BookingStatus status) {
        return ResponseEntity.ok(driverService.getDriverTrips(principal.getName(), status));
    }

    @GetMapping("/driver/{driverId}/bookings")
    @PreAuthorize("hasAnyRole('STAFF', 'MANAGER', 'ADMIN')")
    public ResponseEntity<List<DriverTripDTO>> getAssignedBookingsForDriver(@PathVariable Long driverId) {
        return ResponseEntity.ok(driverService.getAssignedBookingsForDriver(driverId));
    }
}
