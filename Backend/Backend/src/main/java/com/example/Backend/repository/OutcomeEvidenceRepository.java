package com.example.Backend.repository;

import com.example.Backend.entity.OutcomeEvidence;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OutcomeEvidenceRepository extends JpaRepository<OutcomeEvidence, UUID> {
    List<OutcomeEvidence> findBySignalId(UUID signalId);
}
