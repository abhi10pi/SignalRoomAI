package com.example.Backend.dto.response;

import com.example.Backend.enums.SignalStatus;
import com.example.Backend.enums.Visibility;

import java.time.LocalDateTime;
import java.util.UUID;

public class SignalSummaryResponse {

    private UUID id;
    private String title;
    private SignalStatus status;
    private Visibility visibility;
    private String domainName;
    private String domainSlug;
    private String submitterUsername;
    private LocalDateTime resolutionDate;
    private LocalDateTime submittedAt;
    private LocalDateTime createdAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public SignalStatus getStatus() { return status; }
    public void setStatus(SignalStatus status) { this.status = status; }
    public Visibility getVisibility() { return visibility; }
    public void setVisibility(Visibility visibility) { this.visibility = visibility; }
    public String getDomainName() { return domainName; }
    public void setDomainName(String domainName) { this.domainName = domainName; }
    public String getDomainSlug() { return domainSlug; }
    public void setDomainSlug(String domainSlug) { this.domainSlug = domainSlug; }
    public String getSubmitterUsername() { return submitterUsername; }
    public void setSubmitterUsername(String submitterUsername) { this.submitterUsername = submitterUsername; }
    public LocalDateTime getResolutionDate() { return resolutionDate; }
    public void setResolutionDate(LocalDateTime resolutionDate) { this.resolutionDate = resolutionDate; }
    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
