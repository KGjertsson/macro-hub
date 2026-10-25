CREATE TABLE IF NOT EXISTS riksdag_party
(
    code     VARCHAR(10) PRIMARY KEY,
    name     VARCHAR(100) NOT NULL,
    color    VARCHAR(7)   NOT NULL,
    ideology VARCHAR(100),
    active   BOOLEAN      NOT NULL DEFAULT TRUE
);

INSERT INTO riksdag_party (code, name, color, ideology, active)
VALUES ('S', 'Socialdemokraterna', '#E8112d', 'Social Democracy', TRUE),
       ('M', 'Moderaterna', '#52BDEC', 'Liberal Conservatism', TRUE),
       ('SD', 'Sverigedemokraterna', '#DDDD00', 'National Conservatism', TRUE),
       ('C', 'Centerpartiet', '#009933', 'Agrarian Liberalism', TRUE),
       ('V', 'Vänsterpartiet', '#DA291C', 'Democratic Socialism', TRUE),
       ('KD', 'Kristdemokraterna', '#000077', 'Christian Democracy', TRUE),
       ('L', 'Liberalerna', '#006AB3', 'Social Liberalism', TRUE),
       ('MP', 'Miljöpartiet', '#83CF39', 'Green Politics', TRUE),
       ('FP', 'Folkpartiet liberalerna', '#006AB3', 'Social Liberalism', FALSE),
       ('NYD', 'Ny Demokrati', '#888888', 'Right-wing Populism', FALSE),
       ('-', 'Politisk vilde', '#9CA3AF', NULL, FALSE);

CREATE TABLE IF NOT EXISTS riksdag_election
(
    year       INTEGER PRIMARY KEY,
    term_start DATE NOT NULL,
    term_end   DATE NOT NULL
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

CREATE TABLE IF NOT EXISTS riksdag_election_result
(
    year       INTEGER     NOT NULL REFERENCES riksdag_election (year),
    party_code VARCHAR(10) NOT NULL REFERENCES riksdag_party (code),
    seats      INTEGER     NOT NULL,
    PRIMARY KEY (year, party_code)
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

CREATE TABLE IF NOT EXISTS riksdag_organ
(
    code VARCHAR(20) PRIMARY KEY,
    name VARCHAR(150) NOT NULL
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
