package com.example.Backend.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "research_analyses")
public class ResearchAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "signal_id", nullable = false)
    private Signal signal;

    @Column(name = "research_run_id")
    private UUID researchRunId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "analysis_json", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> analysisJson;

    @Column
    private String model;

    @Column(name = "prompt_version")
    private String promptVersion;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public UUID getId() { return id; }
    public Signal getSignal() { return signal; }
    public void setSignal(Signal signal) { this.signal = signal; }
    public UUID getResearchRunId() { return researchRunId; }
    public void setResearchRunId(UUID researchRunId) { this.researchRunId = researchRunId; }
    public Map<String, Object> getAnalysisJson() { return analysisJson; }
    public void setAnalysisJson(Map<String, Object> analysisJson) { this.analysisJson = analysisJson; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public String getPromptVersion() { return promptVersion; }
    public void setPromptVersion(String promptVersion) { this.promptVersion = promptVersion; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
