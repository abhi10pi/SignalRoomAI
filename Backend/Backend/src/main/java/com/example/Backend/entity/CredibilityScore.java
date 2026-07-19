package com.example.Backend.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "credibility_scores",uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "domain_id"}))
public class CredibilityScore {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "domain_id", nullable = false)
    private Domain domain;

    @Column(name = "total_signals", nullable = false)
    private int totalSignals = 0;

    @Column(name = "correct_signals", nullable = false)
    private int correctSignals = 0;

    @Column(name = "total_validations", nullable = false)
    private int totalValidations = 0;

    @Column(name = "correct_validations", nullable = false)
    private int correctValidations = 0;

    @Column(name = "accuracy_score", nullable = false, precision = 5, scale = 4)
    private BigDecimal accuracyScore = new BigDecimal("0.5");

    @Column(name = "overconfidence_penalty", nullable = false, precision = 5, scale = 4)
    private BigDecimal overconfidencePenalty = BigDecimal.ZERO;

    @Column(name = "final_score", nullable = false, precision = 5, scale = 4)
    private BigDecimal finalScore = new BigDecimal("0.5");

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    public UUID getId() { return id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Domain getDomain() { return domain; }
    public void setDomain(Domain domain) { this.domain = domain; }
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
