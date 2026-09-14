package com.example.Backend.controller;

import com.example.Backend.dto.request.CreateOpinionRequest;
import com.example.Backend.dto.request.VoteRequest;
import com.example.Backend.dto.response.OpinionResponse;
import com.example.Backend.service.OpinionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class OpinionController {

    private final OpinionService opinionService;

    public OpinionController(OpinionService opinionService) {
        this.opinionService = opinionService;
    }

    @PostMapping("/signals/{signalId}/opinions")
    @ResponseStatus(HttpStatus.CREATED)
    public OpinionResponse createOpinion(@PathVariable UUID signalId,
                                         @Valid @RequestBody CreateOpinionRequest req,
                                         Authentication auth) {
        return opinionService.createOpinion(signalId, userId(auth), req);
    }

    @GetMapping("/signals/{signalId}/opinions")
    public List<OpinionResponse> getOpinions(@PathVariable UUID signalId, Authentication auth) {
        UUID currentUser = auth != null ? (UUID) auth.getPrincipal() : null;
        return opinionService.getOpinions(signalId, currentUser);
    }

    @DeleteMapping("/opinions/{opinionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteOpinion(@PathVariable UUID opinionId, Authentication auth) {
        opinionService.deleteOpinion(opinionId, userId(auth));
    }

    @PostMapping("/opinions/{opinionId}/vote")
    public OpinionResponse voteOpinion(@PathVariable UUID opinionId,
                                       @Valid @RequestBody VoteRequest req,
                                       Authentication auth) {
        return opinionService.voteOpinion(opinionId, userId(auth), req.getVoteType());
    }

    @DeleteMapping("/opinions/{opinionId}/vote")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeOpinionVote(@PathVariable UUID opinionId, Authentication auth) {
        opinionService.removeOpinionVote(opinionId, userId(auth));
    }

    private UUID userId(Authentication auth) {
        return (UUID) auth.getPrincipal();
    }
}
