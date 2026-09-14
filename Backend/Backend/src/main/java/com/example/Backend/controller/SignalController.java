package com.example.Backend.controller;

import com.example.Backend.dto.request.CreateSignalRequest;
import com.example.Backend.dto.request.ResolveSignalRequest;
import com.example.Backend.dto.request.ValidationRequest;
import com.example.Backend.dto.response.SignalResponse;
import com.example.Backend.dto.response.SignalSummaryResponse;
import com.example.Backend.dto.response.ValidationResponse;
import com.example.Backend.enums.Outcome;
import com.example.Backend.service.AiProcessingService;
import com.example.Backend.service.SignalService;
import com.example.Backend.service.ValidationService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class SignalController {

    private final SignalService signalService;
    private final ValidationService validationService;
    private final AiProcessingService aiProcessingService;

    public SignalController(SignalService signalService, ValidationService validationService,
                            AiProcessingService aiProcessingService) {
        this.signalService = signalService;
        this.validationService = validationService;
        this.aiProcessingService = aiProcessingService;
    }

    @PostMapping("/signals")
    @ResponseStatus(HttpStatus.CREATED)
    public SignalResponse createSignal(@Valid @RequestBody CreateSignalRequest req, Authentication auth) {
        return signalService.createSignal(userId(auth), req);
    }

    @GetMapping("/signals/{id}")
    public SignalResponse getSignal(@PathVariable UUID id, Authentication auth) {
        UUID currentUser = auth != null ? (UUID) auth.getPrincipal() : null;
        return signalService.getSignal(id, currentUser);
    }

    @GetMapping("/signals")
    public Page<SignalSummaryResponse> getFeed(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String category) {
        if (category != null && !category.isBlank()) {
            return signalService.getByCategory(category, page, size);
        }
        return signalService.getPublicFeed(page, size, sort);
    }

    @GetMapping("/signals/search")
    public Page<SignalSummaryResponse> search(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return signalService.search(q, page, size);
    }

    @GetMapping("/users/me/signals")
    public List<SignalSummaryResponse> mySignals(Authentication auth) {
        return signalService.getMySignals(userId(auth));
    }

    @PostMapping("/signals/{id}/validate")
    @ResponseStatus(HttpStatus.CREATED)
    public ValidationResponse submitValidation(@PathVariable UUID id,
                                              @Valid @RequestBody ValidationRequest req,
                                              Authentication auth) {
        return validationService.submitValidation(id, userId(auth), req);
    }

    @GetMapping("/signals/{id}/validations")
    public List<ValidationResponse> getValidations(@PathVariable UUID id) {
        return validationService.getValidations(id);
    }

    @GetMapping("/signals/my-validations")
    public List<ValidationResponse> getMyValidations(Authentication auth) {
        return validationService.getMyValidations(userId(auth));
    }

    @PostMapping("/signals/{id}/approve")
    public ValidationResponse approveSignal(@PathVariable UUID id) {
        return validationService.approveSignal(id);
    }

    @PostMapping("/signals/{id}/reject")
    public ValidationResponse rejectSignal(@PathVariable UUID id) {
        return validationService.rejectSignal(id);
    }

    @PostMapping("/signals/{id}/resolve")
    public ValidationResponse resolveSignal(@PathVariable UUID id,
                                           @Valid @RequestBody ResolveSignalRequest req) {
        return validationService.resolveSignal(id, req.getActualOutcome());
    }

    @DeleteMapping("/signals/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSignal(@PathVariable UUID id, Authentication auth) {
        signalService.deleteSignal(id, userId(auth));
    }

    @GetMapping("/signals/{id}/community-analysis")
    public Map<String, Object> getCommunityAnalysis(@PathVariable UUID id) {
        return aiProcessingService.getCommunityAnalysis(id);
    }

    @GetMapping("/signals/{id}/research-analysis")
    public Map<String, Object> getResearchAnalysis(@PathVariable UUID id) {
        return aiProcessingService.getResearchAnalysis(id);
    }

    @GetMapping("/signals/{id}/comparison-analysis")
    public Map<String, Object> getComparisonAnalysis(@PathVariable UUID id) {
        return aiProcessingService.getComparisonAnalysis(id);
    }

    private UUID userId(Authentication auth) {
        return (UUID) auth.getPrincipal();
    }
}
