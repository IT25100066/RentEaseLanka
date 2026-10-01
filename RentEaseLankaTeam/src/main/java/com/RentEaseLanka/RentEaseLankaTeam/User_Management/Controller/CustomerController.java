package com.RentEaseLanka.RentEaseLankaTeam.User_Management.Controller;

import com.RentEaseLanka.RentEaseLankaTeam.User_Management.Model.User;
import com.RentEaseLanka.RentEaseLankaTeam.User_Management.Service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;

@RestController
@RequestMapping("/api/customer")
@RequiredArgsConstructor
public class CustomerController {

    private final UserService userService;

    @PostMapping("/nic-upload")
    public ResponseEntity<User> uploadNic(
            @RequestParam("file") MultipartFile file,
            Principal principal) {
        // Principal contains the email of the currently authenticated user
        return ResponseEntity.ok(userService.uploadNic(principal.getName(), file));
    }
}
