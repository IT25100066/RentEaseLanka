package com.RentEaseLanka.RentEaseLankaTeam.UserAuthAndDriver_Management.Controller;

import com.RentEaseLanka.RentEaseLankaTeam.UserAuthAndDriver_Management.Model.AvailabilityStatus;
import com.RentEaseLanka.RentEaseLankaTeam.UserAuthAndDriver_Management.Model.DriverAssignmentDTO;
import com.RentEaseLanka.RentEaseLankaTeam.UserAuthAndDriver_Management.Model.DriverProfile;
import com.RentEaseLanka.RentEaseLankaTeam.UserAuthAndDriver_Management.Model.DriverRegistrationDTO;
import com.RentEaseLanka.RentEaseLankaTeam.UserAuthAndDriver_Management.Service.AuthAndDriverService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller handling Driver Management, Availability Queries, and Booking Assignment endpoints.
 * Base Path: /api/drivers
 */
@RestController
@RequestMapping("/api/drivers")
@CrossOrigin(origins = "*")
public class DriverController {

    @Autowired
    private AuthAndDriverService driverService;

    /**
     * POST: Register/Create driver profile details.
     * Endpoint: POST /api/drivers/register
     */
    @PostMapping("/register")
    public ResponseEntity<?> createDriverProfile(@RequestBody DriverRegistrationDTO dto) {
        try {
            // Service call creates driver profile linked to a user with ROLE_DRIVER
            DriverProfile profile = driverService.createDriverProfile(dto);
            return ResponseEntity.ok(profile);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * GET: Fetch available drivers for assignment.
     * Endpoint: GET /api/drivers/available
     */
    @GetMapping("/available")
    public ResponseEntity<List<DriverProfile>> getAvailableDrivers() {
        // Retrieves all drivers currently marked with status = AVAILABLE
        List<DriverProfile> availableDrivers = driverService.getAvailableDrivers();
        return ResponseEntity.ok(availableDrivers);
    }

    /**
     * POST: Assign an available driver to a customer booking.
     * Endpoint: POST /api/drivers/assign
     */
    @PostMapping("/assign")
    public ResponseEntity<?> assignDriver(@RequestBody DriverAssignmentDTO dto) {
        try {
            // Assigns driver to booking ID and transitions driver status to ON_TRIP
            DriverProfile updatedDriver = driverService.assignDriverToBooking(dto.getDriverId(), dto.getBookingId());
            return ResponseEntity.ok(updatedDriver);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * PUT: Manual update of driver availability status (AVAILABLE, ON_TRIP, OFF_DUTY, SUSPENDED).
     * Endpoint: PUT /api/drivers/{driverId}/status?status=AVAILABLE
     */
    @PutMapping("/{driverId}/status")
    public ResponseEntity<?> updateDriverStatus(@PathVariable Long driverId, @RequestParam AvailabilityStatus status) {
        try {
            // Updates status field directly in the DriverProfile repository
            DriverProfile updatedDriver = driverService.updateDriverStatus(driverId, status);
            return ResponseEntity.ok(updatedDriver);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * PUT: Driver updates trip progress status (e.g., STARTED, COMPLETED).
     * Endpoint: PUT /api/drivers/trips/{tripId}/status?driverId=X&status=COMPLETED
     */
    @PutMapping("/trips/{tripId}/status")
    public ResponseEntity<?> updateTripStatus(
            @PathVariable Long tripId,
            @RequestParam Long driverId,
            @RequestParam String status) {
        try {
            DriverProfile updatedDriver = driverService.updateTripStatus(driverId, status);
            return ResponseEntity.ok(updatedDriver);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}