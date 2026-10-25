INSERT INTO series_configuration (
    global_id,
    name,
    display_name,
    scrape_url,
    category
)
VALUES
    (uuid_generate_v4(), 'MembersOfParliamentActivity', 'Riksdagsledamöters aktivitet', 'https://data.riksdagen.se', 'Riksdagen')
;

CREATE TABLE IF NOT EXISTS swedish_member_activity
(
    intressent_id   VARCHAR(64) PRIMARY KEY,
    votes_yes       INTEGER NOT NULL DEFAULT 0,
    votes_no        INTEGER NOT NULL DEFAULT 0,
    votes_absent    INTEGER NOT NULL DEFAULT 0,
    votes_abstain   INTEGER NOT NULL DEFAULT 0,
    speech_count    INTEGER NOT NULL DEFAULT 0,
    motion_count    INTEGER NOT NULL DEFAULT 0,
    recent_speeches JSONB   NOT NULL DEFAULT '[]',
    recent_motions  JSONB   NOT NULL DEFAULT '[]',
    updated         TIMESTAMP DEFAULT NOW()
);
