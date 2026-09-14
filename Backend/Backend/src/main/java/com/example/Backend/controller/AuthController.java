package com.example.Backend.controller;

import com.example.Backend.dto.request.LoginRequest;
import com.example.Backend.dto.request.RefreshTokenRequest;
import com.example.Backend.dto.request.RegisterRequest;
import com.example.Backend.dto.response.AuthResponse;
import com.example.Backend.dto.response.UserProfileResponse;
import com.example.Backend.service.AuthService;
import com.example.Backend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    public AuthController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return authService.refreshToken(request.getRefreshToken());
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.OK)
    public Map<String, String> logout() {
        return Map.of("status", "success", "message", "Logged out successfully");
    }

    @GetMapping("/me")
    public UserProfileResponse me(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new IllegalArgumentException("Authentication required");
        }
        UUID userId = (UUID) authentication.getPrincipal();
        return userService.getMyProfile(userId);
    }
}
