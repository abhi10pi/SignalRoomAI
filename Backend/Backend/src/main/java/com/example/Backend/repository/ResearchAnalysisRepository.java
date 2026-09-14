package com.example.Backend.repository;

import com.example.Backend.entity.ResearchAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ResearchAnalysisRepository extends JpaRepository<ResearchAnalysis, UUID> {
    Optional<ResearchAnalysis> findBySignalId(UUID signalId);
}
