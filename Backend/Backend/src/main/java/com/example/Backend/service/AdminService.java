package com.example.Backend.service;

import com.example.Backend.dto.request.PromotionRequestDTO;
import com.example.Backend.dto.response.PromotionRequestResponse;
import com.example.Backend.entity.PromotionRequest;
import com.example.Backend.entity.User;
import com.example.Backend.enums.PromotionStatus;
import com.example.Backend.enums.Role;
import com.example.Backend.exception.ResourceNotFoundException;
import com.example.Backend.repository.PromotionRequestRepository;
import com.example.Backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AdminService {

    private final PromotionRequestRepository promotionRequestRepository;
    private final UserRepository userRepository;

    public AdminService(PromotionRequestRepository promotionRequestRepository,
                        UserRepository userRepository) {
        this.promotionRequestRepository = promotionRequestRepository;
        this.userRepository = userRepository;
    }

    public PromotionRequestResponse submitPromotionRequest(UUID userId, PromotionRequestDTO dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        boolean hasPending = promotionRequestRepository.findByUserId(userId).stream()
                .anyMatch(r -> r.getStatus() == PromotionStatus.PENDING);
        if (hasPending)
            throw new IllegalArgumentException("You already have a pending promotion request");

        PromotionRequest req = new PromotionRequest();
        req.setUser(user);
        req.setJustification(dto.getJustification());
        return toResponse(promotionRequestRepository.save(req));
    }

    public List<PromotionRequestResponse> getPendingRequests() {
        return promotionRequestRepository.findByStatus(PromotionStatus.PENDING)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<PromotionRequestResponse> getAllRequests() {
        return promotionRequestRepository.findAll()
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public PromotionRequestResponse approveRequest(UUID requestId, UUID adminId) {
        PromotionRequest req = getRequest(requestId);
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found"));
        req.setStatus(PromotionStatus.APPROVED);
        req.setReviewedBy(admin);
        req.setReviewedAt(LocalDateTime.now());
        promotionRequestRepository.save(req);
        User user = req.getUser();
        user.setRole(Role.CONSULTANT);
        userRepository.save(user);
        return toResponse(req);
    }

    @Transactional
    public PromotionRequestResponse rejectRequest(UUID requestId, UUID adminId) {
        PromotionRequest req = getRequest(requestId);
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found"));
        req.setStatus(PromotionStatus.REJECTED);
        req.setReviewedBy(admin);
        req.setReviewedAt(LocalDateTime.now());
        return toResponse(promotionRequestRepository.save(req));
    }

    public List<PromotionRequestResponse> getMyRequests(UUID userId) {
        return promotionRequestRepository.findByUserId(userId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional
    public void setUserRole(UUID userId, Role role) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setRole(role);
        userRepository.save(user);
    }

    private PromotionRequest getRequest(UUID id) {
        return promotionRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found"));
    }

    private PromotionRequestResponse toResponse(PromotionRequest r) {
        PromotionRequestResponse res = new PromotionRequestResponse();
        res.setId(r.getId());
        res.setUserId(r.getUser().getId());
        res.setUsername(r.getUser().getUsername());
        res.setJustification(r.getJustification());
        res.setStatus(r.getStatus());
        res.setReviewedByUsername(r.getReviewedBy() != null ? r.getReviewedBy().getUsername() : null);
        res.setCreatedAt(r.getCreatedAt());
        res.setReviewedAt(r.getReviewedAt());
        return res;
    }
}
