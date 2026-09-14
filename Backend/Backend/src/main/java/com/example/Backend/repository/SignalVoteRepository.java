package com.example.Backend.repository;

import com.example.Backend.entity.SignalVote;
import com.example.Backend.enums.VoteType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SignalVoteRepository extends JpaRepository<SignalVote, UUID> {

    Optional<SignalVote> findBySignalIdAndUserId(UUID signalId, UUID userId);

    long countBySignalIdAndVoteType(UUID signalId, VoteType voteType);

    long countBySignalId(UUID signalId);
}
