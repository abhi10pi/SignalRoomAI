package com.example.Backend.controller;

import com.example.Backend.dto.request.VoteRequest;
import com.example.Backend.service.VoteService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/signals/{signalId}/vote")
public class VoteController {

    private final VoteService voteService;

    public VoteController(VoteService voteService) {
        this.voteService = voteService;
    }

    @PostMapping
    public Map<String, Object> vote(@PathVariable UUID signalId,
                                    @Valid @RequestBody VoteRequest req,
                                    Authentication auth) {
        return voteService.vote(signalId, userId(auth), req.getVoteType());
    }

    @DeleteMapping
    public Map<String, Object> removeVote(@PathVariable UUID signalId, Authentication auth) {
        return voteService.removeVote(signalId, userId(auth));
    }

    private UUID userId(Authentication auth) {
        return (UUID) auth.getPrincipal();
    }
}
