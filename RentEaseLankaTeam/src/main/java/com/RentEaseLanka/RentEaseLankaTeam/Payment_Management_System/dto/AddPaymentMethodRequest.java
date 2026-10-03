package com.RentEaseLanka.RentEaseLankaTeam.Payment_Management_System.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public class AddPaymentMethodRequest {
    @NotBlank(message = "Card number is required")
    public String cardNumber;

    @NotNull(message = "Simulated balance is required")
    @DecimalMin(value = "0.00", message = "Balance cannot be negative")
    public BigDecimal simulatedBalance;

    @NotNull(message = "Expiry date is required")
    public LocalDate expiryDate;
}
