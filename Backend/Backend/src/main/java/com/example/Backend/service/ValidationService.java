package com.example.Backend.service;

import com.example.Backend.dto.request.ValidationRequest;
import com.example.Backend.dto.response.ValidationResponse;
import com.example.Backend.entity.Signal;
import com.example.Backend.entity.User;
import com.example.Backend.entity.Validation;
import com.example.Backend.enums.SignalStatus;
import com.example.Backend.exception.ResourceNotFoundException;
import com.example.Backend.repository.SignalRepository;
import com.example.Backend.repository.UserRepository;
import com.example.Backend.repository.ValidationRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ValidationService {

    private final ValidationRepository validationRepository;
    private final SignalRepository signalRepository;
    private final UserRepository userRepository;

    public ValidationService(ValidationRepository validationRepository,
                              SignalRepository signalRepository,
                              UserRepository userRepository) {
        this.validationRepository = validationRepository;
        this.signalRepository = signalRepository;
        this.userRepository = userRepository;
    }

    /**
     * Submit validation for a signal.
     * Allows validations on PENDING_VALIDATION or VALIDATED status (ongoing consultations allowed).
     * Does NOT allow validations after EVALUATED status.
     */
    public ValidationResponse submitValidation(UUID signalId, UUID consultantId, ValidationRequest req) {
        Signal signal = signalRepository.findById(signalId)
                .orElseThrow(() -> new ResourceNotFoundException("Signal not found"));

        // Allow validations until the signal is resolved (EVALUATED)
        if (signal.getStatus() == SignalStatus.EVALUATED || signal.getStatus() == SignalStatus.REJECTED)
            throw new IllegalArgumentException("Signal is closed for further validations");

        if (signal.getStatus() != SignalStatus.PENDING_VALIDATION && signal.getStatus() != SignalStatus.VALIDATED)
            throw new IllegalArgumentException("Signal is not open for validation");

        if (signal.getSubmitter().getId().equals(consultantId))
            throw new IllegalArgumentException("Cannot validate your own signal");

        // Allow same consultant to update their validation
        var existing = validationRepository.findBySignalIdAndConsultantId(signalId, consultantId);
        if (existing.isPresent()) {
            Validation v = existing.get();
            v.setPredictedOutcome(req.getPredictedOutcome());
            v.setConfidence(req.getConfidence());
            v.setThesis(req.getThesis());
            return toResponse(validationRepository.save(v));
        }

        User consultant = userRepository.findById(consultantId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Validation v = new Validation();
        v.setSignal(signal);
        v.setConsultant(consultant);
        v.setPredictedOutcome(req.getPredictedOutcome());
        v.setConfidence(req.getConfidence());
        v.setThesis(req.getThesis());

        return toResponse(validationRepository.save(v));
    }

    public List<ValidationResponse> getValidationsForSignal(UUID signalId) {
        if (!signalRepository.existsById(signalId))
            throw new ResourceNotFoundException("Signal not found");
        return validationRepository.findBySignalId(signalId).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    /**
     * Approve signal for resolution - moves to VALIDATED status if currently PENDING_VALIDATION.
     * Any number of consultants can continue validating after this point.
     */
    public void approveSignal(UUID signalId) {
        Signal signal = signalRepository.findById(signalId)
                .orElseThrow(() -> new ResourceNotFoundException("Signal not found"));
        if (signal.getStatus() != SignalStatus.PENDING_VALIDATION)
            throw new IllegalArgumentException("Signal is not pending validation");
        signal.setStatus(SignalStatus.VALIDATED);
        signalRepository.save(signal);
    }

    public void rejectSignal(UUID signalId) {
        Signal signal = signalRepository.findById(signalId)
                .orElseThrow(() -> new ResourceNotFoundException("Signal not found"));
        if (signal.getStatus() != SignalStatus.PENDING_VALIDATION)
            throw new IllegalArgumentException("Signal is not pending validation");
        signal.setStatus(SignalStatus.REJECTED);
        signalRepository.save(signal);
    }

    public List<ValidationResponse> getMyValidations(UUID consultantId) {
        return validationRepository.findByConsultantId(consultantId).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    private ValidationResponse toResponse(Validation v) {
        ValidationResponse r = new ValidationResponse();
        r.setId(v.getId());
        r.setSignalId(v.getSignal().getId());
        r.setConsultantId(v.getConsultant().getId());
        r.setConsultantUsername(v.getConsultant().getUsername());
        r.setPredictedOutcome(v.getPredictedOutcome());
        r.setConfidence(v.getConfidence());
        r.setThesis(v.getThesis());
        r.setWasCorrect(v.getWasCorrect());
        r.setCreatedAt(v.getCreatedAt());
        return r;
    }
}
