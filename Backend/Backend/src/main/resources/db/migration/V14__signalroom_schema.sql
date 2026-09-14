-- Drop old tables that are no longer needed
DROP TABLE IF EXISTS resolution_votes CASCADE;
DROP TABLE IF EXISTS poll_votes CASCADE;
DROP TABLE IF EXISTS outcome_evidence CASCADE;
DROP TABLE IF EXISTS promotion_requests CASCADE;
DROP TABLE IF EXISTS credibility_scores CASCADE;
DROP TABLE IF EXISTS validations CASCADE;
DROP TABLE IF EXISTS audit_logs CASCADE;

-- Drop old signals table and recreate for Signalroom
DROP TABLE IF EXISTS signals CASCADE;

-- Tags
CREATE TABLE IF NOT EXISTS tags (
    id   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(50) NOT NULL UNIQUE
);

-- Signals (Signalroom model)
CREATE TABLE signals (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    author_id        UUID NOT NULL REFERENCES users(id),
    title            VARCHAR(300) NOT NULL,
    description      TEXT NOT NULL,
    category         VARCHAR(50) NOT NULL,
    status           VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    discussion_start TIMESTAMP NOT NULL DEFAULT now(),
    discussion_end   TIMESTAMP NOT NULL,
    created_at       TIMESTAMP NOT NULL DEFAULT now(),
    updated_at       TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_signals_status     ON signals(status);
CREATE INDEX idx_signals_author     ON signals(author_id);
CREATE INDEX idx_signals_disc_end   ON signals(discussion_end);

-- Signal ↔ Tag join
CREATE TABLE signal_tags (
    signal_id UUID NOT NULL REFERENCES signals(id) ON DELETE CASCADE,
    tag_id    UUID NOT NULL REFERENCES tags(id)    ON DELETE CASCADE,
    PRIMARY KEY (signal_id, tag_id)
);

-- Optional sources attached to a signal
CREATE TABLE signal_sources (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    signal_id   UUID NOT NULL REFERENCES signals(id) ON DELETE CASCADE,
    url         TEXT NOT NULL,
    title       VARCHAR(300),
    description TEXT,
    created_at  TIMESTAMP NOT NULL DEFAULT now()
);

-- Signal votes (UP / DOWN, one per user per signal)
CREATE TABLE signal_votes (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    signal_id  UUID NOT NULL REFERENCES signals(id) ON DELETE CASCADE,
    user_id    UUID NOT NULL REFERENCES users(id),
    vote_type  VARCHAR(10) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE (signal_id, user_id)
);

-- Opinions
CREATE TABLE opinions (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    signal_id  UUID NOT NULL REFERENCES signals(id) ON DELETE CASCADE,
    user_id    UUID NOT NULL REFERENCES users(id),
    position   VARCHAR(10) NOT NULL,
    content    TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_opinions_signal ON opinions(signal_id);

-- Opinion sources
CREATE TABLE opinion_sources (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    opinion_id UUID NOT NULL REFERENCES opinions(id) ON DELETE CASCADE,
    url        TEXT NOT NULL,
    title      VARCHAR(300),
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

-- Opinion votes
CREATE TABLE opinion_votes (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    opinion_id UUID NOT NULL REFERENCES opinions(id) ON DELETE CASCADE,
    user_id    UUID NOT NULL REFERENCES users(id),
    vote_type  VARCHAR(10) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE (opinion_id, user_id)
);

-- Comments (supports signal-level and opinion-level, with replies)
CREATE TABLE comments (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    signal_id         UUID NOT NULL REFERENCES signals(id) ON DELETE CASCADE,
    opinion_id        UUID REFERENCES opinions(id) ON DELETE SET NULL,
    user_id           UUID NOT NULL REFERENCES users(id),
    parent_comment_id UUID REFERENCES comments(id) ON DELETE CASCADE,
    content           TEXT NOT NULL,
    created_at        TIMESTAMP NOT NULL DEFAULT now(),
    updated_at        TIMESTAMP NOT NULL DEFAULT now(),
    deleted_at        TIMESTAMP
);

CREATE INDEX idx_comments_signal  ON comments(signal_id);
CREATE INDEX idx_comments_opinion ON comments(opinion_id);
CREATE INDEX idx_comments_parent  ON comments(parent_comment_id);
