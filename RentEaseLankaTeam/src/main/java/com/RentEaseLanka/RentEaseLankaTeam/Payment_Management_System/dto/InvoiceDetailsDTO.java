package com.RentEaseLanka.RentEaseLankaTeam.Payment_Management_System.dto;

import com.RentEaseLanka.RentEaseLankaTeam.Payment_Management_System.enums.InvoiceStatus;
import java.math.BigDecimal;
import java.util.List;

public record InvoiceDetailsDTO(Long id,
                                Long vehicleReservationId,
                                Long customerId,
                                Integer rentalDays,
                                BigDecimal dailyRate,
                                BigDecimal subtotal,
                                BigDecimal taxAmount,
                                BigDecimal totalAmount,
                                BigDecimal paidAmount,
                                BigDecimal remainingBalance,
                                Boolean isPartialAllowed,
                                BigDecimal minimumDepositAmount,
                                InvoiceStatus status,
                                java.time.LocalDateTime pickupDeadline,
                                List<PaymentReceiptDTO> paymentHistory) {
}
