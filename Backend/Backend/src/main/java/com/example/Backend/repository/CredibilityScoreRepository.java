package com.example.Backend.repository;

import com.example.Backend.entity.CredibilityScore;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CredibilityScoreRepository extends JpaRepository<CredibilityScore, UUID> {
    Optional<CredibilityScore> findByUserIdAndDomainId(UUID userId, UUID domainId);
    List<CredibilityScore> findByUserId(UUID userId);
    List<CredibilityScore> findByDomainIdOrderByFinalScoreDesc(UUID domainId);
}
