package com.RentEaseLanka.RentEaseLankaTeam.Driver_Management.Model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssignDriverRequest {
    @NotNull
    private Long driverId;
}
