package com.example.Backend.repository;

import com.example.Backend.entity.ResolutionVote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ResolutionVoteRepository extends JpaRepository<ResolutionVote, UUID> {
    List<ResolutionVote> findBySignalId(UUID signalId);
    long countBySignalId(UUID signalId);
}
