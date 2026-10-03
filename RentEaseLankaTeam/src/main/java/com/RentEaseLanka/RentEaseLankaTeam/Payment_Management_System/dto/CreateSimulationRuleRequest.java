package com.RentEaseLanka.RentEaseLankaTeam.Payment_Management_System.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CreateSimulationRuleRequest {
    @NotBlank(message = "Card trigger pattern is required")
    public String cardNumberTrigger;

    @NotBlank(message = "Simulated response code is required")
    public String simulatedResponseCode;

    @NotNull(message = "Latency in ms is required")
    public Long latencyMs;
}
