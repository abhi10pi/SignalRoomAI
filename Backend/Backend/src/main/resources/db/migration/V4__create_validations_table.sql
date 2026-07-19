CREATE TABLE validations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    signal_id UUID NOT NULL REFERENCES signals(id),
    consultant_id UUID NOT NULL REFERENCES users(id),
    predicted_outcome VARCHAR(20) NOT NULL,
    confidence VARCHAR(20) NOT NULL,
    thesis TEXT NOT NULL,
    was_correct BOOLEAN,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE (signal_id, consultant_id)
);

CREATE INDEX idx_validations_signal ON validations(signal_id);
CREATE INDEX idx_validations_consultant ON validations(consultant_id);