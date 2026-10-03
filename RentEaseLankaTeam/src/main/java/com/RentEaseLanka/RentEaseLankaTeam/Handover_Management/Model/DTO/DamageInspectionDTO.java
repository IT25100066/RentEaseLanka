package com.RentEaseLanka.RentEaseLankaTeam.Handover_Management.Model.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DamageInspectionDTO {

    private Boolean hasDamage;

    private String damageDescription;
}
