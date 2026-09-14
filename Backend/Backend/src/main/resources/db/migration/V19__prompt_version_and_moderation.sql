-- Phase 6: add prompt_version metadata to AI result tables
ALTER TABLE community_analyses  ADD COLUMN IF NOT EXISTS prompt_version VARCHAR(50);
ALTER TABLE research_analyses   ADD COLUMN IF NOT EXISTS prompt_version VARCHAR(50);
ALTER TABLE comparison_analyses ADD COLUMN IF NOT EXISTS prompt_version VARCHAR(50);

-- Phase 10: moderation — content reports
CREATE TABLE IF NOT EXISTS reports (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    reporter_id  UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    signal_id    UUID REFERENCES signals(id) ON DELETE CASCADE,
    opinion_id   UUID REFERENCES opinions(id) ON DELETE CASCADE,
    comment_id   UUID REFERENCES comments(id) ON DELETE CASCADE,
    reason       VARCHAR(100) NOT NULL,
    detail       TEXT,
    status       VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    reviewed_by  UUID REFERENCES users(id) ON DELETE SET NULL,
    reviewed_at  TIMESTAMP,
    created_at   TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT reports_target_check CHECK (
        (signal_id IS NOT NULL)::int +
        (opinion_id IS NOT NULL)::int +
        (comment_id IS NOT NULL)::int = 1
    )
);

CREATE INDEX IF NOT EXISTS idx_reports_status ON reports(status);
CREATE INDEX IF NOT EXISTS idx_reports_signal  ON reports(signal_id);

-- Phase 10: hidden content flag on opinions and comments
ALTER TABLE opinions ADD COLUMN IF NOT EXISTS hidden BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE comments ADD COLUMN IF NOT EXISTS hidden BOOLEAN NOT NULL DEFAULT false;
