package com.example.Backend.entity;

import com.example.Backend.enums.Outcome;
import com.example.Backend.enums.ResolutionType;
import com.example.Backend.enums.SignalStatus;
import com.example.Backend.enums.Visibility;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "signals")
public class Signal extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submitter_id",nullable = false)
    private User submitter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "domain_id",nullable = false)
    private Domain domain;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "resolution_type", nullable = false)
    private ResolutionType resolutionType;

    @Column(name = "resolution_criteria", columnDefinition = "jsonb", nullable = false)
    private String resolutionCriteria;

    @Column(name = "resolution_date", nullable = false)
    private LocalDateTime resolutionDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SignalStatus status = SignalStatus.DRAFT;

    @Column(nullable = false)
    private boolean contested = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "actual_outcome")
    private Outcome actualOutcome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Visibility visibility = Visibility.PUBLIC;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "evaluated_at")
    private LocalDateTime evaluatedAt;

    public UUID getId() { return id; }
    public User getSubmitter() { return submitter; }
    public void setSubmitter(User submitter) { this.submitter = submitter; }
    public Domain getDomain() { return domain; }
    public void setDomain(Domain domain) { this.domain = domain; }
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
    public boolean isContested() { return contested; }
    public void setContested(boolean contested) { this.contested = contested; }
    public Outcome getActualOutcome() { return actualOutcome; }
    public void setActualOutcome(Outcome actualOutcome) { this.actualOutcome = actualOutcome; }
    public Visibility getVisibility() { return visibility; }
    public void setVisibility(Visibility visibility) { this.visibility = visibility; }
    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }
    public LocalDateTime getEvaluatedAt() { return evaluatedAt; }
    public void setEvaluatedAt(LocalDateTime evaluatedAt) { this.evaluatedAt = evaluatedAt; }
}
