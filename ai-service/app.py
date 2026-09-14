import hashlib
import re
from typing import List, Optional
from fastapi import FastAPI, Query, HTTPException
from schemas import (
    CommunityAnalysisResult,
    ResearchAnalysisResult,
    ComparisonAnalysisResult,
    EvidenceItem,
    EvidenceType,
    ResearchConclusion,
    SourceItem,
    StrongestArgument,
)

app = FastAPI(title="Signalroom AI Service")

# ---------------------------------------------------------------------------
# Community Analysis — Phase 6
# ---------------------------------------------------------------------------

def _community_analysis(signal_id: str, title: str, description: str, opinions: list) -> CommunityAnalysisResult:
    """
    Derive community analysis from signal content and submitted opinions.
    In production this would call an LLM; here we produce a deterministic
    structured result from the available text so the contract is always valid.
    """
    supporting = [o["content"] for o in opinions if o.get("position") == "AGREE"][:5]
    opposing = [o["content"] for o in opinions if o.get("position") == "DISAGREE"][:5]
    neutral = [o["content"] for o in opinions if o.get("position") == "NEUTRAL"][:3]

    if not supporting:
        supporting = [f"The signal '{title}' reflects a concern visible in public discourse."]
    if not opposing:
        opposing = ["The evidence base remains limited without independent verification."]

    return CommunityAnalysisResult(
        signal_id=signal_id,
        community_summary=(
            f"Community discussion for '{title}' was reviewed. "
            f"{len(supporting)} supporting and {len(opposing)} opposing arguments were identified."
        ),
        supporting_arguments=supporting,
        opposing_arguments=opposing,
        common_arguments=neutral or ["The signal should be evaluated with evidence and stakeholder impact in mind."],
        minority_arguments=["The issue may be overrepresented in a single community segment."],
        unsupported_claims=[],
    )


# ---------------------------------------------------------------------------
# Research Analysis — Phase 7
# ---------------------------------------------------------------------------

def _generate_search_queries(title: str, description: str) -> List[str]:
    """Generate multiple search queries from the signal text."""
    words = re.sub(r"[^\w\s]", "", title).split()
    base = " ".join(words[:6])
    queries = [
        base,
        f"{base} evidence",
        f"{base} research study",
        f"{base} statistics data",
        f"{base} expert opinion",
    ]
    return list(dict.fromkeys(q.strip() for q in queries if q.strip()))


def _content_hash(url: str) -> str:
    return hashlib.sha256(url.encode()).hexdigest()[:16]


def _deduplicate_sources(sources: List[SourceItem]) -> List[SourceItem]:
    seen: set = set()
    result = []
    for s in sources:
        key = _content_hash(s.url)
        if key not in seen:
            seen.add(key)
            result.append(s)
    return result


def _rank_sources(sources: List[SourceItem]) -> List[SourceItem]:
    """Rank: prefer sources with title and publisher over bare URLs."""
    return sorted(sources, key=lambda s: (s.title is not None, s.publisher is not None), reverse=True)


def _classify_evidence(title: str, sources: List[SourceItem]) -> List[EvidenceItem]:
    """
    Produce evidence items with SUPPORTING / CONTRADICTING / CONTEXT classification.
    In production this calls an LLM with each source excerpt.
    """
    evidence = []
    for i, source in enumerate(sources[:6]):
        if i % 3 == 0:
            ev_type = EvidenceType.SUPPORTING
            claim = f"Evidence supports the concern raised in '{title}'."
            text = f"Source '{source.title or source.url}' provides context consistent with the signal claim."
        elif i % 3 == 1:
            ev_type = EvidenceType.CONTRADICTING
            claim = f"Evidence challenges the certainty of the claim in '{title}'."
            text = f"Source '{source.title or source.url}' presents data that limits high-confidence conclusions."
        else:
            ev_type = EvidenceType.CONTEXT
            claim = f"Background context relevant to '{title}'."
            text = f"Source '{source.title or source.url}' provides background information."
        evidence.append(EvidenceItem(
            claim=claim,
            evidence_text=text,
            evidence_type=ev_type,
            relevance_score=round(max(0.4, 0.9 - i * 0.08), 3),
            source_url=source.url,
        ))
    return evidence


def _research_analysis(signal_id: str, title: str, description: str) -> ResearchAnalysisResult:
    queries = _generate_search_queries(title, description)

    # Simulated external sources — in production these come from a search API
    raw_sources = [
        SourceItem(url=f"https://example.org/source/{_content_hash(q)}", title=f"Study on {q}", publisher="Research Institute", source_type="ACADEMIC")
        for q in queries[:3]
    ] + [
        SourceItem(url=f"https://news.example.com/{_content_hash(title)}", title=f"News: {title[:40]}", publisher="News Outlet", source_type="NEWS"),
        SourceItem(url=f"https://gov.example.org/{_content_hash(description[:20])}", title="Government Report", publisher="Gov Agency", source_type="GOVERNMENT"),
    ]

    sources = _rank_sources(_deduplicate_sources(raw_sources))
    evidence = _classify_evidence(title, sources)

    supporting_count = sum(1 for e in evidence if e.evidence_type == EvidenceType.SUPPORTING)
    contradicting_count = sum(1 for e in evidence if e.evidence_type == EvidenceType.CONTRADICTING)

    if supporting_count > contradicting_count * 2:
        conclusion = ResearchConclusion.STRONGLY_SUPPORTS
        confidence = 82
    elif supporting_count > contradicting_count:
        conclusion = ResearchConclusion.PARTIALLY_SUPPORTS
        confidence = 65
    elif contradicting_count > supporting_count:
        conclusion = ResearchConclusion.PARTIALLY_CONTRADICTS
        confidence = 60
    else:
        conclusion = ResearchConclusion.INSUFFICIENT_EVIDENCE
        confidence = 40

    return ResearchAnalysisResult(
        signal_id=signal_id,
        search_queries=queries,
        sources=sources,
        evidence=evidence,
        summary=(
            f"Independent evidence review for '{title}' identified {len(sources)} sources "
            f"and {len(evidence)} evidence items. "
            f"{supporting_count} supporting, {contradicting_count} contradicting."
        ),
        conclusion=conclusion,
        confidence=confidence,
        limitations=[
            "Sources are simulated; production requires a live search API.",
            "Evidence classification is rule-based pending LLM integration.",
        ],
    )


# ---------------------------------------------------------------------------
# Comparison Analysis — Phase 8
# ---------------------------------------------------------------------------

def _comparison_analysis(
    signal_id: str,
    community: CommunityAnalysisResult,
    research: ResearchAnalysisResult,
) -> ComparisonAnalysisResult:
    agreement = []
    disagreement = []
    differences = []

    research_supports = research.conclusion in (
        ResearchConclusion.STRONGLY_SUPPORTS, ResearchConclusion.PARTIALLY_SUPPORTS
    )
    community_positive = len(community.supporting_arguments) >= len(community.opposing_arguments)

    if research_supports and community_positive:
        agreement.append("Both community discussion and research evidence identify a plausible concern.")
    elif not research_supports and not community_positive:
        agreement.append("Both community and research perspectives express significant doubt.")
    else:
        disagreement.append(
            "Community sentiment is more confident than the research evidence currently supports."
            if community_positive else
            "Research evidence is more supportive than community discussion reflects."
        )

    if research.confidence < 60:
        differences.append("Research confidence is low; community certainty may be overstated.")
    if len(community.unsupported_claims) > 0:
        differences.append(f"{len(community.unsupported_claims)} community claims lack supporting evidence.")

    strongest_community = StrongestArgument(
        summary=community.supporting_arguments[0] if community.supporting_arguments else "No strong community argument identified.",
    )
    strongest_research = StrongestArgument(
        summary=research.evidence[0].evidence_text if research.evidence else "No strong research evidence identified.",
        source=research.evidence[0].source_url if research.evidence else None,
    )

    missing_community = ["Underrepresented stakeholder viewpoints.", "Long-term impact perspectives."]
    missing_research = ["Primary-source verification.", "Independent impact studies."]

    return ComparisonAnalysisResult(
        signal_id=signal_id,
        summary=(
            f"Community and research perspectives were compared. "
            f"Research conclusion: {research.conclusion.value} (confidence {research.confidence}%). "
            f"Community: {len(community.supporting_arguments)} supporting vs {len(community.opposing_arguments)} opposing arguments."
        ),
        agreement=agreement,
        disagreement=disagreement,
        important_differences=differences or ["Community sentiment is broader; research view is more conservative."],
        strongest_community_argument=strongest_community,
        strongest_research_evidence=strongest_research,
        missing_community_perspectives=missing_community,
        missing_research_perspectives=missing_research,
        community_conclusion="POSITIVE" if community_positive else "NEGATIVE",
        research_conclusion=research.conclusion.value,
    )


# ---------------------------------------------------------------------------
# Endpoints
# ---------------------------------------------------------------------------

@app.get("/health")
def health():
    return {"status": "UP", "service": "Signalroom AI Service"}


@app.post("/api/community")
def community(
    signal_id: str = Query(...),
    title: str = Query(default=""),
    description: str = Query(default=""),
    opinions: Optional[str] = Query(default=None),
):
    import json
    parsed_opinions = []
    if opinions:
        try:
            parsed_opinions = json.loads(opinions)
        except Exception:
            raise HTTPException(status_code=400, detail="opinions must be valid JSON array")

    result = _community_analysis(signal_id, title, description, parsed_opinions)
    return {"status": "ok", "analysis": result.model_dump()}


@app.get("/api/community")
def community_get(signal_id: str = Query(default="unknown")):
    result = _community_analysis(signal_id, f"Signal {signal_id}", "", [])
    return {"status": "ok", "analysis": result.model_dump()}


@app.post("/api/research")
def research(
    signal_id: str = Query(...),
    title: str = Query(default=""),
    description: str = Query(default=""),
):
    result = _research_analysis(signal_id, title, description)
    return {"status": "ok", "analysis": result.model_dump()}


@app.get("/api/research")
def research_get(signal_id: str = Query(default="unknown")):
    result = _research_analysis(signal_id, f"Signal {signal_id}", "")
    return {"status": "ok", "analysis": result.model_dump()}


@app.post("/api/comparison")
def comparison(
    signal_id: str = Query(...),
    title: str = Query(default=""),
    description: str = Query(default=""),
    opinions: Optional[str] = Query(default=None),
):
    import json
    parsed_opinions = []
    if opinions:
        try:
            parsed_opinions = json.loads(opinions)
        except Exception:
            raise HTTPException(status_code=400, detail="opinions must be valid JSON array")

    community_result = _community_analysis(signal_id, title, description, parsed_opinions)
    research_result = _research_analysis(signal_id, title, description)
    result = _comparison_analysis(signal_id, community_result, research_result)
    return {"status": "ok", "analysis": result.model_dump()}


@app.get("/api/comparison")
def comparison_get(signal_id: str = Query(default="unknown")):
    community_result = _community_analysis(signal_id, f"Signal {signal_id}", "", [])
    research_result = _research_analysis(signal_id, f"Signal {signal_id}", "")
    result = _comparison_analysis(signal_id, community_result, research_result)
    return {"status": "ok", "analysis": result.model_dump()}
