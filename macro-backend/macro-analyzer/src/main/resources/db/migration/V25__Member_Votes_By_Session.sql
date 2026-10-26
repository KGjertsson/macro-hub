-- Votes per riksmöte, so a member's profile can chart voting over time
ALTER TABLE swedish_member_activity
    ADD COLUMN IF NOT EXISTS votes_by_session JSONB NOT NULL DEFAULT '[]';
