package com.RentEaseLanka.RentEaseLankaTeam.UserAuthAndDriver_Management.Controller;

import com.RentEaseLanka.RentEaseLankaTeam.UserAuthAndDriver_Management.Model.User;
import com.RentEaseLanka.RentEaseLankaTeam.UserAuthAndDriver_Management.Model.UserRegisterDTO;
import com.RentEaseLanka.RentEaseLankaTeam.UserAuthAndDriver_Management.Service.AuthAndDriverService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthAndDriverService authService;

    // POST: Register a new User (Customer or Driver)
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody UserRegisterDTO dto) {
        try {
            User registeredUser = authService.registerUser(dto);
            return ResponseEntity.ok(registeredUser);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // PUT: Physical KYC verification by Staff
    @PutMapping("/users/{userId}/verify-kyc")
    public ResponseEntity<?> verifyKYC(@PathVariable Long userId, @RequestParam Long staffUserId) {
        try {
            User verifiedUser = authService.verifyPhysicalKYC(userId, staffUserId);
            return ResponseEntity.ok(verifiedUser);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}