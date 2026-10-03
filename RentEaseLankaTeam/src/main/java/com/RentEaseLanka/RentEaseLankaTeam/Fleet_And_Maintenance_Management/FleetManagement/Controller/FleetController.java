package com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Controller;

import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Model.DTOs.UpdateVehicleRequestDTO;
import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Model.DTOs.VehicleCreateRequestDTO;
import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Model.DTOs.VehicleResponseDTO;
import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Service.FleetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class FleetController {

    @Autowired
    private FleetService fleetService;

    @PostMapping(value = "/vehicle/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<String> addVehicle(@Valid @ModelAttribute VehicleCreateRequestDTO newVehicle) {

        try {

            fleetService.createVehicleWithImages(newVehicle);
            return new ResponseEntity<>("Vehicle created Successfully", HttpStatus.CREATED);

        } catch (IllegalArgumentException e) {
            // Catch your specific validation error and send a 400
            log.warn("Validation failed: {}", e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            // Catch unexpected server errors, log the stack trace, and send a 500
            log.error("Failed to create vehicle", e);
            return new ResponseEntity<>("Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @PostMapping(value = {"/vehicle/{id}/images", "/manager/vehicle/{id}/images"}, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<?> addVehicleImages(
            @PathVariable("id") Long id,
            @RequestParam("images") List<MultipartFile> images) {
        try {
            if (images == null || images.isEmpty()) {
                return new ResponseEntity<>("Please select at least one image to upload", HttpStatus.BAD_REQUEST);
            }
            List<String> uploadedPaths = fleetService.addVehicleImages(id, images);
            return new ResponseEntity<>(uploadedPaths, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            log.warn("Validation failed adding vehicle images: {}", e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (RuntimeException e) {
            log.error("Error adding vehicle images: {}", e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            log.error("Failed to add vehicle images", e);
            return new ResponseEntity<>("Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // GET all vehicles with their image URLs
    @GetMapping("/vehicles")
    public ResponseEntity<List<VehicleResponseDTO>> getAllVehicles() {
        try {
            List<VehicleResponseDTO> vehicles = fleetService.getAllVehicles();
            return new ResponseEntity<>(vehicles, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Failed to get all vehicles: ", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // GET a single vehicle by ID with its image URLs
    @GetMapping("/vehicle/{id}")
    public ResponseEntity<?> getVehicleById(@PathVariable Long id) {
        try {
            VehicleResponseDTO vehicle = fleetService.getVehicleById(id);
            return new ResponseEntity<>(vehicle, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/vehicle/update")
    public ResponseEntity<?> updateVehicle(@RequestBody UpdateVehicleRequestDTO dto) {

        boolean updated = fleetService.updateVehicle(dto);
        if (updated) {
            return new ResponseEntity<>("Vehicle Updated Successfully", HttpStatus.ACCEPTED);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

    }

    @GetMapping("/pendingVehicles")
    public ResponseEntity<List<VehicleResponseDTO>> pendingVehicles() {
        try {
            List<VehicleResponseDTO> vehicles = fleetService.pendingVehicles();
            if (vehicles.isEmpty()) {
                log.info("No pending vehicles present");
            }
            return new ResponseEntity<>(vehicles, HttpStatus.OK);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @PostMapping("/manager/vehicle/accept")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<String> acceptPendingVehicles(@RequestBody List<Long> ids) {
        try {
            log.info("Manager accepting vehicles with IDs: {}", ids);

            boolean done = fleetService.makeAvailable(ids);
            if (done) {
                return new ResponseEntity<>("Vehicles accepted successfully", HttpStatus.OK);
            }
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @GetMapping("/vehicle")
    public ResponseEntity<?> GetVehicleByFilter(@RequestParam("filterBy") String filter,
            @Nullable @RequestParam("category") String category,
            @Nullable @RequestParam("status") String status) {

        String filterType = filter.toLowerCase();
        try {
            List<VehicleResponseDTO> vehicles = fleetService.getVehiclesByFilter(filterType, category, status);
            return new ResponseEntity<>(vehicles, HttpStatus.OK);

        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }

    }

    @DeleteMapping("/vehicle/{id}/remove")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<?> setVehicleToOutOfService(@PathVariable Long id) {
        try {
            boolean outOfService = fleetService.setVehicleToOutOfService(id);
            if (outOfService) {
                return new ResponseEntity<>("Vehicle is set to Out Of Service", HttpStatus.OK);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
        return null;
    }

    @DeleteMapping("/vehicle/{id}/delete")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<?> deleteVehicle(@PathVariable Long id) {
        try {
            boolean deleted = fleetService.deleteVehicle(id);
            if (deleted) {
                return new ResponseEntity<>("Vehicle deleted Successfully", HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Vehicle could not be deleted", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

}
