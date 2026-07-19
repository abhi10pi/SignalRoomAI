package com.example.Backend.entity;

import com.example.Backend.enums.Outcome;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "outcome_evidence")
public class OutcomeEvidence {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "signal_id", nullable = false)
    private Signal signal;

    @Column(name = "source_url")
    private String sourceUrl;

    @Column(columnDefinition = "TEXT")
    private String snippet;

    @Column(name = "ai_confidence", precision = 5, scale = 4)
    private BigDecimal aiConfidence;

    @Enumerated(EnumType.STRING)
    @Column(name = "proposed_outcome")
    private Outcome proposedOutcome;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public UUID getId() { return id; }
    public Signal getSignal() { return signal; }
    public void setSignal(Signal signal) { this.signal = signal; }
    public String getSourceUrl() { return sourceUrl; }
    public void setSourceUrl(String sourceUrl) { this.sourceUrl = sourceUrl; }
    public String getSnippet() { return snippet; }
    public void setSnippet(String snippet) { this.snippet = snippet; }
    public BigDecimal getAiConfidence() { return aiConfidence; }
    public void setAiConfidence(BigDecimal aiConfidence) { this.aiConfidence = aiConfidence; }
    public Outcome getProposedOutcome() { return proposedOutcome; }
    public void setProposedOutcome(Outcome proposedOutcome) { this.proposedOutcome = proposedOutcome; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
