package com.example.Backend.dto.request;

import com.example.Backend.enums.Outcome;
import jakarta.validation.constraints.NotNull;

public class ResolveSignalRequest {
    @NotNull private Outcome actualOutcome;

    public Outcome getActualOutcome() { return actualOutcome; }
    public void setActualOutcome(Outcome actualOutcome) { this.actualOutcome = actualOutcome; }
}
