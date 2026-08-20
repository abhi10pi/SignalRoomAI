package com.example.Backend.dto.response;

import com.example.Backend.enums.Confidence;
import com.example.Backend.enums.Outcome;
import java.time.LocalDateTime;
import java.util.UUID;

public class ValidationResponse {
    private UUID id;
    private UUID signalId;
    private UUID consultantId;
    private String consultantUsername;
    private Outcome predictedOutcome;
    private Confidence confidence;
    private String thesis;
    private Boolean wasCorrect;
    private LocalDateTime createdAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getSignalId() { return signalId; }
    public void setSignalId(UUID signalId) { this.signalId = signalId; }
    public UUID getConsultantId() { return consultantId; }
    public void setConsultantId(UUID consultantId) { this.consultantId = consultantId; }
    public String getConsultantUsername() { return consultantUsername; }
    public void setConsultantUsername(String consultantUsername) { this.consultantUsername = consultantUsername; }
    public Outcome getPredictedOutcome() { return predictedOutcome; }
    public void setPredictedOutcome(Outcome predictedOutcome) { this.predictedOutcome = predictedOutcome; }
    public Confidence getConfidence() { return confidence; }
    public void setConfidence(Confidence confidence) { this.confidence = confidence; }
    public String getThesis() { return thesis; }
    public void setThesis(String thesis) { this.thesis = thesis; }
    public Boolean getWasCorrect() { return wasCorrect; }
    public void setWasCorrect(Boolean wasCorrect) { this.wasCorrect = wasCorrect; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
