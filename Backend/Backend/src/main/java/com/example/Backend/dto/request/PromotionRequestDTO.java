package com.example.Backend.dto.request;

import jakarta.validation.constraints.NotBlank;

public class PromotionRequestDTO {
    @NotBlank private String justification;

    public String getJustification() { return justification; }
    public void setJustification(String justification) { this.justification = justification; }
}
