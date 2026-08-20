package com.example.Backend.dto.request;

import com.example.Backend.enums.Confidence;
import com.example.Backend.enums.Outcome;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ValidationRequest {
    @NotNull private Outcome predictedOutcome;
    @NotNull private Confidence confidence;
    @NotBlank private String thesis;

    public Outcome getPredictedOutcome() { return predictedOutcome; }
    public void setPredictedOutcome(Outcome predictedOutcome) { this.predictedOutcome = predictedOutcome; }
    public Confidence getConfidence() { return confidence; }
    public void setConfidence(Confidence confidence) { this.confidence = confidence; }
    public String getThesis() { return thesis; }
    public void setThesis(String thesis) { this.thesis = thesis; }
}
