package com.example.Backend.repository;

import com.example.Backend.entity.Signal;
import com.example.Backend.enums.SignalStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SignalRepository extends JpaRepository<Signal, UUID> {
    List<Signal> findBySubmitterId(UUID submitterId);
    List<Signal> findByStatus(SignalStatus status);
    List<Signal> findByDomainIdAndStatus(UUID domainId, SignalStatus status);
}
