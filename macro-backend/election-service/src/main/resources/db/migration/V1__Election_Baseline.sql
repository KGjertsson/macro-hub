-- Squashed baseline: the three election tables as pg_dump shows them, and the Valmyndigheten rows of
-- series_configuration. series_configuration and scrape_action_queue come from macro-platform's migrations, which run
-- first. There are no sequences of its own: every table has a natural key. See "Step 5" in the migration guide.

-- One row per kommun (KF), region (RF) or Riksdag valkrets (RD) and election, parsed from Valmyndigheten's
-- mandatfördelning files. source_md5 is the file's md5, which lets the scraper skip files that haven't changed
CREATE TABLE local_election_area
(
    election_year   integer                 NOT NULL,
    election_type   character(2)            NOT NULL,
    area_code       character varying(4)    NOT NULL,
    area_name       character varying(100)  NOT NULL,
    lan_code        character varying(2)    NOT NULL,
    total_seats     integer                 NOT NULL,
    turnout         numeric(5, 2),
    eligible_voters integer,
    total_votes     integer,
    elected         jsonb     DEFAULT '[]'::jsonb NOT NULL,
    source_md5      character varying(32)   NOT NULL,
    updated         timestamp without time zone DEFAULT now(),
    CONSTRAINT local_election_area_pkey PRIMARY KEY (election_year, election_type, area_code)
);

CREATE TABLE local_election_party_result
(
    election_year integer                NOT NULL,
    election_type character(2)           NOT NULL,
    area_code     character varying(4)   NOT NULL,
    party_code    character varying(10)  NOT NULL,
    party_abbr    character varying(50)  NOT NULL,
    party_name    character varying(200) NOT NULL,
    color         character varying(7),
    votes         integer                NOT NULL,
    vote_share    numeric(5, 2)          NOT NULL,
    seats         integer DEFAULT 0      NOT NULL,
    CONSTRAINT local_election_party_result_pkey PRIMARY KEY (election_year, election_type, area_code, party_code),
    CONSTRAINT local_election_party_result_election_year_election_type_ar_fkey
        FOREIGN KEY (election_year, election_type, area_code)
            REFERENCES local_election_area (election_year, election_type, area_code) ON DELETE CASCADE
);

-- Who governs is not published as open data, so rows are curated by hand in later migrations. It starts empty
CREATE TABLE local_governance
(
    election_year     integer                 NOT NULL,
    election_type     character(2)            NOT NULL,
    area_code         character varying(4)    NOT NULL,
    coalition_name    character varying(200)  NOT NULL,
    description       text,
    governing_parties character varying(50)[] NOT NULL,
    support_parties   character varying(50)[] DEFAULT '{}'::character varying[] NOT NULL,
    CONSTRAINT local_governance_pkey PRIMARY KEY (election_year, election_type, area_code)
);

-- The existing rows' ids and global_ids, as literals, so no extension is needed
INSERT INTO series_configuration (id, global_id, name, display_name, scrape_url, category, scrape_flow)
VALUES (113, 'aca5cde3-d240-4c9e-bd8d-2ff0779ab1a0', 'MunicipalElections', 'Kommunval',
        'https://resultat.val.se/resultatfiler/val2026', 'Valmyndigheten', 'LOCAL_ELECTION'),
       (114, '336cea33-3920-4509-b93f-137fcfd9485e', 'RegionElections', 'Regionval',
        'https://resultat.val.se/resultatfiler/val2026', 'Valmyndigheten', 'LOCAL_ELECTION'),
       (115, '6da7a73a-ec46-494b-84c2-33e95ef60799', 'ParliamentElections', 'Riksdagsval',
        'https://resultat.val.se/resultatfiler/val2026', 'Valmyndigheten', 'LOCAL_ELECTION');
SELECT setval('series_configuration_id_seq', 115);
