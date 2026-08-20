package com.example.Backend.repository;

import com.example.Backend.entity.Validation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ValidationRepository extends JpaRepository<Validation, UUID> {
    List<Validation> findBySignalId(UUID signalId);
    List<Validation> findByConsultantId(UUID consultantId);
    List<Validation> findByConsultantIdAndSignalDomainId(UUID consultantId, UUID domainId);
    Optional<Validation> findBySignalIdAndConsultantId(UUID signalId, UUID consultantId);
    long countBySignalId(UUID signalId);
}
