CREATE TABLE signals (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    submitter_id UUID NOT NULL REFERENCES users(id),
    domain_id UUID NOT NULL REFERENCES domains(id),
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    resolution_type VARCHAR(30) NOT NULL,
    resolution_criteria JSONB NOT NULL,
    resolution_date TIMESTAMP NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    contested BOOLEAN NOT NULL DEFAULT FALSE,
    actual_outcome VARCHAR(20),
    visibility VARCHAR(20) NOT NULL DEFAULT 'PUBLIC',
    submitted_at TIMESTAMP,
    evaluated_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_signals_status ON signals(status);
CREATE INDEX idx_signals_domain ON signals(domain_id);
CREATE INDEX idx_signals_submitter ON signals(submitter_id);