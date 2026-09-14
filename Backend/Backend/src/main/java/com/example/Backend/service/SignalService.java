package com.example.Backend.service;

import com.example.Backend.dto.request.CreateSignalRequest;
import com.example.Backend.dto.response.SignalResponse;
import com.example.Backend.dto.response.SignalSummaryResponse;
import com.example.Backend.entity.*;
import com.example.Backend.enums.SignalStatus;
import com.example.Backend.enums.VoteType;
import com.example.Backend.exception.ResourceNotFoundException;
import com.example.Backend.exception.UnauthorizedException;
import com.example.Backend.repository.*;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SignalService {

    private final SignalRepository signalRepository;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;
    private final SignalVoteRepository signalVoteRepository;

    public SignalService(SignalRepository signalRepository, UserRepository userRepository,
                         TagRepository tagRepository, SignalVoteRepository signalVoteRepository) {
        this.signalRepository = signalRepository;
        this.userRepository = userRepository;
        this.tagRepository = tagRepository;
        this.signalVoteRepository = signalVoteRepository;
    }

    @Transactional
    public SignalResponse createSignal(UUID userId, CreateSignalRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Signal signal = new Signal();
        signal.setAuthor(user);
        signal.setTitle(req.getTitle());
        signal.setDescription(req.getDescription());
        signal.setCategory(req.getCategory());
        signal.setStatus(SignalStatus.OPEN);
        signal.setDiscussionStart(LocalDateTime.now());
        signal.setDiscussionEnd(LocalDateTime.now().plusDays(7));

        // Tags
        for (String tagName : req.getTags()) {
            String normalized = tagName.trim().toLowerCase();
            if (normalized.isEmpty()) continue;
            Tag tag = tagRepository.findByName(normalized)
                    .orElseGet(() -> { Tag t = new Tag(); t.setName(normalized); return tagRepository.save(t); });
            SignalTag st = new SignalTag();
            st.setSignal(signal);
            st.setTag(tag);
            signal.getSignalTags().add(st);
        }

        // Sources
        for (CreateSignalRequest.SourceRequest sr : req.getSources()) {
            if (sr.getUrl() == null || sr.getUrl().isBlank()) continue;
            SignalSource ss = new SignalSource();
            ss.setSignal(signal);
            ss.setUrl(sr.getUrl());
            ss.setTitle(sr.getTitle());
            ss.setDescription(sr.getDescription());
            signal.getSources().add(ss);
        }

        return toResponse(signalRepository.save(signal), null);
    }

    public SignalResponse getSignal(UUID signalId, UUID currentUserId) {
        Signal signal = signalRepository.findById(signalId)
                .orElseThrow(() -> new ResourceNotFoundException("Signal not found"));
        return toResponse(signal, currentUserId);
    }

    public Page<SignalSummaryResponse> getPublicFeed(int page, int size, String sort) {
        Sort sortOrder = "oldest".equals(sort)
                ? Sort.by("createdAt").ascending()
                : Sort.by("createdAt").descending();
        return signalRepository.findPublic(PageRequest.of(page, size, sortOrder))
                .map(s -> toSummary(s));
    }

    public Page<SignalSummaryResponse> getByCategory(String category, int page, int size) {
        return signalRepository.findPublicByCategory(category, PageRequest.of(page, size, Sort.by("createdAt").descending()))
                .map(s -> toSummary(s));
    }

    public Page<SignalSummaryResponse> search(String q, int page, int size) {
        return signalRepository.search(q, PageRequest.of(page, size, Sort.by("createdAt").descending()))
                .map(s -> toSummary(s));
    }

    public List<SignalSummaryResponse> getMySignals(UUID userId) {
        return signalRepository.findByAuthorId(userId).stream()
                .map(s -> toSummary(s))
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteSignal(UUID signalId, UUID userId) {
        Signal signal = signalRepository.findById(signalId)
                .orElseThrow(() -> new ResourceNotFoundException("Signal not found"));
        if (!signal.getAuthor().getId().equals(userId)) {
            throw new UnauthorizedException("Not your signal");
        }
        if (signal.getStatus() != SignalStatus.OPEN) {
            throw new IllegalArgumentException("Only OPEN signals can be deleted");
        }
        signalRepository.delete(signal);
    }

    // ---- helpers ----

    private SignalResponse toResponse(Signal s, UUID currentUserId) {
        SignalResponse r = new SignalResponse();
        r.setId(s.getId());
        r.setTitle(s.getTitle());
        r.setDescription(s.getDescription());
        r.setCategory(s.getCategory());
        r.setStatus(s.getStatus());
        r.setDiscussionStart(s.getDiscussionStart());
        r.setDiscussionEnd(s.getDiscussionEnd());
        r.setCreatedAt(s.getCreatedAt());
        r.setUpdatedAt(s.getUpdatedAt());
        r.setAuthorId(s.getAuthor().getId());
        r.setAuthorUsername(s.getAuthor().getUsername());

        r.setTags(s.getSignalTags().stream()
                .map(st -> st.getTag().getName())
                .collect(Collectors.toList()));

        r.setSources(s.getSources().stream().map(src -> {
            SignalResponse.SourceResponse sr = new SignalResponse.SourceResponse();
            sr.setId(src.getId());
            sr.setUrl(src.getUrl());
            sr.setTitle(src.getTitle());
            sr.setDescription(src.getDescription());
            return sr;
        }).collect(Collectors.toList()));

        long up = signalVoteRepository.countBySignalIdAndVoteType(s.getId(), VoteType.UP);
        long down = signalVoteRepository.countBySignalIdAndVoteType(s.getId(), VoteType.DOWN);
        long total = up + down;
        r.setUpVotes(up);
        r.setDownVotes(down);
        r.setTotalVotes(total);
        r.setUpPercent(total > 0 ? Math.round((up * 100.0 / total) * 10.0) / 10.0 : 0);
        r.setDownPercent(total > 0 ? Math.round((down * 100.0 / total) * 10.0) / 10.0 : 0);

        if (currentUserId != null) {
            signalVoteRepository.findBySignalIdAndUserId(s.getId(), currentUserId)
                    .ifPresent(v -> r.setMyVote(v.getVoteType().name()));
        }

        return r;
    }

    private SignalSummaryResponse toSummary(Signal s) {
        SignalSummaryResponse r = new SignalSummaryResponse();
        r.setId(s.getId());
        r.setTitle(s.getTitle());
        r.setCategory(s.getCategory());
        r.setStatus(s.getStatus());
        r.setDiscussionEnd(s.getDiscussionEnd());
        r.setCreatedAt(s.getCreatedAt());
        r.setAuthorUsername(s.getAuthor().getUsername());
        r.setTags(s.getSignalTags().stream()
                .map(st -> st.getTag().getName())
                .collect(Collectors.toList()));

        long up = signalVoteRepository.countBySignalIdAndVoteType(s.getId(), VoteType.UP);
        long down = signalVoteRepository.countBySignalIdAndVoteType(s.getId(), VoteType.DOWN);
        long total = up + down;
        r.setUpVotes(up);
        r.setDownVotes(down);
        r.setTotalVotes(total);
        r.setUpPercent(total > 0 ? Math.round((up * 100.0 / total) * 10.0) / 10.0 : 0);
        return r;
    }
}
