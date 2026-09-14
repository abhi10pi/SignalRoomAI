from pydantic import BaseModel, Field
from typing import List, Optional
from enum import Enum


class EvidenceType(str, Enum):
    SUPPORTING = "SUPPORTING"
    CONTRADICTING = "CONTRADICTING"
    CONTEXT = "CONTEXT"


class ResearchConclusion(str, Enum):
    STRONGLY_SUPPORTS = "STRONGLY_SUPPORTS"
    PARTIALLY_SUPPORTS = "PARTIALLY_SUPPORTS"
    NEUTRAL = "NEUTRAL"
    PARTIALLY_CONTRADICTS = "PARTIALLY_CONTRADICTS"
    STRONGLY_CONTRADICTS = "STRONGLY_CONTRADICTS"
    INSUFFICIENT_EVIDENCE = "INSUFFICIENT_EVIDENCE"


class CommunityAnalysisResult(BaseModel):
    signal_id: str
    community_summary: str
    supporting_arguments: List[str] = Field(default_factory=list)
    opposing_arguments: List[str] = Field(default_factory=list)
    common_arguments: List[str] = Field(default_factory=list)
    minority_arguments: List[str] = Field(default_factory=list)
    unsupported_claims: List[str] = Field(default_factory=list)
    prompt_version: str = "community-v1"
    model_version: str = "signalroom-community-analyzer-v1"


class SourceItem(BaseModel):
    url: str
    title: Optional[str] = None
    publisher: Optional[str] = None
    published_at: Optional[str] = None
    source_type: Optional[str] = None


class EvidenceItem(BaseModel):
    claim: str
    evidence_text: str
    evidence_type: EvidenceType
    relevance_score: float = Field(ge=0.0, le=1.0)
    source_url: Optional[str] = None


class ResearchAnalysisResult(BaseModel):
    signal_id: str
    search_queries: List[str] = Field(default_factory=list)
    sources: List[SourceItem] = Field(default_factory=list)
    evidence: List[EvidenceItem] = Field(default_factory=list)
    summary: str
    conclusion: ResearchConclusion
    confidence: int = Field(ge=0, le=100)
    limitations: List[str] = Field(default_factory=list)
    prompt_version: str = "research-v1"
    model_version: str = "signalroom-research-agent-v1"


class StrongestArgument(BaseModel):
    summary: str
    source: Optional[str] = None


class ComparisonAnalysisResult(BaseModel):
    signal_id: str
    summary: str
    agreement: List[str] = Field(default_factory=list)
    disagreement: List[str] = Field(default_factory=list)
    important_differences: List[str] = Field(default_factory=list)
    strongest_community_argument: StrongestArgument
    strongest_research_evidence: StrongestArgument
    missing_community_perspectives: List[str] = Field(default_factory=list)
    missing_research_perspectives: List[str] = Field(default_factory=list)
    community_conclusion: Optional[str] = None
    research_conclusion: Optional[str] = None
    prompt_version: str = "comparison-v1"
    model_version: str = "signalroom-comparison-analyzer-v1"
