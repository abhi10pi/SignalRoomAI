CREATE TABLE poll_votes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    signal_id UUID NOT NULL REFERENCES signals(id),
    user_id UUID NOT NULL REFERENCES users(id),
    vote VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE (signal_id, user_id)
);

CREATE INDEX idx_poll_votes_signal ON poll_votes(signal_id);
CREATE INDEX idx_poll_votes_user ON poll_votes(user_id);
