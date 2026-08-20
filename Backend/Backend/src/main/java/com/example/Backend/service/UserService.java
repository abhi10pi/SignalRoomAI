package com.example.Backend.service;

import com.example.Backend.dto.response.CredibilityScoreResponse;
import com.example.Backend.dto.response.UserProfileResponse;
import com.example.Backend.entity.CredibilityScore;
import com.example.Backend.entity.User;
import com.example.Backend.exception.ResourceNotFoundException;
import com.example.Backend.repository.CredibilityScoreRepository;
import com.example.Backend.repository.SignalRepository;
import com.example.Backend.repository.UserRepository;
import com.example.Backend.repository.ValidationRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final SignalRepository signalRepository;
    private final ValidationRepository validationRepository;
    private final CredibilityScoreRepository credibilityScoreRepository;

    public UserService(UserRepository userRepository, SignalRepository signalRepository,
                       ValidationRepository validationRepository,
                       CredibilityScoreRepository credibilityScoreRepository) {
        this.userRepository = userRepository;
        this.signalRepository = signalRepository;
        this.validationRepository = validationRepository;
        this.credibilityScoreRepository = credibilityScoreRepository;
    }

    public UserProfileResponse getProfileByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return buildProfile(user);
    }

    public UserProfileResponse getMyProfile(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return buildProfile(user);
    }

    private UserProfileResponse buildProfile(User user) {
        List<CredibilityScore> scores = credibilityScoreRepository.findByUserId(user.getId());
        UserProfileResponse r = new UserProfileResponse();
        r.setId(user.getId());
        r.setUsername(user.getUsername());
        r.setBio(user.getBio());
        r.setRole(user.getRole().name());
        r.setTotalSignals(signalRepository.findBySubmitterId(user.getId()).size());
        r.setTotalValidations(validationRepository.findByConsultantId(user.getId()).size());
        r.setCredibilityScores(scores.stream().map(this::toScoreResponse).collect(Collectors.toList()));
        return r;
    }

    private CredibilityScoreResponse toScoreResponse(CredibilityScore s) {
        CredibilityScoreResponse r = new CredibilityScoreResponse();
        r.setId(s.getId());
        r.setDomainName(s.getDomain().getName());
        r.setDomainSlug(s.getDomain().getSlug());
        r.setTotalSignals(s.getTotalSignals());
        r.setCorrectSignals(s.getCorrectSignals());
        r.setTotalValidations(s.getTotalValidations());
        r.setCorrectValidations(s.getCorrectValidations());
        r.setAccuracyScore(s.getAccuracyScore());
        r.setOverconfidencePenalty(s.getOverconfidencePenalty());
        r.setFinalScore(s.getFinalScore());
        r.setUpdatedAt(s.getUpdatedAt());
        return r;
    }
}
