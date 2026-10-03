package com.RentEaseLanka.RentEaseLankaTeam.Payment_Management_System.controller;


import com.RentEaseLanka.RentEaseLankaTeam.Payment_Management_System.dto.*;
import com.RentEaseLanka.RentEaseLankaTeam.Payment_Management_System.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/invoices")
    public ResponseEntity<InvoiceDTO> createInvoice(@Valid @RequestBody CreateInvoiceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.createInvoice(request));
    }

    @PostMapping("/process")
    public ResponseEntity<PaymentReceiptDTO> processPayment(@Valid @RequestBody ProcessPaymentRequest request) {
        return ResponseEntity.ok(paymentService.processPayment(request));
    }

    @PostMapping("/holds/place")
    public ResponseEntity<PaymentReceiptDTO> placeSecurityHold(
            @RequestParam Long invoiceId,
            @RequestParam Long paymentMethodId,
            @RequestParam BigDecimal holdAmount,
            @RequestParam String idempotencyKey) {
        return ResponseEntity.ok(paymentService.placeSecurityHold(invoiceId, paymentMethodId, holdAmount, idempotencyKey));
    }

    @PostMapping("/holds/{paymentId}/release")
    public ResponseEntity<PaymentReceiptDTO> releaseSecurityHold(
            @PathVariable Long paymentId,
            @RequestParam String reason) {
        return ResponseEntity.ok(paymentService.releaseSecurityHold(paymentId, reason));
    }

    @GetMapping("/holds/booking/{bookingId}")
    public ResponseEntity<PaymentReceiptDTO> getActiveSecurityHold(@PathVariable Long bookingId) {
        return ResponseEntity.ok(paymentService.getActiveSecurityHoldForBooking(bookingId));
    }

    @PostMapping("/damage/charge")
    public ResponseEntity<PaymentReceiptDTO> chargeDamage(@Valid @RequestBody ChargeDamageRequest request) {
        return ResponseEntity.ok(paymentService.processDamageRecovery(request));
    }

    @PostMapping("/cron/no-shows")
    public ResponseEntity<List<NoShowResultDTO>> triggerNoShowProcessing() {
        return ResponseEntity.ok(paymentService.processOverdueNoShows());
    }

    @GetMapping("/invoices/{invoiceId}")
    public ResponseEntity<InvoiceDetailsDTO> getInvoiceDetails(@PathVariable Long invoiceId) {
        return ResponseEntity.ok(paymentService.getInvoiceDetails(invoiceId));
    }

    @GetMapping("/invoices/booking/{bookingId}")
    public ResponseEntity<InvoiceDetailsDTO> getInvoiceByBookingId(@PathVariable Long bookingId) {
        return ResponseEntity.ok(paymentService.getInvoiceByBookingId(bookingId));
    }

    @GetMapping("/customer/{customerId}/invoices")
    public ResponseEntity<List<InvoiceDTO>> getCustomerInvoices(@PathVariable Long customerId) {
        return ResponseEntity.ok(paymentService.getInvoicesByCustomerId(customerId));
    }

    @PostMapping("/{paymentId}/refund")
    public ResponseEntity<RefundStatusDTO> issueRefund(@PathVariable Long paymentId, @Valid @RequestBody RefundRequest request) {
        return ResponseEntity.ok(paymentService.issueRefund(paymentId, request));
    }

    @GetMapping("/customer/{customerId}/methods")
    public ResponseEntity<List<PaymentMethodDTO>> getCustomerMethods(@PathVariable Long customerId) {
        return ResponseEntity.ok(paymentService.getCustomerMethods(customerId));
    }

    @PostMapping("/customer/{customerId}/methods")
    public ResponseEntity<PaymentMethodDTO> addCustomerPaymentMethod(@PathVariable Long customerId, @Valid @RequestBody AddPaymentMethodRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.addPaymentMethod(customerId, request));
    }

    @PostMapping("/simulator/rules")
    public ResponseEntity<SimulationRuleDTO> configureRule(@Valid @RequestBody CreateSimulationRuleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.configureRule(request));
    }
}

