package com.RentEaseLanka.RentEaseLankaTeam.Payment_Management.Model.DTO;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DamageDeductionRequestDTO {
    @NotNull
    @DecimalMin(value = "0.00")
    private BigDecimal amount;

    @NotBlank
    private String description;

    private String idempotencyKey;
}
