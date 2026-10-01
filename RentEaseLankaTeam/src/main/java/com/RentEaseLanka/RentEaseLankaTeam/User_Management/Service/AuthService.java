package com.RentEaseLanka.RentEaseLankaTeam.User_Management.Service;

import com.RentEaseLanka.RentEaseLankaTeam.User_Management.Config.JwtService;
import com.RentEaseLanka.RentEaseLankaTeam.User_Management.DTOs.AuthenticationRequest;
import com.RentEaseLanka.RentEaseLankaTeam.User_Management.DTOs.AuthenticationResponse;
import com.RentEaseLanka.RentEaseLankaTeam.User_Management.DTOs.RegisterRequest;
import com.RentEaseLanka.RentEaseLankaTeam.User_Management.Model.Role;
import com.RentEaseLanka.RentEaseLankaTeam.User_Management.Model.User;
import com.RentEaseLanka.RentEaseLankaTeam.User_Management.Model.VerificationStatus;
import com.RentEaseLanka.RentEaseLankaTeam.User_Management.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthenticationResponse register(RegisterRequest request) {
        if(repository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }
        
        var user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.CUSTOMER) // Default role is CUSTOMER
                .verificationStatus(VerificationStatus.PENDING) // Needs verification
                .build();
                
        repository.save(user);
        
        var jwtToken = jwtService.generateToken(user);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .user(user)
                .build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        var user = repository.findByEmail(request.getEmail())
                .orElseThrow();
        var jwtToken = jwtService.generateToken(user);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .user(user)
                .build();
    }
}
