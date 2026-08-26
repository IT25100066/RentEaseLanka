package com.RentEaseLanka.RentEaseLankaTeam.UserAuthAndDriver_Management.Repository;

import com.RentEaseLanka.RentEaseLankaTeam.UserAuthAndDriver_Management.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Checks if an email exists during registration
    boolean existsByEmail(String email);

    // Finds a user by email for authentication & login
    Optional<User> findByEmail(String email);
}