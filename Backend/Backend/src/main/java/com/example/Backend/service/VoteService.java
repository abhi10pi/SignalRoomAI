package com.example.Backend.service;

import com.example.Backend.entity.Signal;
import com.example.Backend.entity.SignalVote;
import com.example.Backend.entity.User;
import com.example.Backend.enums.SignalStatus;
import com.example.Backend.enums.VoteType;
import com.example.Backend.exception.ResourceNotFoundException;
import com.example.Backend.repository.SignalRepository;
import com.example.Backend.repository.SignalVoteRepository;
import com.example.Backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class VoteService {

    private final SignalVoteRepository signalVoteRepository;
    private final SignalRepository signalRepository;
    private final UserRepository userRepository;

    public VoteService(SignalVoteRepository signalVoteRepository,
                       SignalRepository signalRepository,
                       UserRepository userRepository) {
        this.signalVoteRepository = signalVoteRepository;
        this.signalRepository = signalRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Map<String, Object> vote(UUID signalId, UUID userId, VoteType voteType) {
        Signal signal = signalRepository.findById(signalId)
                .orElseThrow(() -> new ResourceNotFoundException("Signal not found"));
        if (signal.getStatus() != SignalStatus.OPEN) {
            throw new IllegalArgumentException("Voting is closed for this signal");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Optional<SignalVote> existing = signalVoteRepository.findBySignalIdAndUserId(signalId, userId);
        if (existing.isPresent()) {
            SignalVote vote = existing.get();
            vote.setVoteType(voteType);
            signalVoteRepository.save(vote);
        } else {
            SignalVote vote = new SignalVote();
            vote.setSignal(signal);
            vote.setUser(user);
            vote.setVoteType(voteType);
            signalVoteRepository.save(vote);
        }

        return buildStats(signalId);
    }

    @Transactional
    public Map<String, Object> removeVote(UUID signalId, UUID userId) {
        Signal signal = signalRepository.findById(signalId)
                .orElseThrow(() -> new ResourceNotFoundException("Signal not found"));
        if (signal.getStatus() != SignalStatus.OPEN) {
            throw new IllegalArgumentException("Voting is closed for this signal");
        }

        signalVoteRepository.findBySignalIdAndUserId(signalId, userId)
                .ifPresent(signalVoteRepository::delete);

        return buildStats(signalId);
    }

    private Map<String, Object> buildStats(UUID signalId) {
        long up = signalVoteRepository.countBySignalIdAndVoteType(signalId, VoteType.UP);
        long down = signalVoteRepository.countBySignalIdAndVoteType(signalId, VoteType.DOWN);
        long total = up + down;
        double upPct = total > 0 ? Math.round((up * 100.0 / total) * 10.0) / 10.0 : 0;
        double downPct = total > 0 ? Math.round((down * 100.0 / total) * 10.0) / 10.0 : 0;
        return Map.of("upVotes", up, "downVotes", down, "totalVotes", total,
                      "upPercent", upPct, "downPercent", downPct);
    }
}
