package com.RentEaseLanka.RentEaseLankaTeam.User_Management.Controller;

import com.RentEaseLanka.RentEaseLankaTeam.User_Management.DTOs.CreateUserRequest;
import com.RentEaseLanka.RentEaseLankaTeam.User_Management.Model.User;
import com.RentEaseLanka.RentEaseLankaTeam.User_Management.Service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;

    @PostMapping("/managers")
    public ResponseEntity<User> createManager(@RequestBody CreateUserRequest request) {
        return ResponseEntity.ok(userService.createManager(request));
    }
}
