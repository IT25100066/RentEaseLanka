package com.RentEaseLanka.RentEaseLankaTeam.Payment_Management_System.dto;

import com.RentEaseLanka.RentEaseLankaTeam.Payment_Management_System.enums.InvoiceStatus;
import java.math.BigDecimal;

public record InvoiceDTO(Long id,
                         Long vehicleReservationId,
                         Long customerId,
                         Integer rentalDays,
                         BigDecimal dailyRate,
                         BigDecimal subtotal,
                         BigDecimal taxAmount,
                         BigDecimal totalAmount,
                         BigDecimal paidAmount,
                         Boolean isPartialAllowed,
                         BigDecimal minimumDepositAmount,
                         InvoiceStatus status) {
}
