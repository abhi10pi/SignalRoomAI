package com.example.Backend.service;

import com.example.Backend.dto.request.CreateSignalRequest;
import com.example.Backend.dto.request.UpdateSignalRequest;
import com.example.Backend.dto.response.SignalResponse;
import com.example.Backend.dto.response.SignalSummaryResponse;
import com.example.Backend.entity.Domain;
import com.example.Backend.entity.Signal;
import com.example.Backend.entity.User;
import com.example.Backend.enums.SignalStatus;
import com.example.Backend.exception.ResourceNotFoundException;
import com.example.Backend.exception.UnauthorizedException;
import com.example.Backend.mapper.SignalMapper;
import com.example.Backend.repository.DomainRepository;
import com.example.Backend.repository.SignalRepository;
import com.example.Backend.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SignalService {

    private final SignalRepository signalRepository;
    private final UserRepository userRepository;
    private final DomainRepository domainRepository;
    private final SignalMapper signalMapper;

    public SignalService(SignalRepository signalRepository, UserRepository userRepository,
                         DomainRepository domainRepository, SignalMapper signalMapper) {
        this.signalRepository = signalRepository;
        this.userRepository = userRepository;
        this.domainRepository = domainRepository;
        this.signalMapper = signalMapper;
    }

    public SignalResponse createDraft(UUID userId, CreateSignalRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Domain domain = domainRepository.findById(req.getDomainId())
                .orElseThrow(() -> new ResourceNotFoundException("Domain not found"));

        Signal signal = new Signal();
        signal.setSubmitter(user);
        signal.setDomain(domain);
        signal.setTitle(req.getTitle());
        signal.setDescription(req.getDescription());
        signal.setResolutionType(req.getResolutionType());
        signal.setResolutionCriteria(req.getResolutionCriteria());
        signal.setResolutionDate(req.getResolutionDate());
        signal.setVisibility(req.getVisibility());
        signal.setStatus(SignalStatus.DRAFT);

        return signalMapper.toResponse(signalRepository.save(signal));
    }

    public SignalResponse updateDraft(UUID signalId, UUID userId, UpdateSignalRequest req) {
        Signal signal = getOwnedDraft(signalId, userId);

        if (req.getTitle() != null) signal.setTitle(req.getTitle());
        if (req.getDescription() != null) signal.setDescription(req.getDescription());
        if (req.getResolutionType() != null) signal.setResolutionType(req.getResolutionType());
        if (req.getResolutionCriteria() != null) signal.setResolutionCriteria(req.getResolutionCriteria());
        if (req.getResolutionDate() != null) signal.setResolutionDate(req.getResolutionDate());
        if (req.getVisibility() != null) signal.setVisibility(req.getVisibility());
        if (req.getDomainId() != null) {
            Domain domain = domainRepository.findById(req.getDomainId())
                    .orElseThrow(() -> new ResourceNotFoundException("Domain not found"));
            signal.setDomain(domain);
        }

        return signalMapper.toResponse(signalRepository.save(signal));
    }

    public void deleteDraft(UUID signalId, UUID userId) {
        Signal signal = getOwnedDraft(signalId, userId);
        signalRepository.delete(signal);
    }

    public SignalResponse publishSignal(UUID signalId, UUID userId) {
        Signal signal = getOwnedDraft(signalId, userId);

        if (signal.getResolutionDate().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Resolution date must be in the future");
        }

        signal.setStatus(SignalStatus.PENDING_VALIDATION);
        signal.setSubmittedAt(LocalDateTime.now());

        return signalMapper.toResponse(signalRepository.save(signal));
    }

    public SignalResponse getSignal(UUID signalId) {
        Signal signal = signalRepository.findById(signalId)
                .orElseThrow(() -> new ResourceNotFoundException("Signal not found"));
        return signalMapper.toResponse(signal);
    }

    public List<SignalSummaryResponse> getMySignals(UUID userId) {
        return signalRepository.findBySubmitterId(userId).stream()
                .map(signalMapper::toSummary)
                .collect(Collectors.toList());
    }

    public Page<SignalSummaryResponse> getPublicSignals(int page, int size, String sort) {
        Sort sortOrder = sort != null && sort.equals("oldest")
                ? Sort.by("createdAt").ascending()
                : Sort.by("createdAt").descending();
        Pageable pageable = PageRequest.of(page, size, sortOrder);
        return signalRepository.findPublic(pageable).map(signalMapper::toSummary);
    }

    public Page<SignalSummaryResponse> getPublicByDomainSlug(String slug, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return signalRepository.findPublicByDomainSlug(slug, pageable).map(signalMapper::toSummary);
    }

    public Page<SignalSummaryResponse> searchSignals(String q, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return signalRepository.search(q, pageable).map(signalMapper::toSummary);
    }

    private Signal getOwnedDraft(UUID signalId, UUID userId) {
        Signal signal = signalRepository.findById(signalId)
                .orElseThrow(() -> new ResourceNotFoundException("Signal not found"));
        if (!signal.getSubmitter().getId().equals(userId)) {
            throw new UnauthorizedException("Not your signal");
        }
        if (signal.getStatus() != SignalStatus.DRAFT) {
            throw new IllegalArgumentException("Signal is not a draft");
        }
        return signal;
    }
}
