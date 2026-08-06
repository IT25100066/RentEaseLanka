package com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Model.DTOs;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NonNull;

import java.math.BigDecimal;

@Data
public class UpdateCategoryPriceDTO {

    @NotNull
    private Long id;
    @NonNull
    private BigDecimal basePrice;


}
