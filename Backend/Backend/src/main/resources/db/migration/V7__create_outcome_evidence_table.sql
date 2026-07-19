CREATE TABLE outcome_evidence (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    signal_id UUID NOT NULL REFERENCES signals(id),
    source_url VARCHAR(1000),
    snippet TEXT,
    ai_confidence NUMERIC(5,4),
    proposed_outcome VARCHAR(20),
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_evidence_signal ON outcome_evidence(signal_id);