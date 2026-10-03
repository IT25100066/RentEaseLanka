package com.RentEaseLanka.RentEaseLankaTeam.Payment_Management_System.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class RefundRequest {
    @NotNull @DecimalMin(value = "0.01", message = "Refund amount must be at least 0.01")
    public BigDecimal refundAmount;

    @NotBlank(message = "Reason is required")
    public String reason;
}
