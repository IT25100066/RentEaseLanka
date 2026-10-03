package com.RentEaseLanka.RentEaseLankaTeam.Payment_Management_System.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public class ChargeDamageRequest {

    @NotNull(message = "Invoice ID is required")
    public Long invoiceId;

    @NotNull(message = "Payment Method ID is required")
    public Long paymentMethodId;

    @NotNull @Positive(message = "Assessed damage amount must be positive")
    public BigDecimal damageAmount;

    @NotBlank(message = "Damage description is required")
    public String damageDescription;

    @NotBlank(message = "Idempotency key is required")
    public String idempotencyKey;

    public Long existingSecurityHoldPaymentId; // Optional: specify hold to capture against
}

