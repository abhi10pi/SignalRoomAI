package com.example.Backend.dto.response;

import com.example.Backend.enums.PromotionStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public class PromotionRequestResponse {
    private UUID id;
    private UUID userId;
    private String username;
    private String justification;
    private PromotionStatus status;
    private String reviewedByUsername;
    private LocalDateTime createdAt;
    private LocalDateTime reviewedAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getJustification() { return justification; }
    public void setJustification(String justification) { this.justification = justification; }
    public PromotionStatus getStatus() { return status; }
    public void setStatus(PromotionStatus status) { this.status = status; }
    public String getReviewedByUsername() { return reviewedByUsername; }
    public void setReviewedByUsername(String reviewedByUsername) { this.reviewedByUsername = reviewedByUsername; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(LocalDateTime reviewedAt) { this.reviewedAt = reviewedAt; }
}
