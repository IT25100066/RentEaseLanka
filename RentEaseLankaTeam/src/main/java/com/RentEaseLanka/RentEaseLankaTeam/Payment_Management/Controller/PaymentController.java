package com.RentEaseLanka.RentEaseLankaTeam.Payment_Management.Controller;

import com.RentEaseLanka.RentEaseLankaTeam.Payment_Management.Model.DTO.DamageDeductionRequestDTO;
import com.RentEaseLanka.RentEaseLankaTeam.Payment_Management.Model.DTO.PaymentRequestDTO;
import com.RentEaseLanka.RentEaseLankaTeam.Payment_Management.Model.DTO.PaymentResponseDTO;
import com.RentEaseLanka.RentEaseLankaTeam.Payment_Management.Service.PaymentService;
import com.RentEaseLanka.RentEaseLankaTeam.User_Management.Model.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    @GetMapping("/deposit-amount")
    public ResponseEntity<Map<String, Object>> getDepositAmount() {
        return ResponseEntity.ok(Map.of("amount", paymentService.getDepositAmount(), "currency", "LKR"));
    }

    @PostMapping("/bookings/{bookingId}/deposit")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<PaymentResponseDTO> payDeposit(@PathVariable Long bookingId,
                                                          @AuthenticationPrincipal User customer,
                                                          @Valid @RequestBody PaymentRequestDTO request) {
        return ResponseEntity.ok(paymentService.payDeposit(bookingId, customer, request));
    }

    @PostMapping("/bookings/{bookingId}/final-payment")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<PaymentResponseDTO> payFinalPayment(@PathVariable Long bookingId,
                                                               @AuthenticationPrincipal User customer,
                                                               @Valid @RequestBody PaymentRequestDTO request) {
        return ResponseEntity.ok(paymentService.payFinalAmount(bookingId, customer, request));
    }

    @PostMapping("/bookings/{bookingId}/damage-deduction")
    @PreAuthorize("hasAnyRole('STAFF', 'MANAGER', 'ADMIN')")
    public ResponseEntity<PaymentResponseDTO> recordDamageDeduction(@PathVariable Long bookingId,
                                                                    @Valid @RequestBody DamageDeductionRequestDTO request,
                                                                    @AuthenticationPrincipal User staff) {
        return ResponseEntity.ok(paymentService.recordDamageDeduction(bookingId, request, staff));
    }
}
