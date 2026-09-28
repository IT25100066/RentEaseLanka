package com.RentEaseLanka.RentEaseLankaTeam.Financial_Management.Controller;

import com.RentEaseLanka.RentEaseLankaTeam.Booking_Management.Model.BookingStatus;
import com.RentEaseLanka.RentEaseLankaTeam.Financial_Management.Model.DTO.FinancialSummaryDTO;
import com.RentEaseLanka.RentEaseLankaTeam.Financial_Management.Service.FinancialService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/financial")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
public class FinancialController {

    private final FinancialService financialService;

    @GetMapping("/income")
    public ResponseEntity<FinancialSummaryDTO> getIncomeSummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) BookingStatus status) {
        return ResponseEntity.ok(financialService.getIncomeSummary(from, to, status));
    }
}
