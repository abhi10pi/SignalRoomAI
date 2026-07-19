package com.example.Backend.repository;

import com.example.Backend.entity.PollVote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PollVoteRepository extends JpaRepository<PollVote, UUID> {
    List<PollVote> findBySignalId(UUID signalId);
    Optional<PollVote> findBySignalIdAndUserId(UUID signalId, UUID userId);
}
