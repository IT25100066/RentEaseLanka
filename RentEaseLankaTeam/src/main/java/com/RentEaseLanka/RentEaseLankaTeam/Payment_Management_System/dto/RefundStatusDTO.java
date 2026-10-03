package com.RentEaseLanka.RentEaseLankaTeam.Payment_Management_System.dto;

import com.RentEaseLanka.RentEaseLankaTeam.Payment_Management_System.enums.InvoiceStatus;
import com.RentEaseLanka.RentEaseLankaTeam.Payment_Management_System.enums.PaymentStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RefundStatusDTO(Long paymentId,
                              BigDecimal refundAmount,
                              PaymentStatus updatedPaymentStatus,
                              InvoiceStatus updatedInvoiceStatus,
                              LocalDateTime processedAt) {
}
