package com.example.Backend.dto.request;

import com.example.Backend.enums.ResolutionType;
import com.example.Backend.enums.Visibility;
import jakarta.validation.constraints.Future;

import java.time.LocalDateTime;
import java.util.UUID;

public class UpdateSignalRequest {

    private String title;
    private String description;
    private UUID domainId;
    private ResolutionType resolutionType;
    private String resolutionCriteria;

    @Future
    private LocalDateTime resolutionDate;

    private Visibility visibility;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public UUID getDomainId() { return domainId; }
    public void setDomainId(UUID domainId) { this.domainId = domainId; }
    public ResolutionType getResolutionType() { return resolutionType; }
    public void setResolutionType(ResolutionType resolutionType) { this.resolutionType = resolutionType; }
    public String getResolutionCriteria() { return resolutionCriteria; }
    public void setResolutionCriteria(String resolutionCriteria) { this.resolutionCriteria = resolutionCriteria; }
    public LocalDateTime getResolutionDate() { return resolutionDate; }
    public void setResolutionDate(LocalDateTime resolutionDate) { this.resolutionDate = resolutionDate; }
    public Visibility getVisibility() { return visibility; }
    public void setVisibility(Visibility visibility) { this.visibility = visibility; }
}
