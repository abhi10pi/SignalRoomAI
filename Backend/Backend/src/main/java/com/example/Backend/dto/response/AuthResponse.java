package com.example.Backend.dto.response;

import java.util.UUID;

public class AuthResponse {

    private String accessToken;
    private String refreshToken;
    private UUID userId;
    private String username;
    private String email;
    private String role;

    public AuthResponse(String accessToken, String refreshToken, UUID userId,
                        String username, String email, String role) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.role = role;
    }

    public String getAccessToken() { return accessToken; }
    public String getRefreshToken() { return refreshToken; }
    public UUID getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
}
