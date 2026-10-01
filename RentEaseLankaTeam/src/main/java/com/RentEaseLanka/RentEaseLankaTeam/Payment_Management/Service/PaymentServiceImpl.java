package com.RentEaseLanka.RentEaseLankaTeam.Payment_Management.Service;

import com.RentEaseLanka.RentEaseLankaTeam.Booking_Management.Model.Booking;
import com.RentEaseLanka.RentEaseLankaTeam.Booking_Management.Model.BookingStatus;
import com.RentEaseLanka.RentEaseLankaTeam.Booking_Management.Repository.BookingRepository;
import com.RentEaseLanka.RentEaseLankaTeam.Payment_Management.Model.DTO.DamageDeductionRequestDTO;
import com.RentEaseLanka.RentEaseLankaTeam.Payment_Management.Model.DTO.PaymentRequestDTO;
import com.RentEaseLanka.RentEaseLankaTeam.Payment_Management.Model.DTO.PaymentResponseDTO;
import com.RentEaseLanka.RentEaseLankaTeam.Payment_Management.Model.PaymentPurpose;
import com.RentEaseLanka.RentEaseLankaTeam.Payment_Management.Model.PaymentTransaction;
import com.RentEaseLanka.RentEaseLankaTeam.Payment_Management.Model.PaymentTransactionStatus;
import com.RentEaseLanka.RentEaseLankaTeam.Payment_Management.Repository.PaymentTransactionRepository;
import com.RentEaseLanka.RentEaseLankaTeam.User_Management.Model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    private final BookingRepository bookingRepository;
    private final PaymentTransactionRepository transactionRepository;
    private final PaymentSimulator paymentSimulator;

    @Value("${payment.deposit.amount:50000.00}")
    private BigDecimal depositAmount;

    @Override
    public BigDecimal getDepositAmount() {
        return depositAmount;
    }

    @Override
    @Transactional
    public PaymentResponseDTO payDeposit(Long bookingId, User customer, PaymentRequestDTO request) {
        Booking booking = getBooking(bookingId);
        assertOwner(booking, customer);
        if (booking.getStatus() == BookingStatus.CANCELLED || booking.getStatus() == BookingStatus.COMPLETED) {
            throw new IllegalStateException("A cancelled or completed booking cannot receive a deposit.");
        }
        return collect(booking, customer, PaymentPurpose.DEPOSIT, depositAmount, request);
    }

    @Override
    @Transactional
    public PaymentResponseDTO payFinalAmount(Long bookingId, User customer, PaymentRequestDTO request) {
        Booking booking = getBooking(bookingId);
        assertOwner(booking, customer);
        if (booking.getStatus() != BookingStatus.ACTIVE) {
            throw new IllegalStateException("Final payment is available only for an active booking.");
        }
        return collect(booking, customer, PaymentPurpose.RENTAL_FINAL_PAYMENT, booking.getTotalAmount(), request);
    }

    @Override
    @Transactional
    public PaymentResponseDTO recordDamageDeduction(Long bookingId, DamageDeductionRequestDTO request, User staff) {
        Booking booking = getBooking(bookingId);
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new IllegalStateException("A cancelled booking cannot receive a damage deduction.");
        }
        BigDecimal paidDeposit = successfulAmount(bookingId, PaymentPurpose.DEPOSIT);
        BigDecimal existingDeductions = successfulAmount(bookingId, PaymentPurpose.DAMAGE_DEDUCTION);
        BigDecimal remaining = paidDeposit.subtract(existingDeductions);
        if (request.getAmount().compareTo(remaining) > 0) {
            throw new IllegalArgumentException("Damage deduction cannot exceed the remaining deposit of " + remaining + ".");
        }

        String key = request.getIdempotencyKey() == null || request.getIdempotencyKey().isBlank()
                ? "damage-" + bookingId + "-" + request.getAmount() + "-" + request.getDescription().hashCode()
                : request.getIdempotencyKey();
        PaymentTransaction existing = transactionRepository.findByIdempotencyKey(key).orElse(null);
        if (existing != null) return toResponse(existing, remaining.subtract(existing.getAmount()));

        PaymentTransaction transaction = PaymentTransaction.builder()
                .bookingId(bookingId)
                .customerId(booking.getUser().getId())
                .amount(request.getAmount())
                .currency("LKR")
                .purpose(PaymentPurpose.DAMAGE_DEDUCTION)
                .status(PaymentTransactionStatus.SUCCESS)
                .transactionReference("DAMAGE-" + bookingId + "-" + System.nanoTime())
                .idempotencyKey(key)
                .failureReason(request.getDescription())
                .completedAt(LocalDateTime.now())
                .build();
        return toResponse(transactionRepository.save(transaction), remaining.subtract(request.getAmount()));
    }

    @Override
    public boolean hasSuccessfulDeposit(Long bookingId) {
        return transactionRepository.findFirstByBookingIdAndPurposeAndStatusOrderByCreatedAtDesc(
                bookingId, PaymentPurpose.DEPOSIT, PaymentTransactionStatus.SUCCESS).isPresent();
    }

    @Override
    public boolean hasSuccessfulFinalPayment(Long bookingId) {
        return transactionRepository.findFirstByBookingIdAndPurposeAndStatusOrderByCreatedAtDesc(
                bookingId, PaymentPurpose.RENTAL_FINAL_PAYMENT, PaymentTransactionStatus.SUCCESS).isPresent();
    }

    @Override
    @Transactional
    public void settleBooking(Long bookingId) {
        Booking booking = getBooking(bookingId);
        if (!hasSuccessfulFinalPayment(bookingId)) {
            throw new IllegalStateException("Full rental payment is required before completing the booking.");
        }
        PaymentTransaction deposit = transactionRepository
                .findFirstByBookingIdAndPurposeAndStatusOrderByCreatedAtDesc(bookingId, PaymentPurpose.DEPOSIT, PaymentTransactionStatus.SUCCESS)
                .orElseThrow(() -> new IllegalStateException("A successful deposit is required before completing the booking."));
        BigDecimal deductions = successfulAmount(bookingId, PaymentPurpose.DAMAGE_DEDUCTION);
        BigDecimal refundAmount = deposit.getAmount().subtract(deductions);
        if (refundAmount.signum() <= 0) return;

        String key = "refund-" + bookingId;
        if (transactionRepository.findByIdempotencyKey(key).filter(t -> t.getStatus() == PaymentTransactionStatus.SUCCESS).isPresent()) return;
        PaymentSimulator.SimulatorResult result = paymentSimulator.transfer(refundAmount, key, "SUCCESS");
        PaymentTransaction refund = PaymentTransaction.builder()
                .bookingId(bookingId)
                .customerId(booking.getUser().getId())
                .amount(refundAmount)
                .currency("LKR")
                .purpose(PaymentPurpose.DEPOSIT_REFUND)
                .status(result.successful() ? PaymentTransactionStatus.SUCCESS : PaymentTransactionStatus.REFUND_PENDING)
                .transactionReference(result.reference() != null ? result.reference() : "REFUND-PENDING-" + bookingId)
                .idempotencyKey(key)
                .failureReason(result.failureReason())
                .completedAt(result.successful() ? LocalDateTime.now() : null)
                .build();
        transactionRepository.save(refund);
        if (!result.successful()) throw new IllegalStateException("Deposit refund is pending: " + result.failureReason());
    }

    private PaymentResponseDTO collect(Booking booking, User customer, PaymentPurpose purpose,
                                       BigDecimal amount, PaymentRequestDTO request) {
        if (request == null || request.getIdempotencyKey() == null || request.getIdempotencyKey().isBlank()) {
            throw new IllegalArgumentException("A unique idempotency key is required.");
        }
        PaymentTransaction successful = transactionRepository
            .findFirstByBookingIdAndPurposeAndStatusOrderByCreatedAtDesc(
                booking.getBookingId(), purpose, PaymentTransactionStatus.SUCCESS)
            .orElse(null);
        if (successful != null) return toResponse(successful, remainingDeposit(booking.getBookingId()));
        PaymentTransaction existing = transactionRepository.findByIdempotencyKey(request.getIdempotencyKey()).orElse(null);
        if (existing != null) return toResponse(existing, remainingDeposit(booking.getBookingId()));
        PaymentSimulator.SimulatorResult result = paymentSimulator.collect(amount, request.getIdempotencyKey(), request.getSimulatorMode());
        PaymentTransaction transaction = PaymentTransaction.builder()
                .bookingId(booking.getBookingId())
                .customerId(customer.getId())
                .amount(amount)
                .currency("LKR")
                .purpose(purpose)
                .status(result.successful() ? PaymentTransactionStatus.SUCCESS : PaymentTransactionStatus.FAILED)
                .transactionReference(result.reference() != null ? result.reference() : "FAILED-" + purpose + "-" + booking.getBookingId())
                .idempotencyKey(request.getIdempotencyKey())
                .failureReason(result.failureReason())
                .completedAt(result.successful() ? LocalDateTime.now() : null)
                .build();
        return toResponse(transactionRepository.save(transaction), remainingDeposit(booking.getBookingId()));
    }

    private Booking getBooking(Long id) {
        return bookingRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Booking not found with ID: #" + id));
    }

    private void assertOwner(Booking booking, User customer) {
        if (customer == null || booking.getUser() == null || !booking.getUser().getId().equals(customer.getId())) {
            throw new IllegalArgumentException("You do not own this booking.");
        }
    }

    private BigDecimal remainingDeposit(Long bookingId) {
        BigDecimal deposit = successfulAmount(bookingId, PaymentPurpose.DEPOSIT);
        return deposit.subtract(successfulAmount(bookingId, PaymentPurpose.DAMAGE_DEDUCTION));
    }

    private BigDecimal successfulAmount(Long bookingId, PaymentPurpose purpose) {
        return transactionRepository.findByBookingIdAndPurposeAndStatus(bookingId, purpose, PaymentTransactionStatus.SUCCESS)
                .stream().map(PaymentTransaction::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private PaymentResponseDTO toResponse(PaymentTransaction transaction, BigDecimal remainingDeposit) {
        return PaymentResponseDTO.builder()
                .bookingId(transaction.getBookingId())
                .purpose(transaction.getPurpose())
                .amount(transaction.getAmount())
                .remainingDeposit(remainingDeposit)
                .currency(transaction.getCurrency())
                .status(transaction.getStatus())
                .transactionReference(transaction.getTransactionReference())
                .message(transaction.getFailureReason())
                .build();
    }
}
