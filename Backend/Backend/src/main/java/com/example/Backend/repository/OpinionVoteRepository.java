package com.example.Backend.repository;

import com.example.Backend.entity.OpinionVote;
import com.example.Backend.enums.VoteType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OpinionVoteRepository extends JpaRepository<OpinionVote, UUID> {

    Optional<OpinionVote> findByOpinionIdAndUserId(UUID opinionId, UUID userId);

    long countByOpinionIdAndVoteType(UUID opinionId, VoteType voteType);
}
