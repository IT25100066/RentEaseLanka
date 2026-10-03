package com.RentEaseLanka.RentEaseLankaTeam.Return_Management.Controller;

import com.RentEaseLanka.RentEaseLankaTeam.Return_Management.DTO.ReturnInspectionRequest;
import com.RentEaseLanka.RentEaseLankaTeam.Return_Management.DTO.ReturnInspectionResponse;
import com.RentEaseLanka.RentEaseLankaTeam.Return_Management.Service.VehicleReturnService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/returns")
@RequiredArgsConstructor
public class VehicleReturnController {
    private final VehicleReturnService returnService;

    @PostMapping("/booking/{bookingId}/inspect")
    @PreAuthorize("hasAnyRole('STAFF', 'MANAGER', 'ADMIN')")
    public ResponseEntity<ReturnInspectionResponse> inspect(@PathVariable Long bookingId,
            @Valid @RequestBody ReturnInspectionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(returnService.inspectAndCompleteReturn(bookingId, request));
    }

    @GetMapping("/booking/{bookingId}")
    @PreAuthorize("hasAnyRole('STAFF', 'MANAGER', 'ADMIN')")
    public ResponseEntity<ReturnInspectionResponse> getByBooking(@PathVariable Long bookingId) {
        return ResponseEntity.ok(returnService.getByBookingId(bookingId));
    }
}
