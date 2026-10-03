package com.RentEaseLanka.RentEaseLankaTeam.Payment_Management_System.dto;
import com.RentEaseLanka.RentEaseLankaTeam.Payment_Management_System.enums.InvoiceStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record NoShowResultDTO(Long invoiceId,
                              BigDecimal originalPaidAmount,
                              BigDecimal penaltyAmount,
                              BigDecimal refundAmount,
                              InvoiceStatus updatedInvoiceStatus,
                              LocalDateTime processedAt) {
}
