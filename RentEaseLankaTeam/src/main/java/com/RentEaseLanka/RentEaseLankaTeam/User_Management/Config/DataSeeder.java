package com.RentEaseLanka.RentEaseLankaTeam.User_Management.Config;

import com.RentEaseLanka.RentEaseLankaTeam.User_Management.Model.Role;
import com.RentEaseLanka.RentEaseLankaTeam.User_Management.Model.User;
import com.RentEaseLanka.RentEaseLankaTeam.User_Management.Model.VerificationStatus;
import com.RentEaseLanka.RentEaseLankaTeam.User_Management.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.findByEmail("admin@renteaselanka.com").isEmpty()) {
            User admin = User.builder()
                    .firstName("Super")
                    .lastName("Admin")
                    .email("admin@renteaselanka.com")
                    .password(passwordEncoder.encode("admin123"))
                    .role(Role.ADMIN)
                    .verificationStatus(VerificationStatus.VERIFIED)
                    .build();
            userRepository.save(admin);
            System.out.println("Default ADMIN account created: admin@renteaselanka.com / admin123");
        }
    }
}
