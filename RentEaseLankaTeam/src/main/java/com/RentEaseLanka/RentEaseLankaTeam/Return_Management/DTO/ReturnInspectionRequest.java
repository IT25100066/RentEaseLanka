package com.RentEaseLanka.RentEaseLankaTeam.Return_Management.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public class ReturnInspectionRequest {
    @NotBlank(message = "Staff ID is required")
    public String staffId;

    @NotNull(message = "Ending mileage is required")
    @PositiveOrZero(message = "Ending mileage cannot be negative")
    public Double endingMileage;

    public boolean hasDamage;
    public String damageDescription;

    /** Required when damage is found; this amount is captured from the security deposit. */
    @PositiveOrZero(message = "Damage amount cannot be negative")
    public BigDecimal damageAmount;
    public String notes;
}
