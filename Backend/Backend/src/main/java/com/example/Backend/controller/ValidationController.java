package com.example.Backend.controller;

import com.example.Backend.dto.request.ResolveSignalRequest;
import com.example.Backend.dto.request.ValidationRequest;
import com.example.Backend.dto.response.SignalResponse;
import com.example.Backend.dto.response.ValidationResponse;
import com.example.Backend.service.ResolutionService;
import com.example.Backend.service.ValidationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/signals")
public class ValidationController {

    private final ValidationService validationService;
    private final ResolutionService resolutionService;

    public ValidationController(ValidationService validationService, ResolutionService resolutionService) {
        this.validationService = validationService;
        this.resolutionService = resolutionService;
    }

    // POST /api/signals/{id}/validate
    @PostMapping("/{id}/validate")
    @ResponseStatus(HttpStatus.CREATED)
    public ValidationResponse submitValidation(@PathVariable UUID id,
                                               @Valid @RequestBody ValidationRequest req,
                                               Authentication auth) {
        return validationService.submitValidation(id, currentUserId(auth), req);
    }

    // GET /api/signals/{id}/validations
    @GetMapping("/{id}/validations")
    public List<ValidationResponse> getValidations(@PathVariable UUID id) {
        return validationService.getValidationsForSignal(id);
    }

    // POST /api/signals/{id}/approve  (admin/consultant)
    @PostMapping("/{id}/approve")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void approveSignal(@PathVariable UUID id, Authentication auth) {
        validationService.approveSignal(id);
    }

    // POST /api/signals/{id}/reject  (admin/consultant)
    @PostMapping("/{id}/reject")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void rejectSignal(@PathVariable UUID id, Authentication auth) {
        validationService.rejectSignal(id);
    }

    // POST /api/signals/{id}/resolve  (admin)
    @PostMapping("/{id}/resolve")
    public SignalResponse resolveSignal(@PathVariable UUID id,
                                        @Valid @RequestBody ResolveSignalRequest req,
                                        Authentication auth) {
        return resolutionService.resolveSignal(id, req);
    }

    // GET /api/signals/my-validations
    @GetMapping("/my-validations")
    public List<ValidationResponse> getMyValidations(Authentication auth) {
        return validationService.getMyValidations(currentUserId(auth));
    }

    private UUID currentUserId(Authentication auth) {
        return (UUID) auth.getPrincipal();
    }
}
