package com.RentEaseLanka.RentEaseLankaTeam.Handover_Management.Model.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HandoverUpdateRequestDTO {

    private String customerName;

    private String staffId;

    private String notes;

    private String damageDescription;



    private String fuelLevel;
}
