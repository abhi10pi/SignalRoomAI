package com.example.Backend.repository;

import com.example.Backend.entity.CommunityAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CommunityAnalysisRepository extends JpaRepository<CommunityAnalysis, UUID> {
    Optional<CommunityAnalysis> findBySignalId(UUID signalId);
}
