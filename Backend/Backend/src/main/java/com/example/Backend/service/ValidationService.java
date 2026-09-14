package com.example.Backend.service;

import com.example.Backend.dto.request.ValidationRequest;
import com.example.Backend.dto.response.ValidationResponse;
import com.example.Backend.entity.Signal;
import com.example.Backend.entity.User;
import com.example.Backend.entity.Validation;
import com.example.Backend.enums.Outcome;
import com.example.Backend.enums.SignalStatus;
import com.example.Backend.exception.ResourceNotFoundException;
import com.example.Backend.repository.SignalRepository;
import com.example.Backend.repository.UserRepository;
import com.example.Backend.repository.ValidationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ValidationService {

    private final SignalRepository signalRepository;
    private final UserRepository userRepository;
    private final ValidationRepository validationRepository;

    public ValidationService(SignalRepository signalRepository,
                             UserRepository userRepository,
                             ValidationRepository validationRepository) {
        this.signalRepository = signalRepository;
        this.userRepository = userRepository;
        this.validationRepository = validationRepository;
    }

    @Transactional
    public ValidationResponse submitValidation(UUID signalId, UUID consultantId, ValidationRequest req) {
        Signal signal = signalRepository.findById(signalId)
                .orElseThrow(() -> new ResourceNotFoundException("Signal not found"));
        User consultant = userRepository.findById(consultantId)
                .orElseThrow(() -> new ResourceNotFoundException("Consultant not found"));

        if (signal.getStatus() == SignalStatus.CLOSED || signal.getStatus() == SignalStatus.FAILED || signal.getStatus() == SignalStatus.VALIDATED || signal.getStatus() == SignalStatus.REJECTED) {
            throw new IllegalArgumentException("Validation is closed for this signal");
        }

        Validation validation = validationRepository.findBySignalIdAndConsultantId(signalId, consultantId)
                .orElse(new Validation());
        validation.setSignal(signal);
        validation.setConsultant(consultant);
        validation.setPredictedOutcome(req.getPredictedOutcome());
        validation.setConfidence(req.getConfidence());
        validation.setThesis(req.getThesis());
        validation.setWasCorrect(null);
        validation.setResolvedOutcome(null);

        Validation saved = validationRepository.save(validation);
        signal.setStatus(SignalStatus.PENDING_VALIDATION);
        signalRepository.save(signal);
        return toResponse(saved);
    }

    public List<ValidationResponse> getValidations(UUID signalId) {
        return validationRepository.findBySignalId(signalId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<ValidationResponse> getMyValidations(UUID consultantId) {
        return validationRepository.findByConsultantId(consultantId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ValidationResponse approveSignal(UUID signalId) {
        Signal signal = signalRepository.findById(signalId)
                .orElseThrow(() -> new ResourceNotFoundException("Signal not found"));

        signal.setStatus(SignalStatus.VALIDATED);
        signal.setDiscussionEnd(LocalDateTime.now());
        signalRepository.save(signal);

        return toResponse(validationRepository.findBySignalId(signalId).stream().findFirst().orElse(null));
    }

    @Transactional
    public ValidationResponse rejectSignal(UUID signalId) {
        Signal signal = signalRepository.findById(signalId)
                .orElseThrow(() -> new ResourceNotFoundException("Signal not found"));

        signal.setStatus(SignalStatus.REJECTED);
        signal.setDiscussionEnd(LocalDateTime.now());
        signalRepository.save(signal);

        return toResponse(validationRepository.findBySignalId(signalId).stream().findFirst().orElse(null));
    }

    @Transactional
    public ValidationResponse resolveSignal(UUID signalId, Outcome actualOutcome) {
        Signal signal = signalRepository.findById(signalId)
                .orElseThrow(() -> new ResourceNotFoundException("Signal not found"));

        if (signal.getStatus() != SignalStatus.PENDING_VALIDATION && signal.getStatus() != SignalStatus.VALIDATED) {
            throw new IllegalArgumentException("Signal is not pending validation");
        }

        signal.setStatus(SignalStatus.CLOSED);
        signal.setDiscussionEnd(LocalDateTime.now());
        signalRepository.save(signal);

        List<Validation> validations = validationRepository.findBySignalId(signalId);
        for (Validation validation : validations) {
            validation.setResolvedOutcome(actualOutcome);
            validation.setWasCorrect(validation.getPredictedOutcome() == actualOutcome);
            validationRepository.save(validation);
        }

        return validations.isEmpty() ? null : toResponse(validations.get(0));
    }

    private ValidationResponse toResponse(Validation validation) {
        if (validation == null) {
            return null;
        }

        ValidationResponse response = new ValidationResponse();
        response.setId(validation.getId());
        response.setSignalId(validation.getSignal().getId());
        response.setSignalTitle(validation.getSignal().getTitle());
        response.setConsultantId(validation.getConsultant().getId());
        response.setConsultantUsername(validation.getConsultant().getUsername());
        response.setPredictedOutcome(validation.getPredictedOutcome());
        response.setConfidence(validation.getConfidence());
        response.setThesis(validation.getThesis());
        response.setWasCorrect(validation.getWasCorrect());
        response.setCreatedAt(validation.getCreatedAt());
        return response;
    }
}
