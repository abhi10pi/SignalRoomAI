package com.example.Backend.service;

import com.example.Backend.dto.request.CreateOpinionRequest;
import com.example.Backend.dto.response.OpinionResponse;
import com.example.Backend.entity.*;
import com.example.Backend.enums.SignalStatus;
import com.example.Backend.enums.VoteType;
import com.example.Backend.exception.ResourceNotFoundException;
import com.example.Backend.exception.UnauthorizedException;
import com.example.Backend.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OpinionService {

    private final OpinionRepository opinionRepository;
    private final OpinionVoteRepository opinionVoteRepository;
    private final SignalRepository signalRepository;
    private final UserRepository userRepository;

    public OpinionService(OpinionRepository opinionRepository,
                          OpinionVoteRepository opinionVoteRepository,
                          SignalRepository signalRepository,
                          UserRepository userRepository) {
        this.opinionRepository = opinionRepository;
        this.opinionVoteRepository = opinionVoteRepository;
        this.signalRepository = signalRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public OpinionResponse createOpinion(UUID signalId, UUID userId, CreateOpinionRequest req) {
        Signal signal = signalRepository.findById(signalId)
                .orElseThrow(() -> new ResourceNotFoundException("Signal not found"));
        if (signal.getStatus() != SignalStatus.OPEN) {
            throw new IllegalArgumentException("Signal is not open for opinions");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Opinion opinion = new Opinion();
        opinion.setSignal(signal);
        opinion.setUser(user);
        opinion.setPosition(req.getPosition());
        opinion.setContent(req.getContent());

        for (CreateOpinionRequest.SourceRequest sr : req.getSources()) {
            if (sr.getUrl() == null || sr.getUrl().isBlank()) continue;
            OpinionSource os = new OpinionSource();
            os.setOpinion(opinion);
            os.setUrl(sr.getUrl());
            os.setTitle(sr.getTitle());
            opinion.getSources().add(os);
        }

        return toResponse(opinionRepository.save(opinion), userId);
    }

    public List<OpinionResponse> getOpinions(UUID signalId, UUID currentUserId) {
        return opinionRepository.findBySignalIdOrderByCreatedAtDesc(signalId).stream()
                .map(o -> toResponse(o, currentUserId))
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteOpinion(UUID opinionId, UUID userId) {
        Opinion opinion = opinionRepository.findById(opinionId)
                .orElseThrow(() -> new ResourceNotFoundException("Opinion not found"));
        if (!opinion.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("Not your opinion");
        }
        opinionRepository.delete(opinion);
    }

    @Transactional
    public OpinionResponse voteOpinion(UUID opinionId, UUID userId, VoteType voteType) {
        Opinion opinion = opinionRepository.findById(opinionId)
                .orElseThrow(() -> new ResourceNotFoundException("Opinion not found"));
        if (opinion.getSignal().getStatus() != SignalStatus.OPEN) {
            throw new IllegalArgumentException("Signal is closed");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        opinionVoteRepository.findByOpinionIdAndUserId(opinionId, userId).ifPresentOrElse(
                v -> { v.setVoteType(voteType); opinionVoteRepository.save(v); },
                () -> {
                    OpinionVote v = new OpinionVote();
                    v.setOpinion(opinion);
                    v.setUser(user);
                    v.setVoteType(voteType);
                    opinionVoteRepository.save(v);
                }
        );

        return toResponse(opinion, userId);
    }

    @Transactional
    public void removeOpinionVote(UUID opinionId, UUID userId) {
        opinionVoteRepository.findByOpinionIdAndUserId(opinionId, userId)
                .ifPresent(opinionVoteRepository::delete);
    }

    private OpinionResponse toResponse(Opinion o, UUID currentUserId) {
        OpinionResponse r = new OpinionResponse();
        r.setId(o.getId());
        r.setSignalId(o.getSignal().getId());
        r.setUserId(o.getUser().getId());
        r.setUsername(o.getUser().getUsername());
        r.setPosition(o.getPosition());
        r.setContent(o.getContent());
        r.setCreatedAt(o.getCreatedAt());
        r.setUpdatedAt(o.getUpdatedAt());

        r.setSources(o.getSources().stream().map(src -> {
            OpinionResponse.SourceResponse sr = new OpinionResponse.SourceResponse();
            sr.setId(src.getId());
            sr.setUrl(src.getUrl());
            sr.setTitle(src.getTitle());
            return sr;
        }).collect(Collectors.toList()));

        long up = opinionVoteRepository.countByOpinionIdAndVoteType(o.getId(), VoteType.UP);
        long down = opinionVoteRepository.countByOpinionIdAndVoteType(o.getId(), VoteType.DOWN);
        r.setUpVotes(up);
        r.setDownVotes(down);

        if (currentUserId != null) {
            opinionVoteRepository.findByOpinionIdAndUserId(o.getId(), currentUserId)
                    .ifPresent(v -> r.setMyVote(v.getVoteType().name()));
        }

        return r;
    }
}
