package com.RentEaseLanka.RentEaseLankaTeam.Payment_Management.Model.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PaymentRequestDTO {
    @NotBlank
    private String idempotencyKey;

    private String simulatorMode = "SUCCESS";
}
