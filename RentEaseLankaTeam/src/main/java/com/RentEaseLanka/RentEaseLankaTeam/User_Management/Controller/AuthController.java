package com.RentEaseLanka.RentEaseLankaTeam.User_Management.Controller;

import com.RentEaseLanka.RentEaseLankaTeam.User_Management.DTOs.AuthenticationRequest;
import com.RentEaseLanka.RentEaseLankaTeam.User_Management.DTOs.AuthenticationResponse;
import com.RentEaseLanka.RentEaseLankaTeam.User_Management.DTOs.RegisterRequest;
import com.RentEaseLanka.RentEaseLankaTeam.User_Management.Service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService service;

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(service.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request) {
        return ResponseEntity.ok(service.authenticate(request));
    }
}
