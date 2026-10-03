package com.RentEaseLanka.RentEaseLankaTeam.Handover_Management.Model.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HandoverCreateRequestDTO {

    private Long vehicleId;

    private Long bookingId;

    private Long customerId;

    private String customerName;

    @NotBlank(message = "Staff ID is required")
    private String staffId;

    private String initialNotes;
}
