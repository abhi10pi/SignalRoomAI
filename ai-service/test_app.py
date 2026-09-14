import json
import unittest

from fastapi.testclient import TestClient

from app import app
from schemas import CommunityAnalysisResult, ResearchAnalysisResult, ComparisonAnalysisResult


class AiServiceContractTests(unittest.TestCase):
    def setUp(self):
        self.client = TestClient(app)

    # ── Health ──────────────────────────────────────────────────────────────

    def test_health(self):
        r = self.client.get("/health")
        self.assertEqual(r.status_code, 200)
        self.assertEqual(r.json()["status"], "UP")

    # ── Community Analysis ──────────────────────────────────────────────────

    def test_community_get_returns_valid_contract(self):
        r = self.client.get("/api/community", params={"signal_id": "sig-1"})
        self.assertEqual(r.status_code, 200)
        payload = r.json()["analysis"]
        result = CommunityAnalysisResult(**payload)  # validates schema
        self.assertEqual(result.signal_id, "sig-1")
        self.assertIsInstance(result.supporting_arguments, list)
        self.assertIsInstance(result.opposing_arguments, list)
        self.assertIsInstance(result.unsupported_claims, list)
        self.assertIsNotNone(result.prompt_version)
        self.assertIsNotNone(result.model_version)

    def test_community_post_with_opinions(self):
        opinions = json.dumps([
            {"position": "AGREE", "content": "This is a real problem."},
            {"position": "DISAGREE", "content": "Insufficient evidence."},
        ])
        r = self.client.post("/api/community", params={
            "signal_id": "sig-2", "title": "Test Signal", "description": "A test.", "opinions": opinions
        })
        self.assertEqual(r.status_code, 200)
        payload = r.json()["analysis"]
        self.assertIn("This is a real problem.", payload["supporting_arguments"])
        self.assertIn("Insufficient evidence.", payload["opposing_arguments"])

    def test_community_post_invalid_opinions_returns_400(self):
        r = self.client.post("/api/community", params={
            "signal_id": "sig-3", "opinions": "not-json"
        })
        self.assertEqual(r.status_code, 400)

    def test_community_summary_is_not_empty(self):
        r = self.client.get("/api/community", params={"signal_id": "sig-4"})
        summary = r.json()["analysis"]["community_summary"]
        self.assertTrue(len(summary) > 10, "community_summary must not be empty")

    # ── Research Analysis ───────────────────────────────────────────────────

    def test_research_get_returns_valid_contract(self):
        r = self.client.get("/api/research", params={"signal_id": "sig-5"})
        self.assertEqual(r.status_code, 200)
        payload = r.json()["analysis"]
        result = ResearchAnalysisResult(**payload)
        self.assertEqual(result.signal_id, "sig-5")
        self.assertIn(result.conclusion.value, [
            "STRONGLY_SUPPORTS", "PARTIALLY_SUPPORTS", "NEUTRAL",
            "PARTIALLY_CONTRADICTS", "STRONGLY_CONTRADICTS", "INSUFFICIENT_EVIDENCE"
        ])
        self.assertGreaterEqual(result.confidence, 0)
        self.assertLessEqual(result.confidence, 100)

    def test_research_generates_multiple_queries(self):
        r = self.client.post("/api/research", params={
            "signal_id": "sig-6", "title": "Air quality in urban areas", "description": "Pollution levels rising."
        })
        queries = r.json()["analysis"]["search_queries"]
        self.assertGreater(len(queries), 1, "Must generate multiple search queries")

    def test_research_sources_are_deduplicated(self):
        r = self.client.post("/api/research", params={
            "signal_id": "sig-7", "title": "Climate change impact", "description": "Rising temperatures."
        })
        sources = r.json()["analysis"]["sources"]
        urls = [s["url"] for s in sources]
        self.assertEqual(len(urls), len(set(urls)), "Sources must be deduplicated")

    def test_research_evidence_has_valid_types(self):
        r = self.client.get("/api/research", params={"signal_id": "sig-8"})
        evidence = r.json()["analysis"]["evidence"]
        valid_types = {"SUPPORTING", "CONTRADICTING", "CONTEXT"}
        for ev in evidence:
            self.assertIn(ev["evidence_type"], valid_types)

    def test_research_evidence_relevance_score_in_range(self):
        r = self.client.get("/api/research", params={"signal_id": "sig-9"})
        for ev in r.json()["analysis"]["evidence"]:
            self.assertGreaterEqual(ev["relevance_score"], 0.0)
            self.assertLessEqual(ev["relevance_score"], 1.0)

    def test_research_no_fabricated_sources(self):
        """Sources must have a url field and not be empty strings."""
        r = self.client.get("/api/research", params={"signal_id": "sig-10"})
        for source in r.json()["analysis"]["sources"]:
            self.assertIn("url", source)
            self.assertTrue(len(source["url"]) > 0, "Source URL must not be empty")

    def test_research_has_limitations(self):
        r = self.client.get("/api/research", params={"signal_id": "sig-11"})
        limitations = r.json()["analysis"]["limitations"]
        self.assertIsInstance(limitations, list)

    def test_research_has_version_metadata(self):
        r = self.client.get("/api/research", params={"signal_id": "sig-12"})
        payload = r.json()["analysis"]
        self.assertIn("prompt_version", payload)
        self.assertIn("model_version", payload)

    # ── Comparison Analysis ─────────────────────────────────────────────────

    def test_comparison_get_returns_valid_contract(self):
        r = self.client.get("/api/comparison", params={"signal_id": "sig-13"})
        self.assertEqual(r.status_code, 200)
        payload = r.json()["analysis"]
        result = ComparisonAnalysisResult(**payload)
        self.assertEqual(result.signal_id, "sig-13")
        self.assertIsNotNone(result.strongest_community_argument.summary)
        self.assertIsNotNone(result.strongest_research_evidence.summary)

    def test_comparison_has_agreement_or_disagreement(self):
        r = self.client.get("/api/comparison", params={"signal_id": "sig-14"})
        payload = r.json()["analysis"]
        has_content = len(payload["agreement"]) > 0 or len(payload["disagreement"]) > 0
        self.assertTrue(has_content, "Comparison must have agreement or disagreement content")

    def test_comparison_includes_research_conclusion(self):
        r = self.client.get("/api/comparison", params={"signal_id": "sig-15"})
        payload = r.json()["analysis"]
        self.assertIn("research_conclusion", payload)
        self.assertIsNotNone(payload["research_conclusion"])

    def test_comparison_missing_perspectives_are_lists(self):
        r = self.client.get("/api/comparison", params={"signal_id": "sig-16"})
        payload = r.json()["analysis"]
        self.assertIsInstance(payload["missing_community_perspectives"], list)
        self.assertIsInstance(payload["missing_research_perspectives"], list)

    # ── Security / Edge Cases ───────────────────────────────────────────────

    def test_unknown_signal_id_returns_valid_structure(self):
        """Even for unknown signals, the contract must be valid."""
        r = self.client.get("/api/community", params={"signal_id": "unknown-xyz"})
        self.assertEqual(r.status_code, 200)
        CommunityAnalysisResult(**r.json()["analysis"])

    def test_empty_signal_id_uses_default(self):
        r = self.client.get("/api/community")
        self.assertEqual(r.status_code, 200)

    def test_no_vote_stats_in_ai_response(self):
        """AI responses must not contain fabricated vote statistics."""
        for endpoint in ["/api/community", "/api/research", "/api/comparison"]:
            r = self.client.get(endpoint, params={"signal_id": "sig-vote-check"})
            payload = str(r.json())
            self.assertNotIn("upVotes", payload)
            self.assertNotIn("downVotes", payload)
            self.assertNotIn("totalVotes", payload)


if __name__ == "__main__":
    unittest.main()
