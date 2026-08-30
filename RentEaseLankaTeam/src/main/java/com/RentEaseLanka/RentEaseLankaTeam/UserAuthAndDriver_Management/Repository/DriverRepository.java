package com.RentEaseLanka.RentEaseLankaTeam.UserAuthAndDriver_Management.Repository;

import com.RentEaseLanka.RentEaseLankaTeam.UserAuthAndDriver_Management.Model.AvailabilityStatus;
import com.RentEaseLanka.RentEaseLankaTeam.UserAuthAndDriver_Management.Model.DriverProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DriverRepository extends JpaRepository<DriverProfile, Long> {

    // Finds driver profile by the linked User ID
    Optional<DriverProfile> findByUserId(Long userId);

    // Lists drivers filtered by availability status
    List<DriverProfile> findByAvailabilityStatus(AvailabilityStatus availabilityStatus);
}
