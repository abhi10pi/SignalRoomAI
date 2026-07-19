CREATE TABLE resolution_votes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    signal_id UUID NOT NULL REFERENCES signals(id),
    consultant_id UUID NOT NULL REFERENCES users(id),
    vote VARCHAR(20) NOT NULL,
    justification TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE (signal_id, consultant_id)
);

CREATE INDEX idx_resolution_votes_signal ON resolution_votes(signal_id);