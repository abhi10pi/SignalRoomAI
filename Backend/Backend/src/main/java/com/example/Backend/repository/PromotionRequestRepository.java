package com.example.Backend.repository;

import com.example.Backend.entity.PromotionRequest;
import com.example.Backend.enums.PromotionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PromotionRequestRepository extends JpaRepository<PromotionRequest, UUID> {
    List<PromotionRequest> findByUserId(UUID userId);

    List<PromotionRequest> findByStatus(PromotionStatus status);

}