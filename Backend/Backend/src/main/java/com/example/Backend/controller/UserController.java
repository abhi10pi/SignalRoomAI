package com.example.Backend.controller;

import com.example.Backend.dto.response.UserProfileResponse;
import com.example.Backend.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // GET /api/users/me/profile
    @GetMapping("/me/profile")
    public UserProfileResponse getMyProfile(Authentication auth) {
        return userService.getMyProfile(currentUserId(auth));
    }

    // GET /api/users/{username}
    @GetMapping("/{username}")
    public UserProfileResponse getProfile(@PathVariable String username) {
        return userService.getProfileByUsername(username);
    }

    private UUID currentUserId(Authentication auth) {
        return (UUID) auth.getPrincipal();
    }
}
