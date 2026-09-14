-- AI result persistence tables

CREATE TABLE IF NOT EXISTS community_analyses (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    signal_id       UUID NOT NULL REFERENCES signals(id) ON DELETE CASCADE,
    analysis_json   JSONB NOT NULL,
    model           VARCHAR(100),
    created_at      TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE (signal_id)
);

CREATE TABLE IF NOT EXISTS research_runs (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    signal_id    UUID NOT NULL REFERENCES signals(id) ON DELETE CASCADE,
    status       VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    model        VARCHAR(100),
    started_at   TIMESTAMP,
    completed_at TIMESTAMP,
    created_at   TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_research_runs_signal ON research_runs(signal_id);

CREATE TABLE IF NOT EXISTS research_sources (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    research_run_id UUID NOT NULL REFERENCES research_runs(id) ON DELETE CASCADE,
    url             TEXT NOT NULL,
    title           VARCHAR(500),
    publisher       VARCHAR(200),
    published_at    TIMESTAMP,
    source_type     VARCHAR(50),
    retrieved_at    TIMESTAMP NOT NULL DEFAULT now(),
    content_hash    VARCHAR(64)
);

CREATE TABLE IF NOT EXISTS evidence (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    research_run_id UUID NOT NULL REFERENCES research_runs(id) ON DELETE CASCADE,
    source_id       UUID REFERENCES research_sources(id) ON DELETE SET NULL,
    claim           TEXT NOT NULL,
    evidence_text   TEXT NOT NULL,
    evidence_type   VARCHAR(20) NOT NULL,
    relevance_score NUMERIC(4,3),
    created_at      TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS research_analyses (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    research_run_id UUID NOT NULL REFERENCES research_runs(id) ON DELETE CASCADE,
    signal_id       UUID NOT NULL REFERENCES signals(id) ON DELETE CASCADE,
    analysis_json   JSONB NOT NULL,
    model           VARCHAR(100),
    created_at      TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE (signal_id)
);

CREATE TABLE IF NOT EXISTS comparison_analyses (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    signal_id     UUID NOT NULL REFERENCES signals(id) ON DELETE CASCADE,
    analysis_json JSONB NOT NULL,
    model         VARCHAR(100),
    created_at    TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE (signal_id)
);
