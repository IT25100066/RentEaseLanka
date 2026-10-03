package com.RentEaseLanka.RentEaseLankaTeam.Payment_Management_System.dto;

import com.RentEaseLanka.RentEaseLankaTeam.Payment_Management_System.enums.PaymentStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
public record PaymentReceiptDTO(Long paymentId,
                                String txnReference,
                                PaymentStatus status,
                                String responseCode,
                                BigDecimal remainingInvoiceBalance,
                                LocalDateTime processedAt) {
}
