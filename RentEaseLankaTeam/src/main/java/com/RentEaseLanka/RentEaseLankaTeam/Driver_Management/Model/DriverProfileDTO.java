package com.RentEaseLanka.RentEaseLankaTeam.Driver_Management.Model;

import com.RentEaseLanka.RentEaseLankaTeam.User_Management.Model.Role;
import com.RentEaseLanka.RentEaseLankaTeam.User_Management.Model.VerificationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DriverProfileDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private Role role;
    private VerificationStatus verificationStatus;
    private long totalCompletedTrips;
    private long upcomingTripsCount;
    private Long activeBookingId;
}
