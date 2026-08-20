package com.example.Backend.service;

import com.example.Backend.dto.request.ResolveSignalRequest;
import com.example.Backend.dto.response.SignalResponse;
import com.example.Backend.entity.Signal;
import com.example.Backend.entity.Validation;
import com.example.Backend.enums.Confidence;
import com.example.Backend.enums.Outcome;
import com.example.Backend.enums.SignalStatus;
import com.example.Backend.exception.ResourceNotFoundException;
import com.example.Backend.mapper.SignalMapper;
import com.example.Backend.repository.SignalRepository;
import com.example.Backend.repository.ValidationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ResolutionService {

    private final SignalRepository signalRepository;
    private final ValidationRepository validationRepository;
    private final CredibilityService credibilityService;
    private final SignalMapper signalMapper;

    public ResolutionService(SignalRepository signalRepository,
                              ValidationRepository validationRepository,
                              CredibilityService credibilityService,
                              SignalMapper signalMapper) {
        this.signalRepository = signalRepository;
        this.validationRepository = validationRepository;
        this.credibilityService = credibilityService;
        this.signalMapper = signalMapper;
    }

    /**
     * Resolve signal with optional admin-provided outcome.
     * If no outcome provided, calculates from consultant validations using weighted voting.
     */
    @Transactional
    public SignalResponse resolveSignal(UUID signalId, ResolveSignalRequest req) {
        Signal signal = signalRepository.findById(signalId)
                .orElseThrow(() -> new ResourceNotFoundException("Signal not found"));

        if (signal.getStatus() != SignalStatus.VALIDATED)
            throw new IllegalArgumentException("Only VALIDATED signals can be resolved");

        Outcome actualOutcome = req.getActualOutcome();
        
        // If no outcome provided, calculate from validations
        if (actualOutcome == null) {
            actualOutcome = calculateOutcomeFromValidations(signalId);
        }

        signal.setActualOutcome(actualOutcome);
        signal.setStatus(SignalStatus.EVALUATED);
        signal.setEvaluatedAt(LocalDateTime.now());
        signalRepository.save(signal);

        credibilityService.recalculateAfterResolution(signal, actualOutcome);

        return signalMapper.toResponse(signal);
    }

    /**
     * Calculate final outcome based on consultant validations using weighted voting.
     * Weighting formula:
     *  - HIGH/CERTAIN confidence = 3 points
     *  - MEDIUM confidence = 2 points
     *  - LOW confidence = 1 point
     */
    public Outcome calculateOutcomeFromValidations(UUID signalId) {
        List<Validation> validations = validationRepository.findBySignalId(signalId);
        
        if (validations.isEmpty()) {
            return Outcome.AMBIGUOUS;
        }

        int trueScore = 0;
        int falseScore = 0;
        int ambiguousScore = 0;

        for (Validation v : validations) {
            int weight = getConfidenceWeight(v.getConfidence());
            
            switch (v.getPredictedOutcome()) {
                case TRUE:
                    trueScore += weight;
                    break;
                case FALSE:
                    falseScore += weight;
                    break;
                case AMBIGUOUS:
                    ambiguousScore += weight;
                    break;
            }
        }

        // Determine majority outcome
        if (trueScore > falseScore && trueScore > ambiguousScore) {
            return Outcome.TRUE;
        } else if (falseScore > trueScore && falseScore > ambiguousScore) {
            return Outcome.FALSE;
        } else if (ambiguousScore > trueScore && ambiguousScore > falseScore) {
            return Outcome.AMBIGUOUS;
        } else if (trueScore == falseScore && trueScore > ambiguousScore) {
            // Tie between TRUE and FALSE -> AMBIGUOUS
            return Outcome.AMBIGUOUS;
        }
        
        // Default to AMBIGUOUS if all tied or no consensus
        return Outcome.AMBIGUOUS;
    }

    private int getConfidenceWeight(Confidence confidence) {
        return switch (confidence) {
            case HIGH, CERTAIN -> 3;
            case MEDIUM -> 2;
            case LOW -> 1;
        };
    }
}
