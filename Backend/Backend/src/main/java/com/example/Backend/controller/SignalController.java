package com.example.Backend.controller;

import com.example.Backend.dto.request.CreateSignalRequest;
import com.example.Backend.dto.request.UpdateSignalRequest;
import com.example.Backend.dto.response.SignalResponse;
import com.example.Backend.dto.response.SignalSummaryResponse;
import com.example.Backend.service.SignalService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class SignalController {

    private final SignalService signalService;

    public SignalController(SignalService signalService) {
        this.signalService = signalService;
    }

    // POST /api/signals — create draft
    @PostMapping("/signals")
    @ResponseStatus(HttpStatus.CREATED)
    public SignalResponse createSignal(@Valid @RequestBody CreateSignalRequest req,
                                       Authentication auth) {
        return signalService.createDraft(currentUserId(auth), req);
    }

    // PUT /api/signals/{id} — edit draft
    @PutMapping("/signals/{id}")
    public SignalResponse updateSignal(@PathVariable UUID id,
                                       @Valid @RequestBody UpdateSignalRequest req,
                                       Authentication auth) {
        return signalService.updateDraft(id, currentUserId(auth), req);
    }

    // DELETE /api/signals/{id} — delete draft
    @DeleteMapping("/signals/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSignal(@PathVariable UUID id, Authentication auth) {
        signalService.deleteDraft(id, currentUserId(auth));
    }

    // POST /api/signals/{id}/publish — publish signal
    @PostMapping("/signals/{id}/publish")
    public SignalResponse publishSignal(@PathVariable UUID id, Authentication auth) {
        return signalService.publishSignal(id, currentUserId(auth));
    }

    // GET /api/signals/{id} — signal detail
    @GetMapping("/signals/{id}")
    public SignalResponse getSignal(@PathVariable UUID id) {
        return signalService.getSignal(id);
    }

    // GET /api/users/me/signals — my signals
    @GetMapping("/users/me/signals")
    public List<SignalSummaryResponse> getMySignals(Authentication auth) {
        return signalService.getMySignals(currentUserId(auth));
    }

    // GET /api/signals — public feed
    @GetMapping("/signals")
    public Page<SignalSummaryResponse> getPublicFeed(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sort) {
        return signalService.getPublicSignals(page, size, sort);
    }

    // GET /api/signals/domain/{slug} — domain feed
    @GetMapping("/signals/domain/{slug}")
    public Page<SignalSummaryResponse> getDomainFeed(
            @PathVariable String slug,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return signalService.getPublicByDomainSlug(slug, page, size);
    }

    // GET /api/signals/search?q=... — search
    @GetMapping("/signals/search")
    public Page<SignalSummaryResponse> searchSignals(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return signalService.searchSignals(q, page, size);
    }

    private UUID currentUserId(Authentication auth) {
        return (UUID) auth.getPrincipal();
    }
}
