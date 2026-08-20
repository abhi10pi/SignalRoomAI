package com.example.Backend.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class CredibilityScoreResponse {
    private UUID id;
    private String domainName;
    private String domainSlug;
    private int totalSignals;
    private int correctSignals;
    private int totalValidations;
    private int correctValidations;
    private BigDecimal accuracyScore;
    private BigDecimal overconfidencePenalty;
    private BigDecimal finalScore;
    private LocalDateTime updatedAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getDomainName() { return domainName; }
    public void setDomainName(String domainName) { this.domainName = domainName; }
    public String getDomainSlug() { return domainSlug; }
    public void setDomainSlug(String domainSlug) { this.domainSlug = domainSlug; }
    public int getTotalSignals() { return totalSignals; }
    public void setTotalSignals(int totalSignals) { this.totalSignals = totalSignals; }
    public int getCorrectSignals() { return correctSignals; }
    public void setCorrectSignals(int correctSignals) { this.correctSignals = correctSignals; }
    public int getTotalValidations() { return totalValidations; }
    public void setTotalValidations(int totalValidations) { this.totalValidations = totalValidations; }
    public int getCorrectValidations() { return correctValidations; }
    public void setCorrectValidations(int correctValidations) { this.correctValidations = correctValidations; }
    public BigDecimal getAccuracyScore() { return accuracyScore; }
    public void setAccuracyScore(BigDecimal accuracyScore) { this.accuracyScore = accuracyScore; }
    public BigDecimal getOverconfidencePenalty() { return overconfidencePenalty; }
    public void setOverconfidencePenalty(BigDecimal overconfidencePenalty) { this.overconfidencePenalty = overconfidencePenalty; }
    public BigDecimal getFinalScore() { return finalScore; }
    public void setFinalScore(BigDecimal finalScore) { this.finalScore = finalScore; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
