package com.example.Backend.dto.response;

import com.example.Backend.enums.ResolutionType;
import com.example.Backend.enums.SignalStatus;
import com.example.Backend.enums.Visibility;

import java.time.LocalDateTime;
import java.util.UUID;

public class SignalResponse {

    private UUID id;
    private String title;
    private String description;
    private ResolutionType resolutionType;
    private String resolutionCriteria;
    private LocalDateTime resolutionDate;
    private SignalStatus status;
    private Visibility visibility;
    private LocalDateTime submittedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // submitter
    private UUID submitterId;
    private String submitterUsername;

    // domain
    private UUID domainId;
    private String domainName;
    private String domainSlug;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public ResolutionType getResolutionType() { return resolutionType; }
    public void setResolutionType(ResolutionType resolutionType) { this.resolutionType = resolutionType; }
    public String getResolutionCriteria() { return resolutionCriteria; }
    public void setResolutionCriteria(String resolutionCriteria) { this.resolutionCriteria = resolutionCriteria; }
    public LocalDateTime getResolutionDate() { return resolutionDate; }
    public void setResolutionDate(LocalDateTime resolutionDate) { this.resolutionDate = resolutionDate; }
    public SignalStatus getStatus() { return status; }
    public void setStatus(SignalStatus status) { this.status = status; }
    public Visibility getVisibility() { return visibility; }
    public void setVisibility(Visibility visibility) { this.visibility = visibility; }
    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public UUID getSubmitterId() { return submitterId; }
    public void setSubmitterId(UUID submitterId) { this.submitterId = submitterId; }
    public String getSubmitterUsername() { return submitterUsername; }
    public void setSubmitterUsername(String submitterUsername) { this.submitterUsername = submitterUsername; }
    public UUID getDomainId() { return domainId; }
    public void setDomainId(UUID domainId) { this.domainId = domainId; }
    public String getDomainName() { return domainName; }
    public void setDomainName(String domainName) { this.domainName = domainName; }
    public String getDomainSlug() { return domainSlug; }
    public void setDomainSlug(String domainSlug) { this.domainSlug = domainSlug; }
}
