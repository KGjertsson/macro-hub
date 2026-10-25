INSERT INTO series_configuration (
    global_id,
    name,
    display_name,
    scrape_url,
    category
)
VALUES
    (uuid_generate_v4(), 'MunicipalElections', 'Kommunval', 'https://resultat.val.se/resultatfiler/val2026', 'Valmyndigheten'),
    (uuid_generate_v4(), 'RegionElections', 'Regionval', 'https://resultat.val.se/resultatfiler/val2026', 'Valmyndigheten')
;

-- One row per kommun (KF) or region (RF) and election, parsed from Valmyndigheten's mandatfördelning files
CREATE TABLE IF NOT EXISTS local_election_area
(
    election_year   INTEGER      NOT NULL,
    election_type   CHAR(2)      NOT NULL,
    area_code       VARCHAR(4)   NOT NULL,
    area_name       VARCHAR(100) NOT NULL,
    lan_code        VARCHAR(2)   NOT NULL,
    total_seats     INTEGER      NOT NULL,
    turnout         NUMERIC(5, 2),
    eligible_voters INTEGER,
    total_votes     INTEGER,
    elected         JSONB        NOT NULL DEFAULT '[]',
    source_md5      VARCHAR(32)  NOT NULL,
    updated         TIMESTAMP DEFAULT NOW(),
    PRIMARY KEY (election_year, election_type, area_code)
);

CREATE TABLE IF NOT EXISTS local_election_party_result
(
    election_year INTEGER      NOT NULL,
    election_type CHAR(2)      NOT NULL,
    area_code     VARCHAR(4)   NOT NULL,
    party_code    VARCHAR(10)  NOT NULL,
    party_abbr    VARCHAR(50)  NOT NULL,
    party_name    VARCHAR(200) NOT NULL,
    color         VARCHAR(7),
    votes         INTEGER      NOT NULL,
    vote_share    NUMERIC(5, 2) NOT NULL,
    seats         INTEGER      NOT NULL DEFAULT 0,
    PRIMARY KEY (election_year, election_type, area_code, party_code),
    FOREIGN KEY (election_year, election_type, area_code)
        REFERENCES local_election_area (election_year, election_type, area_code) ON DELETE CASCADE
);

-- Who governs is not published as open data, so rows are curated by hand in later migrations
CREATE TABLE IF NOT EXISTS local_governance
(
    election_year     INTEGER      NOT NULL,
    election_type     CHAR(2)      NOT NULL,
    area_code         VARCHAR(4)   NOT NULL,
    coalition_name    VARCHAR(200) NOT NULL,
    description       TEXT,
    governing_parties VARCHAR(50)[] NOT NULL,
    support_parties   VARCHAR(50)[] NOT NULL DEFAULT '{}',
    PRIMARY KEY (election_year, election_type, area_code)
);
