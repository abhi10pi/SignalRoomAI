CREATE TABLE IF NOT EXISTS validations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(), signal_id UUID NOT NULL REFERENCES signals(id), consultant_id UUID NOT NULL REFERENCES users(id), predicted_outcome VARCHAR(20) NOT NULL, confidence VARCHAR(20) NOT NULL, thesis TEXT NOT NULL, was_correct BOOLEAN, created_at TIMESTAMP NOT NULL DEFAULT now(), updated_at TIMESTAMP NOT NULL DEFAULT now(), UNIQUE (signal_id, consultant_id)
);

CREATE TABLE IF NOT EXISTS credibility_scores (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(), user_id UUID NOT NULL REFERENCES users(id), domain_id UUID NOT NULL REFERENCES domains(id), total_signals INT NOT NULL DEFAULT 0, correct_signals INT NOT NULL DEFAULT 0, total_validations INT NOT NULL DEFAULT 0, correct_validations INT NOT NULL DEFAULT 0, accuracy_score NUMERIC(5,4) NOT NULL DEFAULT 0.5, overconfidence_penalty NUMERIC(5,4) NOT NULL DEFAULT 0, final_score NUMERIC(5,4) NOT NULL DEFAULT 0.5, updated_at TIMESTAMP NOT NULL DEFAULT now(), UNIQUE (user_id, domain_id)
);

CREATE TABLE IF NOT EXISTS promotion_requests (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(), user_id UUID NOT NULL REFERENCES users(id), justification TEXT NOT NULL, status VARCHAR(20) NOT NULL DEFAULT 'PENDING', reviewed_by UUID REFERENCES users(id), created_at TIMESTAMP NOT NULL DEFAULT now(), reviewed_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS outcome_evidence (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(), signal_id UUID NOT NULL REFERENCES signals(id), source_url VARCHAR(1000), snippet TEXT, ai_confidence NUMERIC(5,4), proposed_outcome VARCHAR(20), created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS resolution_votes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(), signal_id UUID NOT NULL REFERENCES signals(id), consultant_id UUID NOT NULL REFERENCES users(id), vote VARCHAR(20) NOT NULL, justification TEXT NOT NULL, created_at TIMESTAMP NOT NULL DEFAULT now(), UNIQUE (signal_id, consultant_id)
);

CREATE TABLE IF NOT EXISTS poll_votes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(), signal_id UUID NOT NULL REFERENCES signals(id), user_id UUID NOT NULL REFERENCES users(id), vote VARCHAR(20) NOT NULL, created_at TIMESTAMP NOT NULL DEFAULT now(), UNIQUE (signal_id, user_id)
);