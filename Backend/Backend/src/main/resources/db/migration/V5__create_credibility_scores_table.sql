CREATE TABLE credibility_scores (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id),
    domain_id UUID NOT NULL REFERENCES domains(id),
    total_signals INT NOT NULL DEFAULT 0,
    correct_signals INT NOT NULL DEFAULT 0,
    total_validations INT NOT NULL DEFAULT 0,
    correct_validations INT NOT NULL DEFAULT 0,
    accuracy_score NUMERIC(5,4) NOT NULL DEFAULT 0.5,
    overconfidence_penalty NUMERIC(5,4) NOT NULL DEFAULT 0,
    final_score NUMERIC(5,4) NOT NULL DEFAULT 0.5,
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE (user_id, domain_id)
);

CREATE INDEX idx_credibility_user ON credibility_scores(user_id);
CREATE INDEX idx_credibility_domain ON credibility_scores(domain_id);