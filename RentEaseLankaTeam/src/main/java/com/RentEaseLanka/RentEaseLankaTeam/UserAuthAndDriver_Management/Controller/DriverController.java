package com.RentEaseLanka.RentEaseLankaTeam.UserAuthAndDriver_Management.Controller;

import com.RentEaseLanka.RentEaseLankaTeam.UserAuthAndDriver_Management.Model.AvailabilityStatus;
import com.RentEaseLanka.RentEaseLankaTeam.UserAuthAndDriver_Management.Model.DriverProfile;
import com.RentEaseLanka.RentEaseLankaTeam.UserAuthAndDriver_Management.Model.DriverRegistrationDTO;
import com.RentEaseLanka.RentEaseLankaTeam.UserAuthAndDriver_Management.Service.AuthAndDriverService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/drivers")
@CrossOrigin(origins = "*")
public class DriverController {

    @Autowired
    private AuthAndDriverService driverService;

    // POST: Create driver profile details
    @PostMapping("/profile")
    public ResponseEntity<?> createDriverProfile(@RequestBody DriverRegistrationDTO dto) {
        try {
            DriverProfile profile = driverService.createDriverProfile(dto);
            return ResponseEntity.ok(profile);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // GET: Fetch available drivers for assignment
    @GetMapping("/available")
    public ResponseEntity<List<DriverProfile>> getAvailableDrivers() {
        List<DriverProfile> availableDrivers = driverService.getAvailableDrivers();
        return ResponseEntity.ok(availableDrivers);
    }

    // PUT: Update driver status (AVAILABLE, ON_TRIP, OFF_DUTY)
    @PutMapping("/{driverId}/status")
    public ResponseEntity<?> updateDriverStatus(@PathVariable Long driverId, @RequestParam AvailabilityStatus status) {
        try {
            DriverProfile updatedDriver = driverService.updateDriverStatus(driverId, status);
            return ResponseEntity.ok(updatedDriver);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
