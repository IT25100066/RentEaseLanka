package com.RentEaseLanka.RentEaseLankaTeam.Payment_Management_System.dto;

public record SimulationRuleDTO(
        Long ruleId,
        String triggerPattern,
        String responseCode,
        Long latencyMs
) {
}
