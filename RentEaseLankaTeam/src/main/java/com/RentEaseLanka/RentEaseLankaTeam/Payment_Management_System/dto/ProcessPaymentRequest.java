package com.RentEaseLanka.RentEaseLankaTeam.Payment_Management_System.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class ProcessPaymentRequest {
    @NotNull(message = "Invoice ID is required")
    public Long invoiceId;

    @NotNull(message = "Payment Method ID is required")
    public Long paymentMethodId;

    @NotNull @DecimalMin(value = "0.01", message = "Amount must be at least 0.01")
    public BigDecimal amount;

    @NotBlank(message = "Idempotency key is required")
    public String idempotencyKey;
}
