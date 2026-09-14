ALTER TABLE validations
    ADD COLUMN IF NOT EXISTS resolved_outcome VARCHAR(20);
