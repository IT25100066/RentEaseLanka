package com.RentEaseLanka.RentEaseLankaTeam.UserAuthAndDriver_Management.Controller;

import com.RentEaseLanka.RentEaseLankaTeam.UserAuthAndDriver_Management.Model.AuthResponseDTO;
import com.RentEaseLanka.RentEaseLankaTeam.UserAuthAndDriver_Management.Model.LoginDTO;
import com.RentEaseLanka.RentEaseLankaTeam.UserAuthAndDriver_Management.Model.User;
import com.RentEaseLanka.RentEaseLankaTeam.UserAuthAndDriver_Management.Model.UserRegisterDTO;
import com.RentEaseLanka.RentEaseLankaTeam.UserAuthAndDriver_Management.Service.AuthAndDriverService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller handling public authentication endpoints (Registration and Login).
 * Endpoint Prefix: /api/auth
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthAndDriverService authService;

    /**
     * POST: Register a new user account (Customer or Driver).
     * Endpoint: POST /api/auth/register
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody UserRegisterDTO dto) {
        try {
            User registeredUser = authService.registerUser(dto);
            return ResponseEntity.ok(registeredUser);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * POST: Authenticate user credentials and return a signed JWT token.
     * Endpoint: POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDTO loginDTO) {
        try {
            AuthResponseDTO response = authService.login(loginDTO);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}