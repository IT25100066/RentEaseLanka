package com.RentEaseLanka.RentEaseLankaTeam.Driver_Management.Model;

import com.RentEaseLanka.RentEaseLankaTeam.User_Management.Model.User;
import lombok.Value;

@Value
public class UserSummaryDTO {
    Long id;
    String firstName;
    String lastName;
    String email;

    public static UserSummaryDTO from(User user) {
        return new UserSummaryDTO(user.getId(), user.getFirstName(), user.getLastName(), user.getEmail());
    }
}
