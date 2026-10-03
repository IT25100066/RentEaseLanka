package com.RentEaseLanka.RentEaseLankaTeam.Handover_Management.Model.DTO;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MileageRecordDTO {

    @NotNull(message = "Starting mileage is required")
    @PositiveOrZero(message = "Starting mileage cannot be negative")
    private Double startingMileage;
}
