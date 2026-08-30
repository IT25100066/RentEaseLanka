package com.RentEaseLanka.RentEaseLankaTeam.UserAuthAndDriver_Management.Controller;

import com.RentEaseLanka.RentEaseLankaTeam.UserAuthAndDriver_Management.Model.User;
import com.RentEaseLanka.RentEaseLankaTeam.UserAuthAndDriver_Management.Service.AuthAndDriverService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller handling User Profile management and Staff Physical KYC verification.
 * Endpoint Prefix: /api/users
 */
@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private AuthAndDriverService authService;

    /**
     * PUT: Physical KYC verification executed by Staff/Admin at the rental counter.
     * Endpoint: PUT /api/users/{userId}/verify-kyc?staffUserId=X
     */
    @PutMapping("/{userId}/verify-kyc")
    public ResponseEntity<?> verifyKYC(@PathVariable Long userId, @RequestParam Long staffUserId) {
        try {
            User verifiedUser = authService.verifyPhysicalKYC(userId, staffUserId);
            return ResponseEntity.ok(verifiedUser);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}