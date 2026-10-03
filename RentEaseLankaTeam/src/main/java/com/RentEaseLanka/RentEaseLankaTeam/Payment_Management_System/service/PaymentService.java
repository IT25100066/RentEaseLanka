package com.RentEaseLanka.RentEaseLankaTeam.Payment_Management_System.service;

import com.RentEaseLanka.RentEaseLankaTeam.Booking_Management.Model.Booking;
import com.RentEaseLanka.RentEaseLankaTeam.Booking_Management.Model.BookingStatus;
import com.RentEaseLanka.RentEaseLankaTeam.Booking_Management.Repository.BookingRepository;
import com.RentEaseLanka.RentEaseLankaTeam.Payment_Management_System.dto.*;
import com.RentEaseLanka.RentEaseLankaTeam.Payment_Management_System.entity.*;
import com.RentEaseLanka.RentEaseLankaTeam.Payment_Management_System.enums.EventType;
import com.RentEaseLanka.RentEaseLankaTeam.Payment_Management_System.enums.InvoiceStatus;
import com.RentEaseLanka.RentEaseLankaTeam.Payment_Management_System.enums.PaymentStatus;
import com.RentEaseLanka.RentEaseLankaTeam.Payment_Management_System.enums.PaymentType;
import com.RentEaseLanka.RentEaseLankaTeam.Payment_Management_System.repository.*;
import com.RentEaseLanka.RentEaseLankaTeam.Return_Management.Repository.VehicleReturnInspectionRepository;

// Required Spring & Lombok Imports
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PaymentService {

        private final InvoiceRepository invoiceRepository;
        private final PaymentRepository paymentRepository;
        private final PaymentMethodRepository paymentMethodRepository;
        private final PaymentTransactionLogRepository logRepository;
        private final SimulatorConfigRepository simulatorConfigRepository;
        private final DamageRecordRepository damageRecordRepository;
        private final PaymentSimulatorService simulatorService;
        private final BookingRepository bookingRepository;
        private final VehicleReturnInspectionRepository returnInspectionRepository;

        // --- Business Validation Matrix Rules ---

        public boolean isPartialPaymentAllowed(int rentalDays) {
                return rentalDays >= 3;
        }

        public BigDecimal calculateMinimumDeposit(BigDecimal totalAmount, BigDecimal dailyRate, int rentalDays) {
                if (!isPartialPaymentAllowed(rentalDays)) {
                        return totalAmount; // 100% upfront for Short-Term (1-2 days)
                }
                BigDecimal thirtyPercent = totalAmount.multiply(new BigDecimal("0.30")).setScale(2,
                                RoundingMode.HALF_UP);
                return thirtyPercent.max(dailyRate);
        }

        @Transactional
        public InvoiceDTO createInvoice(CreateInvoiceRequest req) {
                BigDecimal total = req.subtotal.add(req.taxAmount);
                boolean partialAllowed = isPartialPaymentAllowed(req.rentalDays);
                BigDecimal minDeposit = calculateMinimumDeposit(total, req.dailyRate, req.rentalDays);

                Invoice invoice = Invoice.builder()
                                .vehicleReservationId(req.vehicleReservationId)
                                .customerId(req.customerId)
                                .rentalDays(req.rentalDays)
                                .dailyRate(req.dailyRate)
                                .subtotal(req.subtotal)
                                .taxAmount(req.taxAmount)
                                .totalAmount(total)
                                .paidAmount(BigDecimal.ZERO)
                                .isPartialAllowed(partialAllowed)
                                .minimumDepositAmount(minDeposit)
                                .status(InvoiceStatus.UNPAID)
                                .pickupDeadline(req.pickupDeadline)
                                .build();

                Invoice saved = invoiceRepository.save(invoice);
                return mapToInvoiceDTO(saved);
        }

        @Transactional
        public PaymentReceiptDTO processPayment(ProcessPaymentRequest req) {
                if (logRepository.existsByPayloadSnapshotContaining(req.idempotencyKey)) {
                        throw new IllegalStateException(
                                        "Duplicate transaction execution detected for key: " + req.idempotencyKey);
                }

                Invoice invoice = invoiceRepository.findByIdForUpdate(req.invoiceId)
                                .orElseThrow(() -> new IllegalArgumentException("Invoice not found: " + req.invoiceId));

                Booking booking = bookingRepository.findById(invoice.getVehicleReservationId())
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Booking not found for invoice: " + invoice.getId()));
                if (booking.getStatus() != BookingStatus.CONFIRMED
                                && booking.getStatus() != BookingStatus.ACTIVE) {
                        throw new IllegalStateException(
                                        "Rental payment can be recorded only after handover is complete and the booking is CONFIRMED.");
                }

                if (invoice.getStatus() == InvoiceStatus.PAID || invoice.getStatus() == InvoiceStatus.NO_SHOW) {
                        throw new IllegalStateException(
                                        "Invoice cannot accept further payments in status: " + invoice.getStatus());
                }

                if (!invoice.getIsPartialAllowed() && req.amount.compareTo(invoice.getTotalAmount()) < 0) {
                        invoice.setStatus(InvoiceStatus.REQUEST_REJECTED);
                        invoiceRepository.save(invoice);
                        throw new IllegalArgumentException(
                                        "Partial payments are REJECTED for short-term rentals (1-2 Days). Minimum required: "
                                                        + invoice.getTotalAmount());
                }

                if (invoice.getPaidAmount().compareTo(BigDecimal.ZERO) == 0
                                && req.amount.compareTo(invoice.getMinimumDepositAmount()) < 0) {
                        throw new IllegalArgumentException("Initial payment is below minimum deposit requirement of "
                                        + invoice.getMinimumDepositAmount());
                }

                if (req.amount.compareTo(invoice.getRemainingBalance()) > 0) {
                        throw new IllegalArgumentException(
                                        "Payment amount exceeds remaining balance of " + invoice.getRemainingBalance());
                }

                PaymentMethod method = paymentMethodRepository.findById(req.paymentMethodId)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Payment method not found: " + req.paymentMethodId));

                if (!method.getCustomerId().equals(invoice.getCustomerId())) {
                        throw new IllegalArgumentException(
                                        "Payment method does not belong to customer: " + invoice.getCustomerId());
                }

                var simResp = simulatorService.simulateTransaction(method, req.amount);

                Payment payment = Payment.builder()
                                .invoiceId(invoice.getId())
                                .paymentMethodId(method.getId())
                                .amount(req.amount)
                                .paymentType(PaymentType.RENTAL_FEE)
                                .build();

                if (simResp.success()) {
                        payment.setStatus(PaymentStatus.SUCCESS);
                        payment.setTxnReference(simResp.txnRef());

                        invoice.setPaidAmount(invoice.getPaidAmount().add(req.amount));
                        if (invoice.getPaidAmount().compareTo(invoice.getTotalAmount()) == 0) {
                                invoice.setStatus(InvoiceStatus.PAID);
                        } else {
                                invoice.setStatus(InvoiceStatus.PARTIALLY_PAID);
                        }
                } else {
                        payment.setStatus(PaymentStatus.FAILED);
                }

                Payment savedPayment = paymentRepository.save(payment);

                PaymentTransactionLog log = PaymentTransactionLog.builder()
                                .paymentId(savedPayment.getId())
                                .eventType(EventType.AUTHORIZATION)
                                .payloadSnapshot("idempotencyKey=" + req.idempotencyKey + ", card="
                                                + method.getMaskedCardNum())
                                .responseCode(simResp.responseCode())
                                .build();
                logRepository.save(log);

                return new PaymentReceiptDTO(
                                savedPayment.getId(),
                                savedPayment.getTxnReference(),
                                savedPayment.getStatus(),
                                simResp.responseCode(),
                                invoice.getRemainingBalance(),
                                LocalDateTime.now());
        }

        // --- Damage Recovery & Security Pre-Auth Module ---

        @Transactional
        public PaymentReceiptDTO placeSecurityHold(Long invoiceId, Long paymentMethodId, BigDecimal holdAmount,
                        String idempotencyKey) {
                if (logRepository.existsByPayloadSnapshotContaining(idempotencyKey)) {
                        throw new IllegalStateException("Duplicate transaction key: " + idempotencyKey);
                }

                Invoice invoice = invoiceRepository.findById(invoiceId)
                                .orElseThrow(() -> new IllegalArgumentException("Invoice not found: " + invoiceId));

                Booking booking = bookingRepository.findById(invoice.getVehicleReservationId())
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Booking not found for invoice: " + invoice.getId()));
                if (booking.getStatus() != com.RentEaseLanka.RentEaseLankaTeam.Booking_Management.Model.BookingStatus.PENDING) {
                        throw new IllegalStateException(
                                        "A security deposit can be authorized only before a pending booking handover is completed.");
                }

                PaymentMethod method = paymentMethodRepository.findById(paymentMethodId)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Payment method not found: " + paymentMethodId));

                var simResp = simulatorService.simulatePreAuthHold(method, holdAmount);

                Payment holdPayment = Payment.builder()
                                .invoiceId(invoice.getId())
                                .paymentMethodId(method.getId())
                                .amount(holdAmount)
                                .paymentType(PaymentType.SECURITY_DEPOSIT_HOLD)
                                .status(simResp.success() ? PaymentStatus.HELD : PaymentStatus.FAILED)
                                .txnReference(simResp.txnRef())
                                .build();

                Payment savedPayment = paymentRepository.save(holdPayment);

                PaymentTransactionLog log = PaymentTransactionLog.builder()
                                .paymentId(savedPayment.getId())
                                .eventType(EventType.PRE_AUTH_HOLD)
                                .payloadSnapshot("Pre-Auth Hold LKR " + holdAmount + " | key=" + idempotencyKey)
                                .responseCode(simResp.responseCode())
                                .build();
                logRepository.save(log);

                return new PaymentReceiptDTO(
                                savedPayment.getId(),
                                savedPayment.getTxnReference(),
                                savedPayment.getStatus(),
                                simResp.responseCode(),
                                invoice.getRemainingBalance(),
                                LocalDateTime.now());
        }

        @Transactional(readOnly = true)
        public boolean hasActiveSecurityHoldForBooking(Long bookingId) {
                return invoiceRepository.findByVehicleReservationId(bookingId)
                                .flatMap(invoice -> paymentRepository.findFirstByInvoiceIdAndPaymentTypeAndStatus(
                                                invoice.getId(), PaymentType.SECURITY_DEPOSIT_HOLD, PaymentStatus.HELD))
                                .isPresent();
        }

        @Transactional(readOnly = true)
        public PaymentReceiptDTO getActiveSecurityHoldForBooking(Long bookingId) {
                Invoice invoice = invoiceRepository.findByVehicleReservationId(bookingId)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Invoice not found for booking: " + bookingId));
                Payment hold = paymentRepository.findFirstByInvoiceIdAndPaymentTypeAndStatus(
                                invoice.getId(), PaymentType.SECURITY_DEPOSIT_HOLD, PaymentStatus.HELD)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "No active security deposit hold found for booking: " + bookingId));
                return new PaymentReceiptDTO(hold.getId(), hold.getTxnReference(), hold.getStatus(),
                                "200_HOLD_ACTIVE", invoice.getRemainingBalance(), hold.getCreatedAt());
        }

        /**
         * Settles the deposit as part of the return workflow, before booking
         * completion.
         */
        @Transactional
        public void settleSecurityDepositForReturn(Long bookingId, boolean hasDamage, BigDecimal damageAmount,
                        String damageDescription) {
                Invoice invoice = invoiceRepository.findByVehicleReservationId(bookingId)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Invoice not found for booking: " + bookingId));
                Payment hold = paymentRepository.findFirstByInvoiceIdAndPaymentTypeAndStatus(
                                invoice.getId(), PaymentType.SECURITY_DEPOSIT_HOLD, PaymentStatus.HELD)
                                .orElseThrow(() -> new IllegalStateException(
                                                "No active security deposit is available to settle."));

                if (!hasDamage) {
                        releaseSecurityHold(hold.getId(), "Vehicle returned with no chargeable damage");
                        return;
                }
                if (damageAmount == null || damageAmount.compareTo(BigDecimal.ZERO) <= 0) {
                        throw new IllegalArgumentException(
                                        "A positive damage amount is required when return damage is recorded.");
                }
                ChargeDamageRequest charge = new ChargeDamageRequest();
                charge.invoiceId = invoice.getId();
                charge.paymentMethodId = hold.getPaymentMethodId();
                charge.damageAmount = damageAmount;
                charge.damageDescription = damageDescription;
                charge.existingSecurityHoldPaymentId = hold.getId();
                charge.idempotencyKey = "RETURN-DAMAGE-" + bookingId + "-" + System.currentTimeMillis();
                PaymentReceiptDTO receipt = processDamageRecovery(charge);
                if (receipt.status() != PaymentStatus.SUCCESS) {
                        throw new IllegalStateException(
                                        "Could not capture the damage amount from the customer deposit: "
                                                        + receipt.responseCode());
                }
        }

        /**
         * Releases the remaining pre-authorized deposit after the vehicle return
         * inspection.
         */
        @Transactional
        public PaymentReceiptDTO releaseSecurityHold(Long paymentId, String reason) {
                Payment hold = paymentRepository.findById(paymentId)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Payment record not found: " + paymentId));
                if (hold.getPaymentType() != PaymentType.SECURITY_DEPOSIT_HOLD
                                || hold.getStatus() != PaymentStatus.HELD) {
                        throw new IllegalStateException("Only an active security deposit hold can be released.");
                }

                Invoice invoice = invoiceRepository.findById(hold.getInvoiceId())
                                .orElseThrow(() -> new IllegalArgumentException("Linked invoice not found."));
                Booking booking = bookingRepository.findById(invoice.getVehicleReservationId())
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Booking not found for invoice: " + invoice.getId()));
                if (booking.getStatus() != BookingStatus.ACTIVE
                                || returnInspectionRepository.findByBookingId(booking.getBookingId()).isEmpty()) {
                        throw new IllegalStateException(
                                        "Security deposit can be released only after the return inspection has been recorded.");
                }

                PaymentMethod method = paymentMethodRepository.findById(hold.getPaymentMethodId())
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Associated payment method not found."));
                simulatorService.releasePreAuthHold(method, hold.getAmount());
                hold.setStatus(PaymentStatus.RELEASED);
                paymentRepository.save(hold);
                logRepository.save(PaymentTransactionLog.builder()
                                .paymentId(hold.getId())
                                .eventType(EventType.HOLD_RELEASE)
                                .payloadSnapshot("depositRelease=" + hold.getAmount() + ", reason=" + reason)
                                .responseCode("200_HOLD_RELEASED")
                                .build());

                return new PaymentReceiptDTO(hold.getId(), hold.getTxnReference(), hold.getStatus(),
                                "200_HOLD_RELEASED", invoice.getRemainingBalance(), LocalDateTime.now());
        }

        @Transactional
        public PaymentReceiptDTO processDamageRecovery(ChargeDamageRequest req) {
                if (logRepository.existsByPayloadSnapshotContaining(req.idempotencyKey)) {
                        throw new IllegalStateException(
                                        "Duplicate transaction execution detected for key: " + req.idempotencyKey);
                }

                Invoice invoice = invoiceRepository.findByIdForUpdate(req.invoiceId)
                                .orElseThrow(() -> new IllegalArgumentException("Invoice not found: " + req.invoiceId));
                Booking booking = bookingRepository.findById(invoice.getVehicleReservationId())
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Booking not found for invoice: " + invoice.getId()));
                if (booking.getStatus() != BookingStatus.ACTIVE
                                || returnInspectionRepository.findByBookingId(booking.getBookingId()).isEmpty()) {
                        throw new IllegalStateException(
                                        "Damage recovery can be charged only after the return inspection has been recorded.");
                }

                PaymentMethod method = paymentMethodRepository.findById(req.paymentMethodId)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Payment method not found: " + req.paymentMethodId));

                DamageRecord record = DamageRecord.builder()
                                .vehicleReservationId(invoice.getVehicleReservationId())
                                .invoiceId(invoice.getId())
                                .assessedDamageAmount(req.damageAmount)
                                .damageDescription(req.damageDescription)
                                .isCharged(false)
                                .build();
                damageRecordRepository.save(record);

                // Priority 1: Check for existing active pre-auth hold to capture against
                Optional<Payment> activeHoldOpt = (req.existingSecurityHoldPaymentId != null)
                                ? paymentRepository.findById(req.existingSecurityHoldPaymentId)
                                : paymentRepository.findFirstByInvoiceIdAndPaymentTypeAndStatus(invoice.getId(),
                                                PaymentType.SECURITY_DEPOSIT_HOLD, PaymentStatus.HELD);

                PaymentReceiptDTO receipt;
                if (activeHoldOpt.isPresent() && activeHoldOpt.get().getStatus() == PaymentStatus.HELD) {
                        Payment holdPayment = activeHoldOpt.get();
                        // A security hold can only be captured against the card that owns it.
                        PaymentMethod holdMethod = paymentMethodRepository.findById(holdPayment.getPaymentMethodId())
                                        .orElseThrow(() -> new IllegalArgumentException(
                                                        "Payment method for the security hold was not found."));
                        var simResp = simulatorService.capturePreAuthHold(holdMethod, holdPayment.getAmount(),
                                        req.damageAmount);

                        if (simResp.success()) {
                                holdPayment.setStatus(PaymentStatus.RELEASED);
                                paymentRepository.save(holdPayment);
                        }

                        Payment capturePayment = Payment.builder()
                                        .invoiceId(invoice.getId())
                                        .paymentMethodId(method.getId())
                                        .amount(req.damageAmount)
                                        .paymentType(PaymentType.DAMAGE_RECOVERY_CHARGE)
                                        .status(simResp.success() ? PaymentStatus.SUCCESS : PaymentStatus.FAILED)
                                        .txnReference(simResp.txnRef())
                                        .build();
                        Payment savedPayment = paymentRepository.save(capturePayment);

                        logRepository.save(PaymentTransactionLog.builder()
                                        .paymentId(savedPayment.getId())
                                        .eventType(EventType.CAPTURE)
                                        .payloadSnapshot("Damage Captured from Hold: " + req.damageDescription
                                                        + " | Key=" + req.idempotencyKey)
                                        .responseCode(simResp.responseCode())
                                        .build());

                        record.setIsCharged(simResp.success());
                        receipt = new PaymentReceiptDTO(savedPayment.getId(), savedPayment.getTxnReference(),
                                        savedPayment.getStatus(), simResp.responseCode(), invoice.getRemainingBalance(),
                                        LocalDateTime.now());
                } else {
                        // Priority 2: Direct off-session card charge if no hold exists
                        var simResp = simulatorService.simulateTransaction(method, req.damageAmount);

                        Payment damagePayment = Payment.builder()
                                        .invoiceId(invoice.getId())
                                        .paymentMethodId(method.getId())
                                        .amount(req.damageAmount)
                                        .paymentType(PaymentType.DAMAGE_RECOVERY_CHARGE)
                                        .status(simResp.success() ? PaymentStatus.SUCCESS : PaymentStatus.FAILED)
                                        .txnReference(simResp.txnRef())
                                        .build();
                        Payment savedPayment = paymentRepository.save(damagePayment);

                        logRepository.save(PaymentTransactionLog.builder()
                                        .paymentId(savedPayment.getId())
                                        .eventType(EventType.DAMAGE_CHARGE)
                                        .payloadSnapshot("Direct Damage Charge: " + req.damageDescription + " | Key="
                                                        + req.idempotencyKey)
                                        .responseCode(simResp.responseCode())
                                        .build());

                        record.setIsCharged(simResp.success());
                        receipt = new PaymentReceiptDTO(savedPayment.getId(), savedPayment.getTxnReference(),
                                        savedPayment.getStatus(), simResp.responseCode(), invoice.getRemainingBalance(),
                                        LocalDateTime.now());
                }

                return receipt;
        }

        // --- No-Show Automated Engine Execution ---

        @Transactional
        public List<NoShowResultDTO> processOverdueNoShows() {
                List<Invoice> overdueInvoices = invoiceRepository.findOverduePickups(LocalDateTime.now());
                List<NoShowResultDTO> results = new ArrayList<>();

                for (Invoice invoice : overdueInvoices) {
                        results.add(executeNoShowPenalty(invoice));
                }

                return results;
        }

        @Transactional
        public NoShowResultDTO executeNoShowPenalty(Invoice invoice) {
                BigDecimal paidAmount = invoice.getPaidAmount();
                BigDecimal dailyRate = invoice.getDailyRate();
                int rentalDays = invoice.getRentalDays();
                boolean isPartial = invoice.getStatus() == InvoiceStatus.PARTIALLY_PAID;

                BigDecimal penaltyAmount = calculatePenalty(paidAmount, dailyRate, rentalDays, isPartial);
                BigDecimal refundAmount = paidAmount.subtract(penaltyAmount).max(BigDecimal.ZERO);

                if (refundAmount.compareTo(BigDecimal.ZERO) > 0) {
                        List<Payment> payments = paymentRepository.findByInvoiceId(invoice.getId());
                        Payment successfulPayment = payments.stream()
                                        .filter(p -> p.getStatus() == PaymentStatus.SUCCESS)
                                        .findFirst()
                                        .orElseThrow(() -> new IllegalStateException(
                                                        "No valid successful payment found to refund"));

                        PaymentMethod method = paymentMethodRepository.findById(successfulPayment.getPaymentMethodId())
                                        .orElseThrow(() -> new IllegalArgumentException(
                                                        "Payment method not found for refund"));

                        simulatorService.simulateRefund(method, refundAmount);
                        successfulPayment.setStatus(refundAmount.compareTo(paidAmount) == 0 ? PaymentStatus.REFUNDED
                                        : PaymentStatus.PARTIALLY_REFUNDED);
                        paymentRepository.save(successfulPayment);
                        logRepository.save(PaymentTransactionLog.builder()
                                        .paymentId(successfulPayment.getId())
                                        .eventType(EventType.REFUND)
                                        .payloadSnapshot(
                                                        "noShowRefund=" + refundAmount + ", invoice=" + invoice.getId())
                                        .responseCode("200_REFUNDED")
                                        .build());
                }

                invoice.setStatus(InvoiceStatus.NO_SHOW);
                invoiceRepository.save(invoice);

                return new NoShowResultDTO(
                                invoice.getId(),
                                paidAmount,
                                penaltyAmount,
                                refundAmount,
                                InvoiceStatus.NO_SHOW,
                                LocalDateTime.now());
        }

        private BigDecimal calculatePenalty(BigDecimal paidAmount, BigDecimal dailyRate, int rentalDays,
                        boolean isPartial) {
                if (isPartial)
                        return paidAmount;
                if (rentalDays == 1)
                        return paidAmount;
                if (rentalDays == 2)
                        return dailyRate;
                if (rentalDays < 30)
                        return dailyRate.multiply(new BigDecimal("2"));
                return dailyRate.multiply(new BigDecimal("3")).min(paidAmount);
        }

        @Transactional(readOnly = true)
        public InvoiceDetailsDTO getInvoiceDetails(Long invoiceId) {
                Invoice invoice = invoiceRepository.findById(invoiceId)
                                .orElseThrow(() -> new IllegalArgumentException("Invoice not found: " + invoiceId));

                List<PaymentReceiptDTO> history = paymentRepository.findByInvoiceId(invoiceId).stream()
                                .map(p -> new PaymentReceiptDTO(
                                                p.getId(),
                                                p.getTxnReference(),
                                                p.getStatus(),
                                                "200_OK",
                                                invoice.getRemainingBalance(),
                                                p.getCreatedAt() != null ? p.getCreatedAt() : LocalDateTime.now()))
                                .toList();

                return new InvoiceDetailsDTO(
                                invoice.getId(),
                                invoice.getVehicleReservationId(),
                                invoice.getCustomerId(),
                                invoice.getRentalDays(),
                                invoice.getDailyRate(),
                                invoice.getSubtotal(),
                                invoice.getTaxAmount(),
                                invoice.getTotalAmount(),
                                invoice.getPaidAmount(),
                                invoice.getRemainingBalance(),
                                invoice.getIsPartialAllowed(),
                                invoice.getMinimumDepositAmount(),
                                invoice.getStatus(),
                                invoice.getPickupDeadline(),
                                history);
        }

        @Transactional
        public InvoiceDetailsDTO getInvoiceByBookingId(Long bookingId) {
                Optional<Invoice> opt = invoiceRepository.findByVehicleReservationId(bookingId);
                if (opt.isPresent()) {
                        return getInvoiceDetails(opt.get().getId());
                }

                Booking booking = bookingRepository.findById(bookingId)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Booking not found with ID: " + bookingId));

                int rentalDays = (int) Math.max(1,
                                ChronoUnit.DAYS.between(booking.getPickupDateTime(), booking.getReturnDateTime()));
                BigDecimal dailyRate = booking.getVehicle() != null ? booking.getVehicle().getDailyRentalRate()
                                : booking.getTotalAmount();

                CreateInvoiceRequest req = new CreateInvoiceRequest();
                req.vehicleReservationId = booking.getBookingId();
                req.customerId = booking.getUser() != null ? booking.getUser().getId() : 1L;
                req.rentalDays = rentalDays;
                req.dailyRate = dailyRate != null ? dailyRate : BigDecimal.valueOf(5000);
                req.subtotal = booking.getTotalAmount();
                req.taxAmount = BigDecimal.ZERO.setScale(2);
                req.pickupDeadline = booking.getPickupDateTime() != null ? booking.getPickupDateTime().plusHours(2)
                                : LocalDateTime.now().plusHours(2);

                InvoiceDTO created = createInvoice(req);
                return getInvoiceDetails(created.id());
        }

        @Transactional(readOnly = true)
        public List<InvoiceDTO> getInvoicesByCustomerId(Long customerId) {
                return invoiceRepository.findByCustomerId(customerId).stream()
                                .map(this::mapToInvoiceDTO)
                                .toList();
        }

        @Transactional
        public PaymentMethodDTO addPaymentMethod(Long customerId, AddPaymentMethodRequest req) {
                String raw = req.cardNumber.replaceAll("\\s+", "");
                String masked = raw.length() >= 4
                                ? "**** **** **** " + raw.substring(raw.length() - 4)
                                : raw;

                PaymentMethod method = PaymentMethod.builder()
                                .customerId(customerId)
                                .maskedCardNum(masked)
                                .simulatedBalance(req.simulatedBalance != null ? req.simulatedBalance
                                                : new BigDecimal("250000.00"))
                                .heldAmount(BigDecimal.ZERO)
                                .expiryDate(req.expiryDate != null ? req.expiryDate
                                                : java.time.LocalDate.now().plusYears(3))
                                .build();

                PaymentMethod saved = paymentMethodRepository.save(method);
                return new PaymentMethodDTO(
                                saved.getId(),
                                saved.getCustomerId(),
                                saved.getMaskedCardNum(),
                                saved.getSimulatedBalance(),
                                saved.getHeldAmount(),
                                saved.getAvailableBalance(),
                                saved.getExpiryDate());
        }

        @Transactional
        public RefundStatusDTO issueRefund(Long paymentId, RefundRequest req) {
                Payment payment = paymentRepository.findById(paymentId)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Payment record not found: " + paymentId));

                if (payment.getStatus() != PaymentStatus.SUCCESS) {
                        throw new IllegalStateException("Only SUCCESSFUL payments can be refunded.");
                }

                if (req.refundAmount.compareTo(payment.getAmount()) > 0) {
                        throw new IllegalArgumentException("Refund amount cannot exceed original transaction amount: "
                                        + payment.getAmount());
                }

                PaymentMethod method = paymentMethodRepository.findById(payment.getPaymentMethodId())
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Associated payment method not found."));

                simulatorService.simulateRefund(method, req.refundAmount);

                payment.setStatus(req.refundAmount.compareTo(payment.getAmount()) == 0 ? PaymentStatus.REFUNDED
                                : PaymentStatus.PARTIALLY_REFUNDED);

                Invoice invoice = invoiceRepository.findByIdForUpdate(payment.getInvoiceId())
                                .orElseThrow(() -> new IllegalArgumentException("Linked invoice not found."));

                invoice.setPaidAmount(invoice.getPaidAmount().subtract(req.refundAmount));
                if (invoice.getPaidAmount().compareTo(BigDecimal.ZERO) == 0) {
                        invoice.setStatus(InvoiceStatus.UNPAID);
                } else {
                        invoice.setStatus(InvoiceStatus.PARTIALLY_PAID);
                }

                PaymentTransactionLog log = PaymentTransactionLog.builder()
                                .paymentId(payment.getId())
                                .eventType(EventType.REFUND)
                                .payloadSnapshot("refundAmount=" + req.refundAmount + ", reason=" + req.reason)
                                .responseCode("200_REFUNDED")
                                .build();
                logRepository.save(log);

                return new RefundStatusDTO(
                                payment.getId(),
                                req.refundAmount,
                                payment.getStatus(),
                                invoice.getStatus(),
                                LocalDateTime.now());
        }

        @Transactional
        public List<PaymentMethodDTO> getCustomerMethods(Long customerId) {
                List<PaymentMethod> methods = paymentMethodRepository.findByCustomerId(customerId);
                if (methods.isEmpty()) {
                        PaymentMethod defaultCard = PaymentMethod.builder()
                                        .customerId(customerId)
                                        .maskedCardNum("**** **** **** 4242")
                                        .simulatedBalance(new BigDecimal("500000.00"))
                                        .heldAmount(BigDecimal.ZERO)
                                        .expiryDate(LocalDate.now().plusYears(3))
                                        .build();
                        paymentMethodRepository.save(defaultCard);
                        methods = List.of(defaultCard);
                }
                return methods.stream()
                                .map(m -> new PaymentMethodDTO(m.getId(), m.getCustomerId(), m.getMaskedCardNum(),
                                                m.getSimulatedBalance(), m.getHeldAmount(), m.getAvailableBalance(),
                                                m.getExpiryDate()))
                                .toList();
        }

        @Transactional
        public SimulationRuleDTO configureRule(CreateSimulationRuleRequest req) {
                SimulatorConfig config = simulatorConfigRepository.findByTriggerPattern(req.cardNumberTrigger)
                                .orElseGet(SimulatorConfig::new);

                config.setTriggerPattern(req.cardNumberTrigger);
                config.setResponseCode(req.simulatedResponseCode);
                config.setLatencyMs(req.latencyMs);

                SimulatorConfig saved = simulatorConfigRepository.save(config);
                return new SimulationRuleDTO(saved.getId(), saved.getTriggerPattern(), saved.getResponseCode(),
                                saved.getLatencyMs());
        }

        private InvoiceDTO mapToInvoiceDTO(Invoice invoice) {
                return new InvoiceDTO(
                                invoice.getId(),
                                invoice.getVehicleReservationId(),
                                invoice.getCustomerId(),
                                invoice.getRentalDays(),
                                invoice.getDailyRate(),
                                invoice.getSubtotal(),
                                invoice.getTaxAmount(),
                                invoice.getTotalAmount(),
                                invoice.getPaidAmount(),
                                invoice.getIsPartialAllowed(),
                                invoice.getMinimumDepositAmount(),
                                invoice.getStatus());
        }
}
