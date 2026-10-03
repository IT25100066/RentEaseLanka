package com.RentEaseLanka.RentEaseLankaTeam.Driver_Management.Model;

import com.RentEaseLanka.RentEaseLankaTeam.User_Management.Model.User;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
public class Driver extends User {

    private String licenseNumber;
    private boolean isAvailable;

}
