-- Squashed from the monolith's migrations at its V36: the parliament tables as pg_dump shows them, their seed data, and
-- the Riksdagen rows of series_configuration. series_configuration and scrape_action_queue come from macro-platform's
-- migrations, which run first.
--
-- The sequence and constraint names are the monolith's, because a data-only pg_dump of its tables calls setval by these
-- names, so a dump restores into this schema unchanged.

CREATE FUNCTION update_updated_column() RETURNS trigger
    LANGUAGE plpgsql
AS
$$
BEGIN
    NEW.updated = NOW();
    RETURN NEW;
END;
$$;

CREATE SEQUENCE swedish_members_of_parliament_id_seq AS integer;
CREATE TABLE swedish_members_of_parliament
(
    id integer DEFAULT nextval('swedish_members_of_parliament_id_seq'::regclass) NOT NULL,
    global_id uuid NOT NULL,
    member_name character varying(100) NOT NULL,
    member_data jsonb NOT NULL,
    created timestamp without time zone DEFAULT now(),
    updated timestamp without time zone DEFAULT now(),
    intressent_id character varying(64) NOT NULL,
    CONSTRAINT swedish_members_of_parliament_pkey PRIMARY KEY (id),
    -- Members are upserted on Riksdagen's stable person id on every scrape
    CONSTRAINT swedish_members_of_parliament_intressent_id_key UNIQUE (intressent_id)
);
ALTER SEQUENCE swedish_members_of_parliament_id_seq OWNED BY swedish_members_of_parliament.id;
CREATE INDEX swedish_members_of_parliament_member_name ON swedish_members_of_parliament USING btree (member_name);
CREATE TRIGGER update_swedish_members_of_parliament_updated
    BEFORE UPDATE
    ON swedish_members_of_parliament
    FOR EACH ROW
EXECUTE FUNCTION update_updated_column();

CREATE TABLE swedish_member_activity
(
    intressent_id character varying(64) NOT NULL,
    votes_yes integer DEFAULT 0 NOT NULL,
    votes_no integer DEFAULT 0 NOT NULL,
    votes_absent integer DEFAULT 0 NOT NULL,
    votes_abstain integer DEFAULT 0 NOT NULL,
    speech_count integer DEFAULT 0 NOT NULL,
    motion_count integer DEFAULT 0 NOT NULL,
    recent_speeches jsonb DEFAULT '[]'::jsonb NOT NULL,
    recent_motions jsonb DEFAULT '[]'::jsonb NOT NULL,
    updated timestamp without time zone DEFAULT now(),
    -- Votes per riksmöte, so a member's profile can chart voting over time
    votes_by_session jsonb DEFAULT '[]'::jsonb NOT NULL,
    CONSTRAINT swedish_member_activity_pkey PRIMARY KEY (intressent_id)
);

CREATE TABLE riksdag_party
(
    code character varying(10) NOT NULL,
    name character varying(100) NOT NULL,
    color character varying(7) NOT NULL,
    ideology character varying(100),
    active boolean DEFAULT true NOT NULL,
    CONSTRAINT riksdag_party_pkey PRIMARY KEY (code)
);

-- /parliament/parties has no ORDER BY, so it answers in the order of the table. SD is last because the monolith's V24
-- updated its row (SD's brand yellow, as used on sd.se), which moved it. Seeded in that order, so an empty database
-- answers like a restored one.
INSERT INTO riksdag_party (code, name, color, ideology, active)
VALUES ('S', 'Socialdemokraterna', '#E8112d', 'Social Democracy', TRUE),
       ('M', 'Moderaterna', '#52BDEC', 'Liberal Conservatism', TRUE),
       ('C', 'Centerpartiet', '#009933', 'Agrarian Liberalism', TRUE),
       ('V', 'Vänsterpartiet', '#DA291C', 'Democratic Socialism', TRUE),
       ('KD', 'Kristdemokraterna', '#000077', 'Christian Democracy', TRUE),
       ('L', 'Liberalerna', '#006AB3', 'Social Liberalism', TRUE),
       ('MP', 'Miljöpartiet', '#83CF39', 'Green Politics', TRUE),
       ('FP', 'Folkpartiet liberalerna', '#006AB3', 'Social Liberalism', FALSE),
       ('NYD', 'Ny Demokrati', '#888888', 'Right-wing Populism', FALSE),
       ('-', 'Politisk vilde', '#9CA3AF', NULL, FALSE),
       ('SD', 'Sverigedemokraterna', '#FFCC00', 'National Conservatism', TRUE);

CREATE TABLE riksdag_election
(
    year integer NOT NULL,
    term_start date NOT NULL,
    term_end date NOT NULL,
    CONSTRAINT riksdag_election_pkey PRIMARY KEY (year)
);

-- Term boundaries match the start dates of the chamber assignments in Riksdagen's open data
INSERT INTO riksdag_election (year, term_start, term_end)
VALUES (1988, '1988-10-03', '1991-09-29'),
       (1991, '1991-09-30', '1994-10-02'),
       (1994, '1994-10-03', '1998-10-04'),
       (1998, '1998-10-05', '2002-09-29'),
       (2002, '2002-09-30', '2006-10-01'),
       (2006, '2006-10-02', '2010-10-03'),
       (2010, '2010-10-04', '2014-09-28'),
       (2014, '2014-09-29', '2018-09-23'),
       (2018, '2018-09-24', '2022-09-25'),
       (2022, '2022-09-26', '2026-09-27'),
       (2026, '2026-09-28', '2030-09-23');

CREATE TABLE riksdag_election_result
(
    year integer NOT NULL,
    party_code character varying(10) NOT NULL,
    seats integer NOT NULL,
    CONSTRAINT riksdag_election_result_pkey PRIMARY KEY (year, party_code),
    CONSTRAINT riksdag_election_result_party_code_fkey FOREIGN KEY (party_code) REFERENCES riksdag_party (code),
    CONSTRAINT riksdag_election_result_year_fkey FOREIGN KEY (year) REFERENCES riksdag_election (year)
);

-- Official results from Valmyndigheten (2026 confirmed 2026-09-19)
INSERT INTO riksdag_election_result (year, party_code, seats)
VALUES (2026, 'S', 99), (2026, 'M', 70), (2026, 'SD', 62), (2026, 'V', 30),
       (2026, 'C', 25), (2026, 'KD', 22), (2026, 'MP', 22), (2026, 'L', 19),
       (2022, 'S', 107), (2022, 'SD', 73), (2022, 'M', 68), (2022, 'C', 24),
       (2022, 'V', 24), (2022, 'KD', 19), (2022, 'MP', 18), (2022, 'L', 16),
       (2018, 'S', 100), (2018, 'M', 70), (2018, 'SD', 62), (2018, 'C', 31),
       (2018, 'V', 28), (2018, 'KD', 22), (2018, 'L', 20), (2018, 'MP', 16),
       (2014, 'S', 113), (2014, 'M', 84), (2014, 'SD', 49), (2014, 'MP', 25),
       (2014, 'C', 22), (2014, 'V', 21), (2014, 'L', 19), (2014, 'KD', 16),
       (2010, 'S', 112), (2010, 'M', 107), (2010, 'MP', 25), (2010, 'L', 24),
       (2010, 'C', 23), (2010, 'SD', 20), (2010, 'V', 19), (2010, 'KD', 19),
       (2006, 'S', 130), (2006, 'M', 97), (2006, 'C', 29), (2006, 'L', 28),
       (2006, 'KD', 24), (2006, 'V', 22), (2006, 'MP', 19),
       (2002, 'S', 144), (2002, 'M', 55), (2002, 'L', 48), (2002, 'KD', 33),
       (2002, 'V', 30), (2002, 'C', 22), (2002, 'MP', 17),
       (1998, 'S', 131), (1998, 'M', 82), (1998, 'V', 43), (1998, 'KD', 42),
       (1998, 'C', 18), (1998, 'L', 17), (1998, 'MP', 16),
       (1994, 'S', 161), (1994, 'M', 80), (1994, 'C', 27), (1994, 'L', 26),
       (1994, 'V', 22), (1994, 'MP', 18), (1994, 'KD', 15),
       (1991, 'S', 138), (1991, 'M', 80), (1991, 'L', 33), (1991, 'C', 31),
       (1991, 'KD', 26), (1991, 'NYD', 25), (1991, 'V', 16),
       (1988, 'S', 156), (1988, 'M', 66), (1988, 'L', 44), (1988, 'C', 42),
       (1988, 'V', 21), (1988, 'MP', 20);

CREATE TABLE riksdag_organ
(
    code character varying(20) NOT NULL,
    name character varying(150) NOT NULL,
    CONSTRAINT riksdag_organ_pkey PRIMARY KEY (code)
);

INSERT INTO riksdag_organ (code, name)
VALUES ('kam', 'Kammaren'),
       ('AU', 'Arbetsmarknadsutskottet'),
       ('CU', 'Civilutskottet'),
       ('FiU', 'Finansutskottet'),
       ('FöU', 'Försvarsutskottet'),
       ('JuU', 'Justitieutskottet'),
       ('KU', 'Konstitutionsutskottet'),
       ('KrU', 'Kulturutskottet'),
       ('MJU', 'Miljö- och jordbruksutskottet'),
       ('NU', 'Näringsutskottet'),
       ('SkU', 'Skatteutskottet'),
       ('SfU', 'Socialförsäkringsutskottet'),
       ('SoU', 'Socialutskottet'),
       ('TU', 'Trafikutskottet'),
       ('UbU', 'Utbildningsutskottet'),
       ('UU', 'Utrikesutskottet'),
       ('UFöU', 'Sammansatta utrikes- och försvarsutskottet'),
       ('CKrU', 'Sammansatta civil- och kulturutskottet'),
       ('EUN', 'EU-nämnden'),
       ('EP', 'Europaparlamentet'),
       ('KD', 'Krigsdelegationen'),
       ('NR', 'Nordiska rådets svenska delegation'),
       ('VB', 'Valberedningen'),
       ('RS', 'Riksdagsstyrelsen'),
       ('UN', 'Utrikesnämnden'),
       ('RB', 'Riksbanksfullmäktige'),
       ('OSSE', 'Delegationen till OSSE:s parlamentariska församling'),
       ('NATO', 'Delegationen till Natos parlamentariska församling'),
       ('IPU', 'Delegationen till Interparlamentariska unionen'),
       ('SB', 'Statsrådsberedningen'),
       ('UD', 'Utrikesdepartementet'),
       ('Fi', 'Finansdepartementet'),
       ('S', 'Socialdepartementet'),
       ('A', 'Arbetsmarknadsdepartementet'),
       ('U', 'Utbildningsdepartementet'),
       ('Ju', 'Justitiedepartementet'),
       ('N', 'Näringsdepartementet'),
       ('I', 'Infrastrukturdepartementet'),
       ('Fö', 'Försvarsdepartementet'),
       ('Ku', 'Kulturdepartementet'),
       ('LI', 'Landsbygds- och infrastrukturdepartementet'),
       ('KN', 'Klimat- och näringslivsdepartementet'),
       ('M', 'Miljödepartementet');

-- The monolith's rows, with its ids and global_ids
INSERT INTO series_configuration (id, global_id, name, display_name, scrape_url, category, scrape_flow)
VALUES (111, '2846f8b4-219c-49b9-9577-f180110b8c6b', 'MembersOfParliament', 'Riksdagsledamöter',
        'https://data.riksdagen.se/personlista/?iid=&fnamn=&enamn=&f_ar=&kn=&parti=&valkrets=&rdlstatus=samtliga&org=&utformat=json&sort=sorteringsnamn&sortorder=asc&termlista=',
        'Riksdagen', 'MEMBER_OF_PARLIAMENT'),
       (112, 'fbec65ad-0526-4c42-b2e9-8ee664741e71', 'MembersOfParliamentActivity', 'Riksdagsledamöters aktivitet',
        'https://data.riksdagen.se', 'Riksdagen', 'MEMBER_ACTIVITY');
SELECT setval('series_configuration_id_seq', 112);
