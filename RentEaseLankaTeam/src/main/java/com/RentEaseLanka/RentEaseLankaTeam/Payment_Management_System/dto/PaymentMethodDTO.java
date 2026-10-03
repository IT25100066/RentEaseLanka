package com.RentEaseLanka.RentEaseLankaTeam.Payment_Management_System.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PaymentMethodDTO(Long id,
                               Long customerId,
                               String maskedCardNum,
                               BigDecimal simulatedBalance,
                               BigDecimal heldAmount,
                               BigDecimal availableBalance,
                               LocalDate expiryDate) {
}
