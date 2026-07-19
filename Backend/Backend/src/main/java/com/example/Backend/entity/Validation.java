package com.example.Backend.entity;

import com.example.Backend.enums.Confidence;
import com.example.Backend.enums.Outcome;
import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "validations",
        uniqueConstraints = @UniqueConstraint(columnNames = {"signal_id", "consultant_id"}))
public class Validation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "signal_id", nullable = false)
    private Signal signal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consultant_id", nullable = false)
    private User consultant;

    @Enumerated(EnumType.STRING)
    @Column(name = "predicted_outcome", nullable = false)
    private Outcome predictedOutcome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Confidence confidence;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String thesis;

    @Column(name = "was_correct")
    private Boolean wasCorrect;

    public UUID getId() { return id; }
    public Signal getSignal() { return signal; }
    public void setSignal(Signal signal) { this.signal = signal; }
    public User getConsultant() { return consultant; }
    public void setConsultant(User consultant) { this.consultant = consultant; }
    public Outcome getPredictedOutcome() { return predictedOutcome; }
    public void setPredictedOutcome(Outcome predictedOutcome) { this.predictedOutcome = predictedOutcome; }
    public Confidence getConfidence() { return confidence; }
    public void setConfidence(Confidence confidence) { this.confidence = confidence; }
    public String getThesis() { return thesis; }
    public void setThesis(String thesis) { this.thesis = thesis; }
    public Boolean getWasCorrect() { return wasCorrect; }
    public void setWasCorrect(Boolean wasCorrect) { this.wasCorrect = wasCorrect; }
}
