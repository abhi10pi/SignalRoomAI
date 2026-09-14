package com.example.Backend.repository;

import com.example.Backend.entity.ComparisonAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ComparisonAnalysisRepository extends JpaRepository<ComparisonAnalysis, UUID> {
    Optional<ComparisonAnalysis> findBySignalId(UUID signalId);
}
