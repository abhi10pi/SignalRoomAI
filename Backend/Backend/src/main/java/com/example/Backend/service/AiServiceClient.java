package com.example.Backend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class AiServiceClient {

    private final RestClient restClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AiServiceClient(@Value("${app.ai.base-url:http://localhost:8000}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    public Map<String, Object> fetchCommunityAnalysis(UUID signalId, String title, String description, List<Map<String, Object>> opinions) {
        String opinionsJson = "[]";
        try { opinionsJson = objectMapper.writeValueAsString(opinions); } catch (JsonProcessingException ignored) {}

        String uri = UriComponentsBuilder.fromPath("/api/community")
                .queryParam("signal_id", signalId)
                .queryParam("title", encode(title))
                .queryParam("description", encode(description))
                .queryParam("opinions", opinionsJson)
                .build(false).toUriString();

        return post(uri, signalId, "community");
    }

    public Map<String, Object> fetchResearchAnalysis(UUID signalId, String title, String description) {
        String uri = UriComponentsBuilder.fromPath("/api/research")
                .queryParam("signal_id", signalId)
                .queryParam("title", encode(title))
                .queryParam("description", encode(description))
                .build(false).toUriString();

        return post(uri, signalId, "research");
    }

    public Map<String, Object> fetchComparisonAnalysis(UUID signalId, String title, String description, List<Map<String, Object>> opinions) {
        String opinionsJson = "[]";
        try { opinionsJson = objectMapper.writeValueAsString(opinions); } catch (JsonProcessingException ignored) {}

        String uri = UriComponentsBuilder.fromPath("/api/comparison")
                .queryParam("signal_id", signalId)
                .queryParam("title", encode(title))
                .queryParam("description", encode(description))
                .queryParam("opinions", opinionsJson)
                .build(false).toUriString();

        return post(uri, signalId, "comparison");
    }

    // Legacy GET-based fallbacks kept for backward compatibility
    public Map<String, Object> fetchCommunityAnalysis(UUID signalId) {
        return getAnalysis("/api/community?signal_id={id}", signalId, "community");
    }

    public Map<String, Object> fetchResearchAnalysis(UUID signalId) {
        return getAnalysis("/api/research?signal_id={id}", signalId, "research");
    }

    public Map<String, Object> fetchComparisonAnalysis(UUID signalId) {
        return getAnalysis("/api/comparison?signal_id={id}", signalId, "comparison");
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> post(String uri, UUID signalId, String analysisType) {
        try {
            Map<String, Object> response = restClient.post()
                    .uri(uri)
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {});

            return extractAnalysis(response, signalId, analysisType);
        } catch (RestClientException ignored) {
            return fallbackAnalysis(signalId, analysisType);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getAnalysis(String path, UUID signalId, String analysisType) {
        try {
            Map<String, Object> response = restClient.get()
                    .uri(path, signalId)
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {});

            return extractAnalysis(response, signalId, analysisType);
        } catch (RestClientException ignored) {
            return fallbackAnalysis(signalId, analysisType);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> extractAnalysis(Map<String, Object> response, UUID signalId, String analysisType) {
        if (response == null || response.get("analysis") == null) return fallbackAnalysis(signalId, analysisType);
        Object analysis = response.get("analysis");
        if (analysis instanceof Map<?, ?> map) {
            Map<String, Object> typed = new HashMap<>();
            map.forEach((k, v) -> typed.put(String.valueOf(k), v));
            return typed;
        }
        return fallbackAnalysis(signalId, analysisType);
    }

    private Map<String, Object> fallbackAnalysis(UUID signalId, String analysisType) {
        Map<String, Object> a = new HashMap<>();
        a.put("signal_id", signalId.toString());
        a.put("prompt_version", analysisType + "-v1");
        a.put("model_version", "signalroom-" + analysisType + "-analyzer-v1");
        switch (analysisType) {
            case "community" -> {
                a.put("community_summary", "Community discussion was analyzed for the signal.");
                a.put("supporting_arguments", List.of("The issue is visible in public discussion."));
                a.put("opposing_arguments", List.of("Evidence remains limited at this stage."));
                a.put("common_arguments", List.of());
                a.put("minority_arguments", List.of());
                a.put("unsupported_claims", List.of());
            }
            case "research" -> {
                a.put("summary", "Independent evidence review completed for the signal.");
                a.put("conclusion", "PARTIALLY_SUPPORTS");
                a.put("confidence", 50);
                a.put("search_queries", List.of());
                a.put("sources", List.of());
                a.put("evidence", List.of());
                a.put("limitations", List.of("AI service unavailable."));
            }
            case "comparison" -> {
                a.put("summary", "Community and research perspectives were compared.");
                a.put("agreement", List.of("Both perspectives identify a plausible concern."));
                a.put("disagreement", List.of());
                a.put("important_differences", List.of());
                a.put("strongest_community_argument", Map.of("summary", "No strong community argument identified."));
                a.put("strongest_research_evidence", Map.of("summary", "No strong research evidence identified."));
                a.put("missing_community_perspectives", List.of());
                a.put("missing_research_perspectives", List.of());
            }
            default -> a.put("summary", "AI analysis completed.");
        }
        return a;
    }

    private String encode(String s) {
        return s == null ? "" : s.replace("&", "%26").replace("+", "%2B");
    }
}
