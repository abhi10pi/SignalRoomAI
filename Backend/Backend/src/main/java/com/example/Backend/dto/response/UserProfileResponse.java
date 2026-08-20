package com.example.Backend.dto.response;

import java.util.List;
import java.util.UUID;

public class UserProfileResponse {
    private UUID id;
    private String username;
    private String bio;
    private String role;
    private int totalSignals;
    private int totalValidations;
    private List<CredibilityScoreResponse> credibilityScores;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public int getTotalSignals() { return totalSignals; }
    public void setTotalSignals(int totalSignals) { this.totalSignals = totalSignals; }
    public int getTotalValidations() { return totalValidations; }
    public void setTotalValidations(int totalValidations) { this.totalValidations = totalValidations; }
    public List<CredibilityScoreResponse> getCredibilityScores() { return credibilityScores; }
    public void setCredibilityScores(List<CredibilityScoreResponse> credibilityScores) { this.credibilityScores = credibilityScores; }
}
