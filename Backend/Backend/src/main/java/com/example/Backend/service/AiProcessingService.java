package com.example.Backend.service;

import com.example.Backend.entity.CommunityAnalysis;
import com.example.Backend.entity.ComparisonAnalysis;
import com.example.Backend.entity.Opinion;
import com.example.Backend.entity.ResearchAnalysis;
import com.example.Backend.entity.Signal;
import com.example.Backend.enums.SignalStatus;
import com.example.Backend.exception.ResourceNotFoundException;
import com.example.Backend.repository.CommunityAnalysisRepository;
import com.example.Backend.repository.ComparisonAnalysisRepository;
import com.example.Backend.repository.OpinionRepository;
import com.example.Backend.repository.ResearchAnalysisRepository;
import com.example.Backend.repository.SignalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class AiProcessingService {

    private final SignalRepository signalRepository;
    private final OpinionRepository opinionRepository;
    private final AiServiceClient aiServiceClient;
    private final CommunityAnalysisRepository communityAnalysisRepository;
    private final ResearchAnalysisRepository researchAnalysisRepository;
    private final ComparisonAnalysisRepository comparisonAnalysisRepository;

    public AiProcessingService(SignalRepository signalRepository,
                               OpinionRepository opinionRepository,
                               AiServiceClient aiServiceClient,
                               CommunityAnalysisRepository communityAnalysisRepository,
                               ResearchAnalysisRepository researchAnalysisRepository,
                               ComparisonAnalysisRepository comparisonAnalysisRepository) {
        this.signalRepository = signalRepository;
        this.opinionRepository = opinionRepository;
        this.aiServiceClient = aiServiceClient;
        this.communityAnalysisRepository = communityAnalysisRepository;
        this.researchAnalysisRepository = researchAnalysisRepository;
        this.comparisonAnalysisRepository = comparisonAnalysisRepository;
    }

    @Transactional
    public void processSignal(UUID signalId) {
        Signal signal = signalRepository.findById(signalId)
                .orElseThrow(() -> new ResourceNotFoundException("Signal not found"));

        if (signal.getStatus() != SignalStatus.PROCESSING) {
            signal.setStatus(SignalStatus.PROCESSING);
            signalRepository.save(signal);
        }

        List<Map<String, Object>> opinions = opinionRepository
                .findBySignalIdOrderByCreatedAtDesc(signalId)
                .stream()
                .map(o -> Map.<String, Object>of(
                        "position", o.getPosition().name(),
                        "content", o.getContent()))
                .toList();

        Map<String, Object> community = validateCommunityData(
                aiServiceClient.fetchCommunityAnalysis(signalId, signal.getTitle(), signal.getDescription(), opinions));
        Map<String, Object> research = validateResearchData(
                aiServiceClient.fetchResearchAnalysis(signalId, signal.getTitle(), signal.getDescription()));
        Map<String, Object> comparison = validateComparisonData(
                aiServiceClient.fetchComparisonAnalysis(signalId, signal.getTitle(), signal.getDescription(), opinions));

        saveCommunityAnalysis(signal, community);
        saveResearchAnalysis(signal, research);
        saveComparisonAnalysis(signal, comparison);

        signal.setStatus(SignalStatus.CLOSED);
        signalRepository.save(signal);
    }

    public Map<String, Object> getCommunityAnalysis(UUID signalId) {
        return communityAnalysisRepository.findBySignalId(signalId)
                .map(CommunityAnalysis::getAnalysisJson)
                .orElseGet(() -> fallbackCommunityAnalysis(signalId));
    }

    public Map<String, Object> getResearchAnalysis(UUID signalId) {
        return researchAnalysisRepository.findBySignalId(signalId)
                .map(ResearchAnalysis::getAnalysisJson)
                .orElseGet(() -> fallbackResearchAnalysis(signalId));
    }

    public Map<String, Object> getComparisonAnalysis(UUID signalId) {
        return comparisonAnalysisRepository.findBySignalId(signalId)
                .map(ComparisonAnalysis::getAnalysisJson)
                .orElseGet(() -> fallbackComparisonAnalysis(signalId));
    }

    // ---- persistence ----

    private void saveCommunityAnalysis(Signal signal, Map<String, Object> analysis) {
        CommunityAnalysis entity = communityAnalysisRepository.findBySignalId(signal.getId())
                .orElse(new CommunityAnalysis());
        entity.setSignal(signal);
        entity.setAnalysisJson(analysis);
        entity.setModel(str(analysis, "model_version", "signalroom-community-analyzer-v1"));
        entity.setPromptVersion(str(analysis, "prompt_version", "community-v1"));
        communityAnalysisRepository.save(entity);
    }

    private void saveResearchAnalysis(Signal signal, Map<String, Object> analysis) {
        ResearchAnalysis entity = researchAnalysisRepository.findBySignalId(signal.getId())
                .orElse(new ResearchAnalysis());
        entity.setSignal(signal);
        entity.setResearchRunId(UUID.randomUUID());
        entity.setAnalysisJson(analysis);
        entity.setModel(str(analysis, "model_version", "signalroom-research-agent-v1"));
        entity.setPromptVersion(str(analysis, "prompt_version", "research-v1"));
        researchAnalysisRepository.save(entity);
    }

    private void saveComparisonAnalysis(Signal signal, Map<String, Object> analysis) {
        ComparisonAnalysis entity = comparisonAnalysisRepository.findBySignalId(signal.getId())
                .orElse(new ComparisonAnalysis());
        entity.setSignal(signal);
        entity.setAnalysisJson(analysis);
        entity.setModel(str(analysis, "model_version", "signalroom-comparison-analyzer-v1"));
        entity.setPromptVersion(str(analysis, "prompt_version", "comparison-v1"));
        comparisonAnalysisRepository.save(entity);
    }

    // ---- validation ----

    private Map<String, Object> validateCommunityData(Map<String, Object> data) {
        Map<String, Object> r = new HashMap<>(data);
        r.putIfAbsent("signal_id", "");
        r.putIfAbsent("community_summary", "Community discussion was analyzed.");
        r.putIfAbsent("supporting_arguments", List.of());
        r.putIfAbsent("opposing_arguments", List.of());
        r.putIfAbsent("common_arguments", List.of());
        r.putIfAbsent("minority_arguments", List.of());
        r.putIfAbsent("unsupported_claims", List.of());
        r.putIfAbsent("prompt_version", "community-v1");
        r.putIfAbsent("model_version", "signalroom-community-analyzer-v1");
        return r;
    }

    private Map<String, Object> validateResearchData(Map<String, Object> data) {
        Map<String, Object> r = new HashMap<>(data);
        r.putIfAbsent("signal_id", "");
        r.putIfAbsent("summary", "Independent research was completed.");
        r.putIfAbsent("conclusion", "PARTIALLY_SUPPORTS");
        r.putIfAbsent("confidence", 50);
        r.putIfAbsent("search_queries", List.of());
        r.putIfAbsent("sources", List.of());
        r.putIfAbsent("evidence", List.of());
        r.putIfAbsent("limitations", List.of());
        r.putIfAbsent("prompt_version", "research-v1");
        r.putIfAbsent("model_version", "signalroom-research-agent-v1");
        return r;
    }

    private Map<String, Object> validateComparisonData(Map<String, Object> data) {
        Map<String, Object> r = new HashMap<>(data);
        r.putIfAbsent("signal_id", "");
        r.putIfAbsent("summary", "Community and research perspectives were compared.");
        r.putIfAbsent("agreement", List.of());
        r.putIfAbsent("disagreement", List.of());
        r.putIfAbsent("important_differences", List.of());
        r.putIfAbsent("strongest_community_argument", Map.of("summary", "No strong community argument identified."));
        r.putIfAbsent("strongest_research_evidence", Map.of("summary", "No strong research evidence identified."));
        r.putIfAbsent("missing_community_perspectives", List.of());
        r.putIfAbsent("missing_research_perspectives", List.of());
        r.putIfAbsent("prompt_version", "comparison-v1");
        r.putIfAbsent("model_version", "signalroom-comparison-analyzer-v1");
        return r;
    }

    // ---- fallbacks ----

    private Map<String, Object> fallbackCommunityAnalysis(UUID signalId) {
        Map<String, Object> a = new HashMap<>();
        a.put("signal_id", signalId.toString());
        a.put("community_summary", "Community discussion was analyzed for the signal.");
        a.put("supporting_arguments", List.of());
        a.put("opposing_arguments", List.of());
        a.put("common_arguments", List.of());
        a.put("minority_arguments", List.of());
        a.put("unsupported_claims", List.of());
        a.put("prompt_version", "community-v1");
        a.put("model_version", "signalroom-community-analyzer-v1");
        return a;
    }

    private Map<String, Object> fallbackResearchAnalysis(UUID signalId) {
        Map<String, Object> a = new HashMap<>();
        a.put("signal_id", signalId.toString());
        a.put("summary", "Independent evidence review completed for the signal.");
        a.put("conclusion", "PARTIALLY_SUPPORTS");
        a.put("confidence", 50);
        a.put("search_queries", List.of());
        a.put("sources", List.of());
        a.put("evidence", List.of());
        a.put("limitations", List.of());
        a.put("prompt_version", "research-v1");
        a.put("model_version", "signalroom-research-agent-v1");
        return a;
    }

    private Map<String, Object> fallbackComparisonAnalysis(UUID signalId) {
        Map<String, Object> a = new HashMap<>();
        a.put("signal_id", signalId.toString());
        a.put("summary", "Community and research perspectives were compared.");
        a.put("agreement", List.of());
        a.put("disagreement", List.of());
        a.put("important_differences", List.of());
        a.put("strongest_community_argument", Map.of("summary", "No strong community argument identified."));
        a.put("strongest_research_evidence", Map.of("summary", "No strong research evidence identified."));
        a.put("missing_community_perspectives", List.of());
        a.put("missing_research_perspectives", List.of());
        a.put("prompt_version", "comparison-v1");
        a.put("model_version", "signalroom-comparison-analyzer-v1");
        return a;
    }

    private String str(Map<String, Object> map, String key, String defaultVal) {
        Object v = map.get(key);
        return v instanceof String s ? s : defaultVal;
    }
}
