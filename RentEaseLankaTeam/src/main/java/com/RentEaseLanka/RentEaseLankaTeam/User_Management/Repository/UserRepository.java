package com.RentEaseLanka.RentEaseLankaTeam.User_Management.Repository;

import com.RentEaseLanka.RentEaseLankaTeam.User_Management.Model.User;
import com.RentEaseLanka.RentEaseLankaTeam.User_Management.Model.Role;
import com.RentEaseLanka.RentEaseLankaTeam.User_Management.Model.VerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    
    List<User> findByVerificationStatus(VerificationStatus status);

    List<User> findByRole(Role role);
}
