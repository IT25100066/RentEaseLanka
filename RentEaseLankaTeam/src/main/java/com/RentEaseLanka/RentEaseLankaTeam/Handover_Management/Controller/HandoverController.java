package com.RentEaseLanka.RentEaseLankaTeam.Handover_Management.Controller;

import com.RentEaseLanka.RentEaseLankaTeam.Handover_Management.Model.DTO.*;
import com.RentEaseLanka.RentEaseLankaTeam.Handover_Management.Service.HandoverService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/handovers")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN') or hasRole('STAFF') or hasRole('MANAGER')")
public class HandoverController {

    private final HandoverService handoverService;

    // CREATE: Initiate a vehicle handover
    @PostMapping
    public ResponseEntity<HandoverResponseDTO> initiateHandover(@Valid @RequestBody HandoverCreateRequestDTO dto) {
        log.info("REST request to initiate handover for vehicle ID: {}, booking ID: {}", dto.getVehicleId(),
                dto.getBookingId());
        HandoverResponseDTO response = handoverService.initiateHandover(dto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // READ: Get all handovers (optional status filter: ?status=PENDING)
    @GetMapping
    public ResponseEntity<List<HandoverResponseDTO>> getAllHandovers(@RequestParam(required = false) String status) {
        List<HandoverResponseDTO> list = handoverService.getAllHandovers(status);
        return ResponseEntity.ok(list);
    }

    // READ: Get handover by ID
    @GetMapping("/{id}")
    public ResponseEntity<HandoverResponseDTO> getHandoverById(@PathVariable Long id) {
        HandoverResponseDTO response = handoverService.getHandoverById(id);
        return ResponseEntity.ok(response);
    }

    // READ: Get handovers for a specific vehicle
    @GetMapping("/vehicle/{vehicleId}")
    public ResponseEntity<List<HandoverResponseDTO>> getHandoversByVehicleId(@PathVariable Long vehicleId) {
        List<HandoverResponseDTO> list = handoverService.getHandoversByVehicleId(vehicleId);
        return ResponseEntity.ok(list);
    }

    // UPDATE: Update editable handover fields prior to completion
    @PutMapping("/{id}")
    public ResponseEntity<HandoverResponseDTO> updateHandover(
            @PathVariable Long id,
            @RequestBody HandoverUpdateRequestDTO dto) {
        HandoverResponseDTO response = handoverService.updateHandover(id, dto);
        return ResponseEntity.ok(response);
    }

    // DELETE: Delete a handover record (only allowed if PENDING, IN_PROGRESS, or
    // CANCELLED)
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteHandover(@PathVariable Long id) {
        handoverService.deleteHandover(id);
        return ResponseEntity.ok("Handover record #" + id + " deleted successfully.");
    }



    // BUSINESS OPERATION: Record vehicle starting mileage
    @PutMapping("/{id}/record-mileage")
    public ResponseEntity<HandoverResponseDTO> recordMileage(
            @PathVariable Long id,
            @Valid @RequestBody MileageRecordDTO dto) {
        HandoverResponseDTO response = handoverService.recordMileage(id, dto);
        return ResponseEntity.ok(response);
    }

    // BUSINESS OPERATION: Record damage or confirm no damage
    @PutMapping("/{id}/record-damage")
    public ResponseEntity<HandoverResponseDTO> recordDamage(
            @PathVariable Long id,
            @Valid @RequestBody DamageInspectionDTO dto) {
        HandoverResponseDTO response = handoverService.recordDamage(id, dto);
        return ResponseEntity.ok(response);
    }

    // BUSINESS OPERATION: Complete vehicle handover
    @PutMapping("/{id}/complete")
    public ResponseEntity<HandoverResponseDTO> completeHandover(@PathVariable Long id) {
        HandoverResponseDTO response = handoverService.completeHandover(id);
        return ResponseEntity.ok(response);
    }

    // BUSINESS OPERATION: Cancel vehicle handover
    @PutMapping("/{id}/cancel")
    public ResponseEntity<HandoverResponseDTO> cancelHandover(@PathVariable Long id) {
        HandoverResponseDTO response = handoverService.cancelHandover(id);
        return ResponseEntity.ok(response);
    }
}
