-- Squashed from the monolith's migrations at its V34: the macro-series tables as pg_dump shows them, and the MACRO_POINT
-- rows of series_configuration. series_configuration and scrape_action_queue come from macro-platform's migrations,
-- which run first.
--
-- Some sequence and constraint names are older than their tables (int_gov_bond_10_year_denmark uses
-- euro_market_10_year_denmark_id_seq, for example). They stay as they are, because a data-only pg_dump of the monolith's
-- tables calls setval by these names, so a dump restores into this schema unchanged.

CREATE SEQUENCE euro_market_3_month_denmark_id_seq AS integer;
CREATE TABLE euro_market_3_month_denmark
(
    id integer DEFAULT nextval('euro_market_3_month_denmark_id_seq'::regclass) NOT NULL,
    global_id uuid NOT NULL,
    value double precision NOT NULL,
    date date NOT NULL,
    created timestamp without time zone DEFAULT now(),
    updated timestamp without time zone DEFAULT now(),
    CONSTRAINT euro_market_3_month_denmark_pkey PRIMARY KEY (id)
);
ALTER SEQUENCE euro_market_3_month_denmark_id_seq OWNED BY euro_market_3_month_denmark.id;

CREATE SEQUENCE euro_market_3_month_eur_id_seq AS integer;
CREATE TABLE euro_market_3_month_eur
(
    id integer DEFAULT nextval('euro_market_3_month_eur_id_seq'::regclass) NOT NULL,
    global_id uuid NOT NULL,
    value double precision NOT NULL,
    date date NOT NULL,
    created timestamp without time zone DEFAULT now(),
    updated timestamp without time zone DEFAULT now(),
    CONSTRAINT euro_market_3_month_eur_pkey PRIMARY KEY (id)
);
ALTER SEQUENCE euro_market_3_month_eur_id_seq OWNED BY euro_market_3_month_eur.id;

CREATE SEQUENCE euro_market_3_month_gb_id_seq AS integer;
CREATE TABLE euro_market_3_month_gb
(
    id integer DEFAULT nextval('euro_market_3_month_gb_id_seq'::regclass) NOT NULL,
    global_id uuid NOT NULL,
    value double precision NOT NULL,
    date date NOT NULL,
    created timestamp without time zone DEFAULT now(),
    updated timestamp without time zone DEFAULT now(),
    CONSTRAINT euro_market_3_month_gb_pkey PRIMARY KEY (id)
);
ALTER SEQUENCE euro_market_3_month_gb_id_seq OWNED BY euro_market_3_month_gb.id;

CREATE SEQUENCE euro_market_3_month_japan_id_seq AS integer;
CREATE TABLE euro_market_3_month_japan
(
    id integer DEFAULT nextval('euro_market_3_month_japan_id_seq'::regclass) NOT NULL,
    global_id uuid NOT NULL,
    value double precision NOT NULL,
    date date NOT NULL,
    created timestamp without time zone DEFAULT now(),
    updated timestamp without time zone DEFAULT now(),
    CONSTRAINT euro_market_3_month_japan_pkey PRIMARY KEY (id)
);
ALTER SEQUENCE euro_market_3_month_japan_id_seq OWNED BY euro_market_3_month_japan.id;

CREATE SEQUENCE euro_market_3_month_norway_id_seq AS integer;
CREATE TABLE euro_market_3_month_norway
(
    id integer DEFAULT nextval('euro_market_3_month_norway_id_seq'::regclass) NOT NULL,
    global_id uuid NOT NULL,
    value double precision NOT NULL,
    date date NOT NULL,
    created timestamp without time zone DEFAULT now(),
    updated timestamp without time zone DEFAULT now(),
    CONSTRAINT euro_market_3_month_norway_pkey PRIMARY KEY (id)
);
ALTER SEQUENCE euro_market_3_month_norway_id_seq OWNED BY euro_market_3_month_norway.id;

CREATE SEQUENCE euro_market_3_month_usa_id_seq AS integer;
CREATE TABLE euro_market_3_month_usa
(
    id integer DEFAULT nextval('euro_market_3_month_usa_id_seq'::regclass) NOT NULL,
    global_id uuid NOT NULL,
    value double precision NOT NULL,
    date date NOT NULL,
    created timestamp without time zone DEFAULT now(),
    updated timestamp without time zone DEFAULT now(),
    CONSTRAINT euro_market_3_month_usa_pkey PRIMARY KEY (id)
);
ALTER SEQUENCE euro_market_3_month_usa_id_seq OWNED BY euro_market_3_month_usa.id;

CREATE SEQUENCE euro_market_6_month_denmark_id_seq AS integer;
CREATE TABLE euro_market_6_month_denmark
(
    id integer DEFAULT nextval('euro_market_6_month_denmark_id_seq'::regclass) NOT NULL,
    global_id uuid NOT NULL,
    value double precision NOT NULL,
    date date NOT NULL,
    created timestamp without time zone DEFAULT now(),
    updated timestamp without time zone DEFAULT now(),
    CONSTRAINT euro_market_6_month_denmark_pkey PRIMARY KEY (id)
);
ALTER SEQUENCE euro_market_6_month_denmark_id_seq OWNED BY euro_market_6_month_denmark.id;

CREATE SEQUENCE euro_market_6_month_eur_id_seq AS integer;
CREATE TABLE euro_market_6_month_eur
(
    id integer DEFAULT nextval('euro_market_6_month_eur_id_seq'::regclass) NOT NULL,
    global_id uuid NOT NULL,
    value double precision NOT NULL,
    date date NOT NULL,
    created timestamp without time zone DEFAULT now(),
    updated timestamp without time zone DEFAULT now(),
    CONSTRAINT euro_market_6_month_eur_pkey PRIMARY KEY (id)
);
ALTER SEQUENCE euro_market_6_month_eur_id_seq OWNED BY euro_market_6_month_eur.id;

CREATE SEQUENCE euro_market_6_month_gb_id_seq AS integer;
CREATE TABLE euro_market_6_month_gb
(
    id integer DEFAULT nextval('euro_market_6_month_gb_id_seq'::regclass) NOT NULL,
    global_id uuid NOT NULL,
    value double precision NOT NULL,
    date date NOT NULL,
    created timestamp without time zone DEFAULT now(),
    updated timestamp without time zone DEFAULT now(),
    CONSTRAINT euro_market_6_month_gb_pkey PRIMARY KEY (id)
);
ALTER SEQUENCE euro_market_6_month_gb_id_seq OWNED BY euro_market_6_month_gb.id;

CREATE SEQUENCE euro_market_6_month_japan_id_seq AS integer;
CREATE TABLE euro_market_6_month_japan
(
    id integer DEFAULT nextval('euro_market_6_month_japan_id_seq'::regclass) NOT NULL,
    global_id uuid NOT NULL,
    value double precision NOT NULL,
    date date NOT NULL,
    created timestamp without time zone DEFAULT now(),
    updated timestamp without time zone DEFAULT now(),
    CONSTRAINT euro_market_6_month_japan_pkey PRIMARY KEY (id)
);
ALTER SEQUENCE euro_market_6_month_japan_id_seq OWNED BY euro_market_6_month_japan.id;

CREATE SEQUENCE euro_market_6_month_norway_id_seq AS integer;
CREATE TABLE euro_market_6_month_norway
(
    id integer DEFAULT nextval('euro_market_6_month_norway_id_seq'::regclass) NOT NULL,
    global_id uuid NOT NULL,
    value double precision NOT NULL,
    date date NOT NULL,
    created timestamp without time zone DEFAULT now(),
    updated timestamp without time zone DEFAULT now(),
    CONSTRAINT euro_market_6_month_norway_pkey PRIMARY KEY (id)
);
ALTER SEQUENCE euro_market_6_month_norway_id_seq OWNED BY euro_market_6_month_norway.id;

CREATE SEQUENCE euro_market_6_month_usa_id_seq AS integer;
CREATE TABLE euro_market_6_month_usa
(
    id integer DEFAULT nextval('euro_market_6_month_usa_id_seq'::regclass) NOT NULL,
    global_id uuid NOT NULL,
    value double precision NOT NULL,
    date date NOT NULL,
    created timestamp without time zone DEFAULT now(),
    updated timestamp without time zone DEFAULT now(),
    CONSTRAINT euro_market_6_month_usa_pkey PRIMARY KEY (id)
);
ALTER SEQUENCE euro_market_6_month_usa_id_seq OWNED BY euro_market_6_month_usa.id;

CREATE SEQUENCE eurostat_national_debt_series_id_seq AS integer;
CREATE TABLE eurostat_national_debt_series
(
    id integer DEFAULT nextval('eurostat_national_debt_series_id_seq'::regclass) NOT NULL,
    global_id uuid NOT NULL,
    series_name character varying(100) NOT NULL,
    value double precision NOT NULL,
    date date NOT NULL,
    created timestamp without time zone DEFAULT now(),
    updated timestamp without time zone DEFAULT now(),
    CONSTRAINT eurostat_national_debt_series_pkey PRIMARY KEY (id)
);
ALTER SEQUENCE eurostat_national_debt_series_id_seq OWNED BY eurostat_national_debt_series.id;

CREATE SEQUENCE exchange_usd_sek_id_seq AS integer;
CREATE TABLE exchange_usd_sek
(
    id integer DEFAULT nextval('exchange_usd_sek_id_seq'::regclass) NOT NULL,
    global_id uuid NOT NULL,
    usd_sek double precision NOT NULL,
    usd_sek_date date NOT NULL,
    created timestamp without time zone DEFAULT now(),
    updated timestamp without time zone DEFAULT now(),
    CONSTRAINT exchange_usd_sek_pkey PRIMARY KEY (id)
);
ALTER SEQUENCE exchange_usd_sek_id_seq OWNED BY exchange_usd_sek.id;

CREATE SEQUENCE fed_series_id_seq AS integer;
CREATE TABLE fed_series
(
    id integer DEFAULT nextval('fed_series_id_seq'::regclass) NOT NULL,
    global_id uuid NOT NULL,
    series_name character varying(100) NOT NULL,
    value double precision NOT NULL,
    date date NOT NULL,
    created timestamp without time zone DEFAULT now(),
    updated timestamp without time zone DEFAULT now(),
    CONSTRAINT fed_series_pkey PRIMARY KEY (id)
);
ALTER SEQUENCE fed_series_id_seq OWNED BY fed_series.id;

CREATE SEQUENCE euro_market_10_year_denmark_id_seq AS integer;
CREATE TABLE int_gov_bond_10_year_denmark
(
    id integer DEFAULT nextval('euro_market_10_year_denmark_id_seq'::regclass) NOT NULL,
    global_id uuid NOT NULL,
    value double precision NOT NULL,
    date date NOT NULL,
    created timestamp without time zone DEFAULT now(),
    updated timestamp without time zone DEFAULT now(),
    CONSTRAINT euro_market_10_year_denmark_pkey PRIMARY KEY (id)
);
ALTER SEQUENCE euro_market_10_year_denmark_id_seq OWNED BY int_gov_bond_10_year_denmark.id;

CREATE SEQUENCE euro_market_10_year_eur_id_seq AS integer;
CREATE TABLE int_gov_bond_10_year_eur
(
    id integer DEFAULT nextval('euro_market_10_year_eur_id_seq'::regclass) NOT NULL,
    global_id uuid NOT NULL,
    value double precision NOT NULL,
    date date NOT NULL,
    created timestamp without time zone DEFAULT now(),
    updated timestamp without time zone DEFAULT now(),
    CONSTRAINT euro_market_10_year_eur_pkey PRIMARY KEY (id)
);
ALTER SEQUENCE euro_market_10_year_eur_id_seq OWNED BY int_gov_bond_10_year_eur.id;

CREATE SEQUENCE euro_market_10_year_finland_id_seq AS integer;
CREATE TABLE int_gov_bond_10_year_finland
(
    id integer DEFAULT nextval('euro_market_10_year_finland_id_seq'::regclass) NOT NULL,
    global_id uuid NOT NULL,
    value double precision NOT NULL,
    date date NOT NULL,
    created timestamp without time zone DEFAULT now(),
    updated timestamp without time zone DEFAULT now(),
    CONSTRAINT euro_market_10_year_finland_pkey PRIMARY KEY (id)
);
ALTER SEQUENCE euro_market_10_year_finland_id_seq OWNED BY int_gov_bond_10_year_finland.id;

CREATE SEQUENCE euro_market_10_year_france_id_seq AS integer;
CREATE TABLE int_gov_bond_10_year_france
(
    id integer DEFAULT nextval('euro_market_10_year_france_id_seq'::regclass) NOT NULL,
    global_id uuid NOT NULL,
    value double precision NOT NULL,
    date date NOT NULL,
    created timestamp without time zone DEFAULT now(),
    updated timestamp without time zone DEFAULT now(),
    CONSTRAINT euro_market_10_year_france_pkey PRIMARY KEY (id)
);
ALTER SEQUENCE euro_market_10_year_france_id_seq OWNED BY int_gov_bond_10_year_france.id;

CREATE SEQUENCE euro_market_10_year_gb_id_seq AS integer;
CREATE TABLE int_gov_bond_10_year_gb
(
    id integer DEFAULT nextval('euro_market_10_year_gb_id_seq'::regclass) NOT NULL,
    global_id uuid NOT NULL,
    value double precision NOT NULL,
    date date NOT NULL,
    created timestamp without time zone DEFAULT now(),
    updated timestamp without time zone DEFAULT now(),
    CONSTRAINT euro_market_10_year_gb_pkey PRIMARY KEY (id)
);
ALTER SEQUENCE euro_market_10_year_gb_id_seq OWNED BY int_gov_bond_10_year_gb.id;

CREATE SEQUENCE euro_market_10_year_germany_id_seq AS integer;
CREATE TABLE int_gov_bond_10_year_germany
(
    id integer DEFAULT nextval('euro_market_10_year_germany_id_seq'::regclass) NOT NULL,
    global_id uuid NOT NULL,
    value double precision NOT NULL,
    date date NOT NULL,
    created timestamp without time zone DEFAULT now(),
    updated timestamp without time zone DEFAULT now(),
    CONSTRAINT euro_market_10_year_germany_pkey PRIMARY KEY (id)
);
ALTER SEQUENCE euro_market_10_year_germany_id_seq OWNED BY int_gov_bond_10_year_germany.id;

CREATE SEQUENCE euro_market_10_year_japan_id_seq AS integer;
CREATE TABLE int_gov_bond_10_year_japan
(
    id integer DEFAULT nextval('euro_market_10_year_japan_id_seq'::regclass) NOT NULL,
    global_id uuid NOT NULL,
    value double precision NOT NULL,
    date date NOT NULL,
    created timestamp without time zone DEFAULT now(),
    updated timestamp without time zone DEFAULT now(),
    CONSTRAINT euro_market_10_year_japan_pkey PRIMARY KEY (id)
);
ALTER SEQUENCE euro_market_10_year_japan_id_seq OWNED BY int_gov_bond_10_year_japan.id;

CREATE SEQUENCE euro_market_10_year_netherlands_id_seq AS integer;
CREATE TABLE int_gov_bond_10_year_netherlands
(
    id integer DEFAULT nextval('euro_market_10_year_netherlands_id_seq'::regclass) NOT NULL,
    global_id uuid NOT NULL,
    value double precision NOT NULL,
    date date NOT NULL,
    created timestamp without time zone DEFAULT now(),
    updated timestamp without time zone DEFAULT now(),
    CONSTRAINT euro_market_10_year_netherlands_pkey PRIMARY KEY (id)
);
ALTER SEQUENCE euro_market_10_year_netherlands_id_seq OWNED BY int_gov_bond_10_year_netherlands.id;

CREATE SEQUENCE euro_market_10_year_norway_id_seq AS integer;
CREATE TABLE int_gov_bond_10_year_norway
(
    id integer DEFAULT nextval('euro_market_10_year_norway_id_seq'::regclass) NOT NULL,
    global_id uuid NOT NULL,
    value double precision NOT NULL,
    date date NOT NULL,
    created timestamp without time zone DEFAULT now(),
    updated timestamp without time zone DEFAULT now(),
    CONSTRAINT euro_market_10_year_norway_pkey PRIMARY KEY (id)
);
ALTER SEQUENCE euro_market_10_year_norway_id_seq OWNED BY int_gov_bond_10_year_norway.id;

CREATE SEQUENCE euro_market_10_year_usa_id_seq AS integer;
CREATE TABLE int_gov_bond_10_year_usa
(
    id integer DEFAULT nextval('euro_market_10_year_usa_id_seq'::regclass) NOT NULL,
    global_id uuid NOT NULL,
    value double precision NOT NULL,
    date date NOT NULL,
    created timestamp without time zone DEFAULT now(),
    updated timestamp without time zone DEFAULT now(),
    CONSTRAINT euro_market_10_year_usa_pkey PRIMARY KEY (id)
);
ALTER SEQUENCE euro_market_10_year_usa_id_seq OWNED BY int_gov_bond_10_year_usa.id;

CREATE SEQUENCE euro_market_5_year_eur_id_seq AS integer;
CREATE TABLE int_gov_bond_5_year_eur
(
    id integer DEFAULT nextval('euro_market_5_year_eur_id_seq'::regclass) NOT NULL,
    global_id uuid NOT NULL,
    value double precision NOT NULL,
    date date NOT NULL,
    created timestamp without time zone DEFAULT now(),
    updated timestamp without time zone DEFAULT now(),
    CONSTRAINT euro_market_5_year_eur_pkey PRIMARY KEY (id)
);
ALTER SEQUENCE euro_market_5_year_eur_id_seq OWNED BY int_gov_bond_5_year_eur.id;

CREATE SEQUENCE euro_market_5_year_france_id_seq AS integer;
CREATE TABLE int_gov_bond_5_year_france
(
    id integer DEFAULT nextval('euro_market_5_year_france_id_seq'::regclass) NOT NULL,
    global_id uuid NOT NULL,
    value double precision NOT NULL,
    date date NOT NULL,
    created timestamp without time zone DEFAULT now(),
    updated timestamp without time zone DEFAULT now(),
    CONSTRAINT euro_market_5_year_france_pkey PRIMARY KEY (id)
);
ALTER SEQUENCE euro_market_5_year_france_id_seq OWNED BY int_gov_bond_5_year_france.id;

CREATE SEQUENCE euro_market_5_year_gb_id_seq AS integer;
CREATE TABLE int_gov_bond_5_year_gb
(
    id integer DEFAULT nextval('euro_market_5_year_gb_id_seq'::regclass) NOT NULL,
    global_id uuid NOT NULL,
    value double precision NOT NULL,
    date date NOT NULL,
    created timestamp without time zone DEFAULT now(),
    updated timestamp without time zone DEFAULT now(),
    CONSTRAINT euro_market_5_year_gb_pkey PRIMARY KEY (id)
);
ALTER SEQUENCE euro_market_5_year_gb_id_seq OWNED BY int_gov_bond_5_year_gb.id;

CREATE SEQUENCE euro_market_5_year_germany_id_seq AS integer;
CREATE TABLE int_gov_bond_5_year_germany
(
    id integer DEFAULT nextval('euro_market_5_year_germany_id_seq'::regclass) NOT NULL,
    global_id uuid NOT NULL,
    value double precision NOT NULL,
    date date NOT NULL,
    created timestamp without time zone DEFAULT now(),
    updated timestamp without time zone DEFAULT now(),
    CONSTRAINT euro_market_5_year_germany_pkey PRIMARY KEY (id)
);
ALTER SEQUENCE euro_market_5_year_germany_id_seq OWNED BY int_gov_bond_5_year_germany.id;

CREATE SEQUENCE euro_market_5_year_japan_id_seq AS integer;
CREATE TABLE int_gov_bond_5_year_japan
(
    id integer DEFAULT nextval('euro_market_5_year_japan_id_seq'::regclass) NOT NULL,
    global_id uuid NOT NULL,
    value double precision NOT NULL,
    date date NOT NULL,
    created timestamp without time zone DEFAULT now(),
    updated timestamp without time zone DEFAULT now(),
    CONSTRAINT euro_market_5_year_japan_pkey PRIMARY KEY (id)
);
ALTER SEQUENCE euro_market_5_year_japan_id_seq OWNED BY int_gov_bond_5_year_japan.id;

CREATE SEQUENCE euro_market_5_year_netherlands_id_seq AS integer;
CREATE TABLE int_gov_bond_5_year_netherlands
(
    id integer DEFAULT nextval('euro_market_5_year_netherlands_id_seq'::regclass) NOT NULL,
    global_id uuid NOT NULL,
    value double precision NOT NULL,
    date date NOT NULL,
    created timestamp without time zone DEFAULT now(),
    updated timestamp without time zone DEFAULT now(),
    CONSTRAINT euro_market_5_year_netherlands_pkey PRIMARY KEY (id)
);
ALTER SEQUENCE euro_market_5_year_netherlands_id_seq OWNED BY int_gov_bond_5_year_netherlands.id;

CREATE SEQUENCE euro_market_5_year_usa_id_seq AS integer;
CREATE TABLE int_gov_bond_5_year_usa
(
    id integer DEFAULT nextval('euro_market_5_year_usa_id_seq'::regclass) NOT NULL,
    global_id uuid NOT NULL,
    value double precision NOT NULL,
    date date NOT NULL,
    created timestamp without time zone DEFAULT now(),
    updated timestamp without time zone DEFAULT now(),
    CONSTRAINT euro_market_5_year_usa_pkey PRIMARY KEY (id)
);
ALTER SEQUENCE euro_market_5_year_usa_id_seq OWNED BY int_gov_bond_5_year_usa.id;

CREATE SEQUENCE policy_rate_sweden_id_seq AS integer;
CREATE TABLE policy_rate_sweden
(
    id integer DEFAULT nextval('policy_rate_sweden_id_seq'::regclass) NOT NULL,
    global_id uuid NOT NULL,
    policy_rate double precision NOT NULL,
    policy_rate_date date NOT NULL,
    created timestamp without time zone DEFAULT now(),
    updated timestamp without time zone DEFAULT now(),
    CONSTRAINT policy_rate_sweden_pkey PRIMARY KEY (id)
);
ALTER SEQUENCE policy_rate_sweden_id_seq OWNED BY policy_rate_sweden.id;

CREATE SEQUENCE swedish_government_bill_12_month_id_seq AS integer;
CREATE TABLE swedish_government_bill_12_month
(
    id integer DEFAULT nextval('swedish_government_bill_12_month_id_seq'::regclass) NOT NULL,
    global_id uuid NOT NULL,
    value double precision NOT NULL,
    date date NOT NULL,
    created timestamp without time zone DEFAULT now(),
    updated timestamp without time zone DEFAULT now(),
    CONSTRAINT swedish_government_bill_12_month_pkey PRIMARY KEY (id)
);
ALTER SEQUENCE swedish_government_bill_12_month_id_seq OWNED BY swedish_government_bill_12_month.id;

CREATE SEQUENCE swedish_government_bill_1_month_id_seq AS integer;
CREATE TABLE swedish_government_bill_1_month
(
    id integer DEFAULT nextval('swedish_government_bill_1_month_id_seq'::regclass) NOT NULL,
    global_id uuid NOT NULL,
    value double precision NOT NULL,
    date date NOT NULL,
    created timestamp without time zone DEFAULT now(),
    updated timestamp without time zone DEFAULT now(),
    CONSTRAINT swedish_government_bill_1_month_pkey PRIMARY KEY (id)
);
ALTER SEQUENCE swedish_government_bill_1_month_id_seq OWNED BY swedish_government_bill_1_month.id;

CREATE SEQUENCE swedish_government_bill_3_month_id_seq AS integer;
CREATE TABLE swedish_government_bill_3_month
(
    id integer DEFAULT nextval('swedish_government_bill_3_month_id_seq'::regclass) NOT NULL,
    global_id uuid NOT NULL,
    value double precision NOT NULL,
    date date NOT NULL,
    created timestamp without time zone DEFAULT now(),
    updated timestamp without time zone DEFAULT now(),
    CONSTRAINT swedish_government_bill_3_month_pkey PRIMARY KEY (id)
);
ALTER SEQUENCE swedish_government_bill_3_month_id_seq OWNED BY swedish_government_bill_3_month.id;

CREATE SEQUENCE swedish_government_bill_6_month_id_seq AS integer;
CREATE TABLE swedish_government_bill_6_month
(
    id integer DEFAULT nextval('swedish_government_bill_6_month_id_seq'::regclass) NOT NULL,
    global_id uuid NOT NULL,
    value double precision NOT NULL,
    date date NOT NULL,
    created timestamp without time zone DEFAULT now(),
    updated timestamp without time zone DEFAULT now(),
    CONSTRAINT swedish_government_bill_6_month_pkey PRIMARY KEY (id)
);
ALTER SEQUENCE swedish_government_bill_6_month_id_seq OWNED BY swedish_government_bill_6_month.id;

CREATE SEQUENCE swedish_government_bonds_10_month_id_seq AS integer;
CREATE TABLE swedish_government_bonds_10_year
(
    id integer DEFAULT nextval('swedish_government_bonds_10_month_id_seq'::regclass) NOT NULL,
    global_id uuid NOT NULL,
    value double precision NOT NULL,
    date date NOT NULL,
    created timestamp without time zone DEFAULT now(),
    updated timestamp without time zone DEFAULT now(),
    CONSTRAINT swedish_government_bonds_10_month_pkey PRIMARY KEY (id)
);
ALTER SEQUENCE swedish_government_bonds_10_month_id_seq OWNED BY swedish_government_bonds_10_year.id;

CREATE SEQUENCE swedish_government_bonds_2_month_id_seq AS integer;
CREATE TABLE swedish_government_bonds_2_year
(
    id integer DEFAULT nextval('swedish_government_bonds_2_month_id_seq'::regclass) NOT NULL,
    global_id uuid NOT NULL,
    value double precision NOT NULL,
    date date NOT NULL,
    created timestamp without time zone DEFAULT now(),
    updated timestamp without time zone DEFAULT now(),
    CONSTRAINT swedish_government_bonds_2_month_pkey PRIMARY KEY (id)
);
ALTER SEQUENCE swedish_government_bonds_2_month_id_seq OWNED BY swedish_government_bonds_2_year.id;

CREATE SEQUENCE swedish_government_bonds_5_month_id_seq AS integer;
CREATE TABLE swedish_government_bonds_5_year
(
    id integer DEFAULT nextval('swedish_government_bonds_5_month_id_seq'::regclass) NOT NULL,
    global_id uuid NOT NULL,
    value double precision NOT NULL,
    date date NOT NULL,
    created timestamp without time zone DEFAULT now(),
    updated timestamp without time zone DEFAULT now(),
    CONSTRAINT swedish_government_bonds_5_month_pkey PRIMARY KEY (id)
);
ALTER SEQUENCE swedish_government_bonds_5_month_id_seq OWNED BY swedish_government_bonds_5_year.id;

CREATE SEQUENCE swedish_government_bonds_7_month_id_seq AS integer;
CREATE TABLE swedish_government_bonds_7_year
(
    id integer DEFAULT nextval('swedish_government_bonds_7_month_id_seq'::regclass) NOT NULL,
    global_id uuid NOT NULL,
    value double precision NOT NULL,
    date date NOT NULL,
    created timestamp without time zone DEFAULT now(),
    updated timestamp without time zone DEFAULT now(),
    CONSTRAINT swedish_government_bonds_7_month_pkey PRIMARY KEY (id)
);
ALTER SEQUENCE swedish_government_bonds_7_month_id_seq OWNED BY swedish_government_bonds_7_year.id;

CREATE INDEX eurostat_national_debt_series_name ON eurostat_national_debt_series USING btree (series_name);
CREATE INDEX fed_series_name ON fed_series USING btree (series_name);

-- id and global_id are the monolith's values, as literals, so no extension is needed. /series-config lists the configs
-- in id order, and /chart-data picks each series' colour from its position in that order
INSERT INTO series_configuration (id, global_id, name, display_name, scrape_url, category, unit, subcategory, country,
                                  maturity_months, scrape_flow)
VALUES
(1, '7ee6722b-1682-4c42-a8f3-73324a4b4f1d', 'PolicyRateSweden', 'Styrränta Sverige',
        'https://api.riksbank.se/swea/v1/Observations/SECBREPOEFF/1994-06-01', 'Räntor', '%', 'Styrränta', 'Sverige', NULL, 'MACRO_POINT'),
(2, 'b42fdd73-9be5-4ad0-b95c-661c62f1837a', 'UsdSekExchangeRate', 'USD/SEK valutakurs',
        'https://api.riksbank.se/swea/v1/Observations/SEKUSDPMI/1993-01-04', 'Valuta', 'SEK/USD', 'Växelkurs', NULL, NULL, 'MACRO_POINT'),
(3, '4f7f53fa-03cd-4d33-a088-6437a8cbdbcb', 'GovernmentBondSweden2Year', 'Svensk statsobligation 2-års löptid',
        'https://api.riksbank.se/swea/v1/Observations/SEGVB2YC/1987-01-07', 'Statsobligationer', '%', 'Statsobligation', 'Sverige', 24, 'MACRO_POINT'),
(4, 'caa55474-687b-4021-9fd7-71f44953d48d', 'GovernmentBondSweden5Year', 'Svensk statsobligation 5-års löptid',
        'https://api.riksbank.se/swea/v1/Observations/SEGVB5YC/1985-01-02', 'Statsobligationer', '%', 'Statsobligation', 'Sverige', 60, 'MACRO_POINT'),
(5, '289e2c80-fae5-4d66-8a38-8cc9df40e737', 'GovernmentBondSweden7Year', 'Svensk statsobligation 7-års löptid',
        'https://api.riksbank.se/swea/v1/Observations/SEGVB7YC/1987-01-02', 'Statsobligationer', '%', 'Statsobligation', 'Sverige', 84, 'MACRO_POINT'),
(6, 'cdc3c226-9872-4744-b6c4-88884b6d0751', 'GovernmentBondSweden10Year', 'Svensk statsobligation 10-års löptid',
        'https://api.riksbank.se/swea/v1/Observations/SEGVB10YC/1987-01-02', 'Statsobligationer', '%', 'Statsobligation', 'Sverige', 120, 'MACRO_POINT'),
(7, '13557813-f1ed-4e3b-85e7-11691e432cf8', 'GovernmentBillSweden1Month', 'Svensk statsskuldväxel 1-månads löptid',
        'https://api.riksbank.se/swea/v1/Observations/SETB1MBENCHC/1983-01-03', 'Statsobligationer', '%', 'Statsskuldväxel', 'Sverige', 1, 'MACRO_POINT'),
(8, 'cd2197b1-42fe-4eec-b58c-134658fda267', 'GovernmentBillSweden3Month', 'Svensk statsskuldväxel 3-månads löptid',
        'https://api.riksbank.se/swea/v1/Observations/SETB3MBENCH/1983-01-03', 'Statsobligationer', '%', 'Statsskuldväxel', 'Sverige', 3, 'MACRO_POINT'),
(9, '9eae00e3-367c-409e-80c8-1721921c5bd7', 'GovernmentBillSweden6Month', 'Svensk statsskuldväxel 6-månads löptid',
        'https://api.riksbank.se/swea/v1/Observations/SETB6MBENCH/1984-01-02', 'Statsobligationer', '%', 'Statsskuldväxel', 'Sverige', 6, 'MACRO_POINT'),
(10, 'dbe26a13-f962-4ff3-bed4-0a3d8a8ab951', 'GovernmentBillSweden12Month', 'Svensk statsskuldväxel 12-månads löptid',
        'https://api.riksbank.se/swea/v1/Observations/SETB12MBENCH/1983-01-03', 'Statsobligationer', '%', 'Statsskuldväxel', 'Sverige', 12, 'MACRO_POINT'),
(11, '8cc189ea-4b95-469a-b1c0-5cade9a8f16c', 'EuroMarketRateDenmark3Month', 'Euromarknadsränta Danmark 3-månaders löptid',
        'https://api.riksbank.se/swea/v1/Observations/EUDP3MDKK/1981-11-12', 'Räntor', '%', 'Euromarknadsränta', 'Danmark', 3, 'MACRO_POINT'),
(12, '7a44f840-3a74-4706-8bd4-6f1a5c08b703', 'EuroMarketRateEur3Month', 'Euromarknadsränta EUR 3-månaders löptid',
        'https://api.riksbank.se/swea/v1/Observations/EUDP3MEUR/1999-01-04', 'Räntor', '%', 'Euromarknadsränta', 'Euroområdet', 3, 'MACRO_POINT'),
(13, '0d28a3c6-f000-4c0c-b29a-76fe76098e83', 'EuroMarketRateGB3Month', 'Euromarknadsränta Storbritannien 3-månaders löptid',
        'https://api.riksbank.se/swea/v1/Observations/EUDP3MGBP/1979-12-19', 'Räntor', '%', 'Euromarknadsränta', 'Storbritannien', 3, 'MACRO_POINT'),
(14, '1a53d58c-4304-4c60-a9dd-ae7d8b6f0460', 'EuroMarketRateJapan3Month', 'Euromarknadsränta Japan 3-månaders löptid',
        'https://api.riksbank.se/swea/v1/Observations/EUDP3MJPY/1979-11-29', 'Räntor', '%', 'Euromarknadsränta', 'Japan', 3, 'MACRO_POINT'),
(15, '35128b63-0151-4b84-899e-ccc63f5f3ef2', 'EuroMarketRateNorway3Month', 'Euromarknadsränta Norge 3-månaders löptid',
        'https://api.riksbank.se/swea/v1/Observations/EUDP3MNOK/1981-11-12', 'Räntor', '%', 'Euromarknadsränta', 'Norge', 3, 'MACRO_POINT'),
(16, 'ceeb8a88-ead0-47db-8a3e-880dd7bc6483', 'EuroMarketRateUsa3Month', 'Euromarknadsränta USA 3-månaders löptid',
        'https://api.riksbank.se/swea/v1/Observations/EUDP3MUSD/1979-11-28', 'Räntor', '%', 'Euromarknadsränta', 'USA', 3, 'MACRO_POINT'),
(17, 'd0caae36-51a5-4f35-a6c7-ef34554ffc82', 'EuroMarketRateDenmark6Month', 'Euromarknadsränta Danmark 6-månaders löptid',
        'https://api.riksbank.se/swea/v1/Observations/EUDP6MDKK/1981-11-12', 'Räntor', '%', 'Euromarknadsränta', 'Danmark', 6, 'MACRO_POINT'),
(18, '0159a441-608b-4520-aa0f-f94386f69b50', 'EuroMarketRateEur6Month', 'Euromarknadsränta EUR 6-månaders löptid',
        'https://api.riksbank.se/swea/v1/Observations/EUDP6MEUR/1999-01-04', 'Räntor', '%', 'Euromarknadsränta', 'Euroområdet', 6, 'MACRO_POINT'),
(19, '45bafb67-87d1-428e-8a73-49a0998a988c', 'EuroMarketRateGB6Month', 'Euromarknadsränta Storbritannien 6-månaders löptid',
        'https://api.riksbank.se/swea/v1/Observations/EUDP6MGBP/1979-11-28', 'Räntor', '%', 'Euromarknadsränta', 'Storbritannien', 6, 'MACRO_POINT'),
(20, 'ec50f987-df91-429f-b5ad-cd52c3e05978', 'EuroMarketRateJapan6Month', 'Euromarknadsränta Japan 6-månaders löptid',
        'https://api.riksbank.se/swea/v1/Observations/EUDP6MJPY/1979-11-29', 'Räntor', '%', 'Euromarknadsränta', 'Japan', 6, 'MACRO_POINT'),
(21, '93e94af7-007e-4552-9127-f35ff349ce66', 'EuroMarketRateNorway6Month', 'Euromarknadsränta Norge 6-månaders löptid',
        'https://api.riksbank.se/swea/v1/Observations/EUDP6MNOK/1981-11-12', 'Räntor', '%', 'Euromarknadsränta', 'Norge', 6, 'MACRO_POINT'),
(22, 'aebd2513-e531-40f0-96ae-4497d1154d3d', 'EuroMarketRateUsa6Month', 'Euromarknadsränta USA 6-månaders löptid',
        'https://api.riksbank.se/swea/v1/Observations/EUDP6MUSD/1979-11-28', 'Räntor', '%', 'Euromarknadsränta', 'USA', 6, 'MACRO_POINT'),
(23, '9028293f-6aa0-405c-831d-4ffe958a54e5', 'IntGovBondsEur5Year', 'Statsobligation EUR 5-års löptid',
        'https://api.riksbank.se/swea/v1/Observations/EMGVB5Y/1999-01-04', 'Statsobligationer', '%', 'Statsobligation', 'Euroområdet', 60, 'MACRO_POINT'),
(24, '574aaa15-4324-4336-8c84-2abf9139082f', 'IntGovBondsFrance5Year', 'Statsobligation Frankrike 5-års löptid',
        'https://api.riksbank.se/swea/v1/Observations/FRGVB5Y/1988-02-08', 'Statsobligationer', '%', 'Statsobligation', 'Frankrike', 60, 'MACRO_POINT'),
(25, 'c6c9f59d-9fe4-4144-b957-8b73c667b02f', 'IntGovBondsGB5Year', 'Statsobligation Storbritannien 5-års löptid',
        'https://api.riksbank.se/swea/v1/Observations/GBGVB5Y/1987-01-02', 'Statsobligationer', '%', 'Statsobligation', 'Storbritannien', 60, 'MACRO_POINT'),
(26, '79a6ae9f-d033-4027-b737-a3bc718b2255', 'IntGovBondsGermany5Year', 'Statsobligation Tyskland 5-års löptid',
        'https://api.riksbank.se/swea/v1/Observations/DEGVB5Y/1987-02-09', 'Statsobligationer', '%', 'Statsobligation', 'Tyskland', 60, 'MACRO_POINT'),
(27, '37f77453-bc82-4aad-963d-0839a12ef705', 'IntGovBondsJapan5Year', 'Statsobligation Japan 5-års löptid',
        'https://api.riksbank.se/swea/v1/Observations/JPGVB5Y/1987-03-23', 'Statsobligationer', '%', 'Statsobligation', 'Japan', 60, 'MACRO_POINT'),
(28, '284f7292-3c3e-49af-b503-267888e90005', 'IntGovBondsNetherlands5Year', 'Statsobligation Nederländerna 5-års löptid',
        'https://api.riksbank.se/swea/v1/Observations/NLGVB5Y/1987-02-09', 'Statsobligationer', '%', 'Statsobligation', 'Nederländerna', 60, 'MACRO_POINT'),
(29, '49735eb3-1b18-4856-98f3-d151e3187e5a', 'IntGovBondsUsa5Year', 'Statsobligation USA 5-års löptid',
        'https://api.riksbank.se/swea/v1/Observations/USGVB5Y/1987-02-02', 'Statsobligationer', '%', 'Statsobligation', 'USA', 60, 'MACRO_POINT'),
(30, 'c888ca08-6042-40f6-bdae-119773c3b14a', 'IntGovBondsDenmark10Year', 'Statsobligation Danmark 10-års löptid',
        'https://api.riksbank.se/swea/v1/Observations/DKGVB10Y/1982-01-04', 'Statsobligationer', '%', 'Statsobligation', 'Danmark', 120, 'MACRO_POINT'),
(31, '9285e3fb-cc3a-4c28-a267-5f46281ced7a', 'IntGovBondsEur10Year', 'Statsobligation EUR 10-års löptid',
        'https://api.riksbank.se/swea/v1/Observations/EMGVB10Y/1999-01-04', 'Statsobligationer', '%', 'Statsobligation', 'Euroområdet', 120, 'MACRO_POINT'),
(32, 'd55d3eb5-9c1a-4f2c-92bc-ea9e8693a836', 'IntGovBondsFinland10Year', 'Statsobligation Finland 10-års löptid',
        'https://api.riksbank.se/swea/v1/Observations/FIGVB10Y/1990-04-02', 'Statsobligationer', '%', 'Statsobligation', 'Finland', 120, 'MACRO_POINT'),
(33, '1b7a8495-54ba-4579-9620-078dcdbf945b', 'IntGovBondsFrance10Year', 'Statsobligation Frankrike 10-års löptid',
        'https://api.riksbank.se/swea/v1/Observations/FRGVB10Y/1988-02-08', 'Statsobligationer', '%', 'Statsobligation', 'Frankrike', 120, 'MACRO_POINT'),
(34, '054c72da-62a0-408e-b9ae-8060e930f62e', 'IntGovBondsGB10Year', 'Statsobligation Storbritannien 10-års löptid',
        'https://api.riksbank.se/swea/v1/Observations/GBGVB10Y/1987-01-02', 'Statsobligationer', '%', 'Statsobligation', 'Storbritannien', 120, 'MACRO_POINT'),
(35, '7170b951-573a-4817-aed3-255a76b70704', 'IntGovBondsGermany10Year', 'Statsobligation Tyskland 10-års löptid',
        'https://api.riksbank.se/swea/v1/Observations/DEGVB10Y/1987-02-09', 'Statsobligationer', '%', 'Statsobligation', 'Tyskland', 120, 'MACRO_POINT'),
(36, '6769b72e-cabf-4765-8cad-66ea90ad3e60', 'IntGovBondsJapan10Year', 'Statsobligation Japan 10-års löptid',
        'https://api.riksbank.se/swea/v1/Observations/JPGVB10Y/1987-01-05', 'Statsobligationer', '%', 'Statsobligation', 'Japan', 120, 'MACRO_POINT'),
(37, '97bc8a6e-8852-4bfa-b93b-d69f89a85d9b', 'IntGovBondsNetherlands10Year', 'Statsobligation Nederländerna 10-års löptid',
        'https://api.riksbank.se/swea/v1/Observations/NLGVB10Y/1987-02-09', 'Statsobligationer', '%', 'Statsobligation', 'Nederländerna', 120, 'MACRO_POINT'),
(38, '34d9f8ba-8331-4037-bc5c-1904c9972dcb', 'IntGovBondsNorway10Year', 'Statsobligation Norge 10-års löptid',
        'https://api.riksbank.se/swea/v1/Observations/NOGVB10Y/1990-05-31', 'Statsobligationer', '%', 'Statsobligation', 'Norge', 120, 'MACRO_POINT'),
(39, '0ba12a3e-06ca-48c9-b907-7d90f5c31ea9', 'IntGovBondsUsa10Year', 'Statsobligation USA 10-års löptid',
        'https://api.riksbank.se/swea/v1/Observations/USGVB10Y/1991-01-02', 'Statsobligationer', '%', 'Statsobligation', 'USA', 120, 'MACRO_POINT'),
(40, 'a73e57e3-9944-4073-9162-fd570c98a66c', 'FEDUnemploymentRate', 'Arbetslöshet USA',
        'https://api.stlouisfed.org/fred/series/observations?series_id=UNRATE', 'Nyckeltal USA', '%', 'Arbetsmarknad', 'USA', NULL, 'MACRO_POINT'),
(41, '37482a8b-d42d-4042-83d7-cfb59ec22393', 'FEDGPD', 'BNP USA',
        'https://api.stlouisfed.org/fred/series/observations?series_id=FYGDP', 'Nyckeltal USA', 'mdr USD', 'BNP', 'USA', NULL, 'MACRO_POINT'),
(42, '8aac8cdb-5936-4a17-86a6-763eded8f03b', 'FEDTotalPublicDebt', 'Statsskuld i miljoner dollar, USA',
        'https://api.stlouisfed.org/fred/series/observations?series_id=GFDEBTN', 'Statsskuld', 'mn USD', 'Nominell', 'USA', NULL, 'MACRO_POINT'),
(43, '5ccbf9ef-d8c3-4f07-898f-bae7246ae3ab', 'FEDPublicDebtAsGDPPercentage', 'Statsskuld i % av BNP, USA',
        'https://api.stlouisfed.org/fred/series/observations?series_id=GFDEGDQ188S', 'Statsskuld', '% av BNP', 'Andel av BNP', 'USA', NULL, 'MACRO_POINT'),
(44, '70fe8273-ed64-4a1f-a13c-c6aa536f296d', 'FEDSurplusOrDeficit', 'Federalt överskott eller underskott USA',
        'https://api.stlouisfed.org/fred/series/observations?series_id=FYFSD', 'Nyckeltal USA', 'mn USD', 'Budgetsaldo', 'USA', NULL, 'MACRO_POINT'),
(45, 'bddba32b-14d1-46ea-b2ca-13f74801b143', 'FEDEffectiveFederalFundsRate', 'Styrränta USA',
        'https://api.stlouisfed.org/fred/series/observations?series_id=EFFR', 'Räntor', '%', 'Styrränta', 'USA', NULL, 'MACRO_POINT'),
(46, '95bb6acb-3107-4cf5-8916-fa1424e8e301', 'FEDSPX500', 'S&P 500',
        'https://api.stlouisfed.org/fred/series/observations?series_id=SP500', 'Nyckeltal USA', 'punkter', 'Aktieindex', 'USA', NULL, 'MACRO_POINT'),
(47, '818eea17-15c3-4d19-aae8-90e56696e12a', 'FEDDJ', 'Dow Jones Industrial Average',
        'https://api.stlouisfed.org/fred/series/observations?series_id=DJIA', 'Nyckeltal USA', 'punkter', 'Aktieindex', 'USA', NULL, 'MACRO_POINT'),
(48, 'beab7fc9-b8ec-4fe3-a214-1b3bf2121782', 'FEDN100', 'NASDAQ 100 Index',
        'https://api.stlouisfed.org/fred/series/observations?series_id=NASDAQ100', 'Nyckeltal USA', 'punkter', 'Aktieindex', 'USA', NULL, 'MACRO_POINT'),
(49, '6dd0d59e-20b6-4c32-bd52-8fb354f4b59d', 'FEDVIX', 'CBOE Volatility Index: VIX',
        'https://api.stlouisfed.org/fred/series/observations?series_id=VIXCLS', 'Nyckeltal USA', 'index', 'Volatilitet', 'USA', NULL, 'MACRO_POINT'),
(50, '3ce6c8e2-1eac-4a4d-9cf4-e1c983fff439', 'FEDCPI', 'Median Consumer Price Index',
        'https://api.stlouisfed.org/fred/series/observations?series_id=MEDCPIM158SFRBCLE', 'Nyckeltal USA', '%', 'Inflation', 'USA', NULL, 'MACRO_POINT'),
(51, 'b77c07af-7c68-4cdc-a830-68981bc38df4', 'DebtMioEurAt', 'Statsskuld i miljoner euro, Österrike',
        'https://ec.europa.eu/eurostat/api/dissemination/statistics/1.0/data/gov_10dd_edpt1?freq=A&unit=MIO_EUR&sector=S13&na_item=GD&geo=at', 'Statsskuld', 'mn EUR', 'Nominell', 'Österrike', NULL, 'MACRO_POINT'),
(52, 'a9615ac6-76e5-4339-b9e6-1b204a07281d', 'DebtMioEurBe', 'Statsskuld i miljoner euro, Belgien',
        'https://ec.europa.eu/eurostat/api/dissemination/statistics/1.0/data/gov_10dd_edpt1?freq=A&unit=MIO_EUR&sector=S13&na_item=GD&geo=be', 'Statsskuld', 'mn EUR', 'Nominell', 'Belgien', NULL, 'MACRO_POINT'),
(53, '643b6796-a281-4737-9733-33cda898d58c', 'DebtMioEurBg', 'Statsskuld i miljoner euro, Bulgarien',
        'https://ec.europa.eu/eurostat/api/dissemination/statistics/1.0/data/gov_10dd_edpt1?freq=A&unit=MIO_EUR&sector=S13&na_item=GD&geo=bg', 'Statsskuld', 'mn EUR', 'Nominell', 'Bulgarien', NULL, 'MACRO_POINT'),
(54, '9bbbb888-6859-40ee-9cb3-fee973e658f8', 'DebtMioEurCy', 'Statsskuld i miljoner euro, Cypern',
        'https://ec.europa.eu/eurostat/api/dissemination/statistics/1.0/data/gov_10dd_edpt1?freq=A&unit=MIO_EUR&sector=S13&na_item=GD&geo=cy', 'Statsskuld', 'mn EUR', 'Nominell', 'Cypern', NULL, 'MACRO_POINT'),
(55, 'e73f32ef-8aa6-4c8c-a04a-e1cc42928787', 'DebtMioEurCz', 'Statsskuld i miljoner euro, Tjeckien',
        'https://ec.europa.eu/eurostat/api/dissemination/statistics/1.0/data/gov_10dd_edpt1?freq=A&unit=MIO_EUR&sector=S13&na_item=GD&geo=cz', 'Statsskuld', 'mn EUR', 'Nominell', 'Tjeckien', NULL, 'MACRO_POINT'),
(56, '913fbba1-dded-4403-866e-92415ddc43b8', 'DebtMioEurDe', 'Statsskuld i miljoner euro, Tyskland',
        'https://ec.europa.eu/eurostat/api/dissemination/statistics/1.0/data/gov_10dd_edpt1?freq=A&unit=MIO_EUR&sector=S13&na_item=GD&geo=de', 'Statsskuld', 'mn EUR', 'Nominell', 'Tyskland', NULL, 'MACRO_POINT'),
(57, '1fbce570-ca31-4b3b-bbb4-b6d0098ad4f4', 'DebtMioEurDk', 'Statsskuld i miljoner euro, Danmark',
        'https://ec.europa.eu/eurostat/api/dissemination/statistics/1.0/data/gov_10dd_edpt1?freq=A&unit=MIO_EUR&sector=S13&na_item=GD&geo=dk', 'Statsskuld', 'mn EUR', 'Nominell', 'Danmark', NULL, 'MACRO_POINT'),
(59, '78b31f67-2dce-470d-b850-41bf7a9faa0c', 'DebtMioEurEa20', 'Statsskuld i miljoner euro, Euroområdet (EA20)',
        'https://ec.europa.eu/eurostat/api/dissemination/statistics/1.0/data/gov_10dd_edpt1?freq=A&unit=MIO_EUR&sector=S13&na_item=GD&geo=ea20', 'Statsskuld', 'mn EUR', 'Nominell', 'Euroområdet', NULL, 'MACRO_POINT'),
(60, 'f0084852-b7de-4cbe-b022-74f23d6ad5ce', 'DebtMioEurEe', 'Statsskuld i miljoner euro, Estland',
        'https://ec.europa.eu/eurostat/api/dissemination/statistics/1.0/data/gov_10dd_edpt1?freq=A&unit=MIO_EUR&sector=S13&na_item=GD&geo=ee', 'Statsskuld', 'mn EUR', 'Nominell', 'Estland', NULL, 'MACRO_POINT'),
(61, '4fc78642-7459-41dd-b54e-de08769571d6', 'DebtMioEurEl', 'Statsskuld i miljoner euro, Grekland',
        'https://ec.europa.eu/eurostat/api/dissemination/statistics/1.0/data/gov_10dd_edpt1?freq=A&unit=MIO_EUR&sector=S13&na_item=GD&geo=el', 'Statsskuld', 'mn EUR', 'Nominell', 'Grekland', NULL, 'MACRO_POINT'),
(62, 'd612be7a-e511-4eac-b383-4dcbb68aaa1e', 'DebtMioEurEs', 'Statsskuld i miljoner euro, Spanien',
        'https://ec.europa.eu/eurostat/api/dissemination/statistics/1.0/data/gov_10dd_edpt1?freq=A&unit=MIO_EUR&sector=S13&na_item=GD&geo=es', 'Statsskuld', 'mn EUR', 'Nominell', 'Spanien', NULL, 'MACRO_POINT'),
(63, '7a31c194-34cb-4a35-911e-6f2a4b97a2c5', 'DebtMioEurEu27_2020', 'Statsskuld i miljoner euro, Europeiska unionen (EU27, från 2020)',
        'https://ec.europa.eu/eurostat/api/dissemination/statistics/1.0/data/gov_10dd_edpt1?freq=A&unit=MIO_EUR&sector=S13&na_item=GD&geo=eu27_2020', 'Statsskuld', 'mn EUR', 'Nominell', 'Europeiska unionen (EU27)', NULL, 'MACRO_POINT'),
(64, 'a1a96ae2-4900-4f2b-a160-a06b0a92d1e6', 'DebtMioEurFi', 'Statsskuld i miljoner euro, Finland',
        'https://ec.europa.eu/eurostat/api/dissemination/statistics/1.0/data/gov_10dd_edpt1?freq=A&unit=MIO_EUR&sector=S13&na_item=GD&geo=fi', 'Statsskuld', 'mn EUR', 'Nominell', 'Finland', NULL, 'MACRO_POINT'),
(65, '5c0299e3-430b-46bd-9f7d-f237a8b41947', 'DebtMioEurFr', 'Statsskuld i miljoner euro, Frankrike',
        'https://ec.europa.eu/eurostat/api/dissemination/statistics/1.0/data/gov_10dd_edpt1?freq=A&unit=MIO_EUR&sector=S13&na_item=GD&geo=fr', 'Statsskuld', 'mn EUR', 'Nominell', 'Frankrike', NULL, 'MACRO_POINT'),
(66, 'f569a5a2-0c98-4e28-9289-829563048dff', 'DebtMioEurHr', 'Statsskuld i miljoner euro, Kroatien',
        'https://ec.europa.eu/eurostat/api/dissemination/statistics/1.0/data/gov_10dd_edpt1?freq=A&unit=MIO_EUR&sector=S13&na_item=GD&geo=hr', 'Statsskuld', 'mn EUR', 'Nominell', 'Kroatien', NULL, 'MACRO_POINT'),
(67, 'd45e0225-0138-4738-8075-5923341f68fa', 'DebtMioEurHu', 'Statsskuld i miljoner euro, Ungern',
        'https://ec.europa.eu/eurostat/api/dissemination/statistics/1.0/data/gov_10dd_edpt1?freq=A&unit=MIO_EUR&sector=S13&na_item=GD&geo=hu', 'Statsskuld', 'mn EUR', 'Nominell', 'Ungern', NULL, 'MACRO_POINT'),
(68, '5ff026e1-b10b-45c8-bff3-2196dbc729ba', 'DebtMioEurIe', 'Statsskuld i miljoner euro, Irland',
        'https://ec.europa.eu/eurostat/api/dissemination/statistics/1.0/data/gov_10dd_edpt1?freq=A&unit=MIO_EUR&sector=S13&na_item=GD&geo=ie', 'Statsskuld', 'mn EUR', 'Nominell', 'Irland', NULL, 'MACRO_POINT'),
(69, 'ba73f8ee-6188-4e4a-83b5-e8a34af4a732', 'DebtMioEurIt', 'Statsskuld i miljoner euro, Italien',
        'https://ec.europa.eu/eurostat/api/dissemination/statistics/1.0/data/gov_10dd_edpt1?freq=A&unit=MIO_EUR&sector=S13&na_item=GD&geo=it', 'Statsskuld', 'mn EUR', 'Nominell', 'Italien', NULL, 'MACRO_POINT'),
(70, 'c12f88e8-f4aa-4589-ae9c-d8d466163e52', 'DebtMioEurLt', 'Statsskuld i miljoner euro, Litauen',
        'https://ec.europa.eu/eurostat/api/dissemination/statistics/1.0/data/gov_10dd_edpt1?freq=A&unit=MIO_EUR&sector=S13&na_item=GD&geo=lt', 'Statsskuld', 'mn EUR', 'Nominell', 'Litauen', NULL, 'MACRO_POINT'),
(71, 'd2e35410-2968-41b8-be04-02ca83e23dd4', 'DebtMioEurLu', 'Statsskuld i miljoner euro, Luxemburg',
        'https://ec.europa.eu/eurostat/api/dissemination/statistics/1.0/data/gov_10dd_edpt1?freq=A&unit=MIO_EUR&sector=S13&na_item=GD&geo=lu', 'Statsskuld', 'mn EUR', 'Nominell', 'Luxemburg', NULL, 'MACRO_POINT'),
(72, '4ea59e8d-1a36-4083-ae20-a8f9e4faeae0', 'DebtMioEurLv', 'Statsskuld i miljoner euro, Lettland',
        'https://ec.europa.eu/eurostat/api/dissemination/statistics/1.0/data/gov_10dd_edpt1?freq=A&unit=MIO_EUR&sector=S13&na_item=GD&geo=lv', 'Statsskuld', 'mn EUR', 'Nominell', 'Lettland', NULL, 'MACRO_POINT'),
(73, 'bf1472d0-dc22-4dee-b333-b24390731791', 'DebtMioEurMt', 'Statsskuld i miljoner euro, Malta',
        'https://ec.europa.eu/eurostat/api/dissemination/statistics/1.0/data/gov_10dd_edpt1?freq=A&unit=MIO_EUR&sector=S13&na_item=GD&geo=mt', 'Statsskuld', 'mn EUR', 'Nominell', 'Malta', NULL, 'MACRO_POINT'),
(74, '00dba8df-e629-4bad-98f9-e3126ad19be7', 'DebtMioEurNl', 'Statsskuld i miljoner euro, Nederländerna',
        'https://ec.europa.eu/eurostat/api/dissemination/statistics/1.0/data/gov_10dd_edpt1?freq=A&unit=MIO_EUR&sector=S13&na_item=GD&geo=nl', 'Statsskuld', 'mn EUR', 'Nominell', 'Nederländerna', NULL, 'MACRO_POINT'),
(75, '96af3ef1-bd98-4d76-a8f7-6cac3aa48122', 'DebtMioEurPl', 'Statsskuld i miljoner euro, Polen',
        'https://ec.europa.eu/eurostat/api/dissemination/statistics/1.0/data/gov_10dd_edpt1?freq=A&unit=MIO_EUR&sector=S13&na_item=GD&geo=pl', 'Statsskuld', 'mn EUR', 'Nominell', 'Polen', NULL, 'MACRO_POINT'),
(76, '27480fbc-ebe3-4468-a240-736dc4e924d9', 'DebtMioEurPt', 'Statsskuld i miljoner euro, Portugal',
        'https://ec.europa.eu/eurostat/api/dissemination/statistics/1.0/data/gov_10dd_edpt1?freq=A&unit=MIO_EUR&sector=S13&na_item=GD&geo=pt', 'Statsskuld', 'mn EUR', 'Nominell', 'Portugal', NULL, 'MACRO_POINT'),
(77, '2b47febd-61b3-4c6f-9a62-b7655a190b6f', 'DebtMioEurRo', 'Statsskuld i miljoner euro, Rumänien',
        'https://ec.europa.eu/eurostat/api/dissemination/statistics/1.0/data/gov_10dd_edpt1?freq=A&unit=MIO_EUR&sector=S13&na_item=GD&geo=ro', 'Statsskuld', 'mn EUR', 'Nominell', 'Rumänien', NULL, 'MACRO_POINT'),
(78, '4941469d-6431-4dc0-83ca-606506599a1a', 'DebtMioEurSe', 'Statsskuld i miljoner euro, Sverige',
        'https://ec.europa.eu/eurostat/api/dissemination/statistics/1.0/data/gov_10dd_edpt1?freq=A&unit=MIO_EUR&sector=S13&na_item=GD&geo=se', 'Statsskuld', 'mn EUR', 'Nominell', 'Sverige', NULL, 'MACRO_POINT'),
(79, '7d16ebe1-c4a1-49b4-9cdf-01edc1c4946c', 'DebtMioEurSi', 'Statsskuld i miljoner euro, Slovenien',
        'https://ec.europa.eu/eurostat/api/dissemination/statistics/1.0/data/gov_10dd_edpt1?freq=A&unit=MIO_EUR&sector=S13&na_item=GD&geo=si', 'Statsskuld', 'mn EUR', 'Nominell', 'Slovenien', NULL, 'MACRO_POINT'),
(80, 'a316eef7-e93f-464b-ae35-23e2d883cb1a', 'DebtMioEurSk', 'Statsskuld i miljoner euro, Slovakien',
        'https://ec.europa.eu/eurostat/api/dissemination/statistics/1.0/data/gov_10dd_edpt1?freq=A&unit=MIO_EUR&sector=S13&na_item=GD&geo=sk', 'Statsskuld', 'mn EUR', 'Nominell', 'Slovakien', NULL, 'MACRO_POINT'),
(81, '2e108977-d2c0-4ac1-977e-f68463556a3f', 'DebtPcGdpAt', 'Statsskuld i % av BNP, Österrike',
        'https://ec.europa.eu/eurostat/api/dissemination/statistics/1.0/data/gov_10dd_edpt1?freq=A&unit=PC_GDP&sector=S13&na_item=GD&geo=at', 'Statsskuld', '% av BNP', 'Andel av BNP', 'Österrike', NULL, 'MACRO_POINT'),
(82, '162564a1-f1ab-4548-ad4b-cc51a39d3897', 'DebtPcGdpBe', 'Statsskuld i % av BNP, Belgien',
        'https://ec.europa.eu/eurostat/api/dissemination/statistics/1.0/data/gov_10dd_edpt1?freq=A&unit=PC_GDP&sector=S13&na_item=GD&geo=be', 'Statsskuld', '% av BNP', 'Andel av BNP', 'Belgien', NULL, 'MACRO_POINT'),
(83, '029a7038-2971-44ab-84b6-ecd8cc0d693b', 'DebtPcGdpBg', 'Statsskuld i % av BNP, Bulgarien',
        'https://ec.europa.eu/eurostat/api/dissemination/statistics/1.0/data/gov_10dd_edpt1?freq=A&unit=PC_GDP&sector=S13&na_item=GD&geo=bg', 'Statsskuld', '% av BNP', 'Andel av BNP', 'Bulgarien', NULL, 'MACRO_POINT'),
(84, '669a098b-acb1-49d9-9d31-a67b2b4d08ac', 'DebtPcGdpCy', 'Statsskuld i % av BNP, Cypern',
        'https://ec.europa.eu/eurostat/api/dissemination/statistics/1.0/data/gov_10dd_edpt1?freq=A&unit=PC_GDP&sector=S13&na_item=GD&geo=cy', 'Statsskuld', '% av BNP', 'Andel av BNP', 'Cypern', NULL, 'MACRO_POINT'),
(85, '52ebdc8d-6b7b-4836-be42-1f0cab73320b', 'DebtPcGdpCz', 'Statsskuld i % av BNP, Tjeckien',
        'https://ec.europa.eu/eurostat/api/dissemination/statistics/1.0/data/gov_10dd_edpt1?freq=A&unit=PC_GDP&sector=S13&na_item=GD&geo=cz', 'Statsskuld', '% av BNP', 'Andel av BNP', 'Tjeckien', NULL, 'MACRO_POINT'),
(86, 'a00beed9-a4df-4149-ab21-da11e87b3869', 'DebtPcGdpDe', 'Statsskuld i % av BNP, Tyskland',
        'https://ec.europa.eu/eurostat/api/dissemination/statistics/1.0/data/gov_10dd_edpt1?freq=A&unit=PC_GDP&sector=S13&na_item=GD&geo=de', 'Statsskuld', '% av BNP', 'Andel av BNP', 'Tyskland', NULL, 'MACRO_POINT'),
(87, 'b21bfc2e-08a1-4e95-b957-b3543f663ee5', 'DebtPcGdpDk', 'Statsskuld i % av BNP, Danmark',
        'https://ec.europa.eu/eurostat/api/dissemination/statistics/1.0/data/gov_10dd_edpt1?freq=A&unit=PC_GDP&sector=S13&na_item=GD&geo=dk', 'Statsskuld', '% av BNP', 'Andel av BNP', 'Danmark', NULL, 'MACRO_POINT'),
(89, '50a67092-d654-40c9-9169-d17b59867e98', 'DebtPcGdpEa20', 'Statsskuld i % av BNP, Euroområdet (EA20)',
        'https://ec.europa.eu/eurostat/api/dissemination/statistics/1.0/data/gov_10dd_edpt1?freq=A&unit=PC_GDP&sector=S13&na_item=GD&geo=ea20', 'Statsskuld', '% av BNP', 'Andel av BNP', 'Euroområdet', NULL, 'MACRO_POINT'),
(90, '9eb5467f-ee91-4ba6-bc53-ea870b915de5', 'DebtPcGdpEe', 'Statsskuld i % av BNP, Estland',
        'https://ec.europa.eu/eurostat/api/dissemination/statistics/1.0/data/gov_10dd_edpt1?freq=A&unit=PC_GDP&sector=S13&na_item=GD&geo=ee', 'Statsskuld', '% av BNP', 'Andel av BNP', 'Estland', NULL, 'MACRO_POINT'),
(91, 'ee5ce175-687c-4e8f-9577-2350e66cad46', 'DebtPcGdpEl', 'Statsskuld i % av BNP, Grekland',
        'https://ec.europa.eu/eurostat/api/dissemination/statistics/1.0/data/gov_10dd_edpt1?freq=A&unit=PC_GDP&sector=S13&na_item=GD&geo=el', 'Statsskuld', '% av BNP', 'Andel av BNP', 'Grekland', NULL, 'MACRO_POINT'),
(92, '1c77d80b-d92b-4a24-b236-868793ceb029', 'DebtPcGdpEs', 'Statsskuld i % av BNP, Spanien',
        'https://ec.europa.eu/eurostat/api/dissemination/statistics/1.0/data/gov_10dd_edpt1?freq=A&unit=PC_GDP&sector=S13&na_item=GD&geo=es', 'Statsskuld', '% av BNP', 'Andel av BNP', 'Spanien', NULL, 'MACRO_POINT'),
(93, 'c7fefb66-81db-40a2-8e96-2f5283bf4d6c', 'DebtPcGdpEu27_2020', 'Statsskuld i % av BNP, Europeiska unionen (EU27, från 2020)',
        'https://ec.europa.eu/eurostat/api/dissemination/statistics/1.0/data/gov_10dd_edpt1?freq=A&unit=PC_GDP&sector=S13&na_item=GD&geo=eu27_2020', 'Statsskuld', '% av BNP', 'Andel av BNP', 'Europeiska unionen (EU27)', NULL, 'MACRO_POINT'),
(94, '92124834-c2fa-45fe-9657-2f7bb2bb7cca', 'DebtPcGdpFi', 'Statsskuld i % av BNP, Finland',
        'https://ec.europa.eu/eurostat/api/dissemination/statistics/1.0/data/gov_10dd_edpt1?freq=A&unit=PC_GDP&sector=S13&na_item=GD&geo=fi', 'Statsskuld', '% av BNP', 'Andel av BNP', 'Finland', NULL, 'MACRO_POINT'),
(95, 'd440f147-d70b-4dc5-9bca-e02692eb4303', 'DebtPcGdpFr', 'Statsskuld i % av BNP, Frankrike',
        'https://ec.europa.eu/eurostat/api/dissemination/statistics/1.0/data/gov_10dd_edpt1?freq=A&unit=PC_GDP&sector=S13&na_item=GD&geo=fr', 'Statsskuld', '% av BNP', 'Andel av BNP', 'Frankrike', NULL, 'MACRO_POINT'),
(96, '49eaa770-f78b-4156-87c4-4a5608f64858', 'DebtPcGdpHr', 'Statsskuld i % av BNP, Kroatien',
        'https://ec.europa.eu/eurostat/api/dissemination/statistics/1.0/data/gov_10dd_edpt1?freq=A&unit=PC_GDP&sector=S13&na_item=GD&geo=hr', 'Statsskuld', '% av BNP', 'Andel av BNP', 'Kroatien', NULL, 'MACRO_POINT'),
(97, '19f7fd0e-1f5a-40f3-bf28-c36fd713b22e', 'DebtPcGdpHu', 'Statsskuld i % av BNP, Ungern',
        'https://ec.europa.eu/eurostat/api/dissemination/statistics/1.0/data/gov_10dd_edpt1?freq=A&unit=PC_GDP&sector=S13&na_item=GD&geo=hu', 'Statsskuld', '% av BNP', 'Andel av BNP', 'Ungern', NULL, 'MACRO_POINT'),
(98, '9c73a16e-4f9b-485a-b663-88808e0a8a8e', 'DebtPcGdpIe', 'Statsskuld i % av BNP, Irland',
        'https://ec.europa.eu/eurostat/api/dissemination/statistics/1.0/data/gov_10dd_edpt1?freq=A&unit=PC_GDP&sector=S13&na_item=GD&geo=ie', 'Statsskuld', '% av BNP', 'Andel av BNP', 'Irland', NULL, 'MACRO_POINT'),
(99, 'ac6ec3db-df69-4ae9-a72e-18b980dfaba8', 'DebtPcGdpIt', 'Statsskuld i % av BNP, Italien',
        'https://ec.europa.eu/eurostat/api/dissemination/statistics/1.0/data/gov_10dd_edpt1?freq=A&unit=PC_GDP&sector=S13&na_item=GD&geo=it', 'Statsskuld', '% av BNP', 'Andel av BNP', 'Italien', NULL, 'MACRO_POINT'),
(100, '6ee8e74c-a459-4628-8f00-a55bce5313e6', 'DebtPcGdpLt', 'Statsskuld i % av BNP, Litauen',
        'https://ec.europa.eu/eurostat/api/dissemination/statistics/1.0/data/gov_10dd_edpt1?freq=A&unit=PC_GDP&sector=S13&na_item=GD&geo=lt', 'Statsskuld', '% av BNP', 'Andel av BNP', 'Litauen', NULL, 'MACRO_POINT'),
(101, 'b56d545b-7a14-4d43-b9c5-3cf81a3736f8', 'DebtPcGdpLu', 'Statsskuld i % av BNP, Luxemburg',
        'https://ec.europa.eu/eurostat/api/dissemination/statistics/1.0/data/gov_10dd_edpt1?freq=A&unit=PC_GDP&sector=S13&na_item=GD&geo=lu', 'Statsskuld', '% av BNP', 'Andel av BNP', 'Luxemburg', NULL, 'MACRO_POINT'),
(102, '48ae2eed-2af4-4325-bc89-618b1f121693', 'DebtPcGdpLv', 'Statsskuld i % av BNP, Lettland',
        'https://ec.europa.eu/eurostat/api/dissemination/statistics/1.0/data/gov_10dd_edpt1?freq=A&unit=PC_GDP&sector=S13&na_item=GD&geo=lv', 'Statsskuld', '% av BNP', 'Andel av BNP', 'Lettland', NULL, 'MACRO_POINT'),
(103, 'b57cbe15-b7a3-4fd1-8238-6b21b22687dc', 'DebtPcGdpMt', 'Statsskuld i % av BNP, Malta',
        'https://ec.europa.eu/eurostat/api/dissemination/statistics/1.0/data/gov_10dd_edpt1?freq=A&unit=PC_GDP&sector=S13&na_item=GD&geo=mt', 'Statsskuld', '% av BNP', 'Andel av BNP', 'Malta', NULL, 'MACRO_POINT'),
(104, '4fe59f39-b316-424a-8775-9161cc89dd3d', 'DebtPcGdpNl', 'Statsskuld i % av BNP, Nederländerna',
        'https://ec.europa.eu/eurostat/api/dissemination/statistics/1.0/data/gov_10dd_edpt1?freq=A&unit=PC_GDP&sector=S13&na_item=GD&geo=nl', 'Statsskuld', '% av BNP', 'Andel av BNP', 'Nederländerna', NULL, 'MACRO_POINT'),
(105, 'bb79be0d-53d7-4410-9b4f-42d7fb07995a', 'DebtPcGdpPl', 'Statsskuld i % av BNP, Polen',
        'https://ec.europa.eu/eurostat/api/dissemination/statistics/1.0/data/gov_10dd_edpt1?freq=A&unit=PC_GDP&sector=S13&na_item=GD&geo=pl', 'Statsskuld', '% av BNP', 'Andel av BNP', 'Polen', NULL, 'MACRO_POINT'),
(106, '8c1339dc-a8f5-488f-8c85-bbc9680a30d7', 'DebtPcGdpPt', 'Statsskuld i % av BNP, Portugal',
        'https://ec.europa.eu/eurostat/api/dissemination/statistics/1.0/data/gov_10dd_edpt1?freq=A&unit=PC_GDP&sector=S13&na_item=GD&geo=pt', 'Statsskuld', '% av BNP', 'Andel av BNP', 'Portugal', NULL, 'MACRO_POINT'),
(107, 'eb16c4c7-2826-4a89-b71f-8a04e84cf212', 'DebtPcGdpRo', 'Statsskuld i % av BNP, Rumänien',
        'https://ec.europa.eu/eurostat/api/dissemination/statistics/1.0/data/gov_10dd_edpt1?freq=A&unit=PC_GDP&sector=S13&na_item=GD&geo=ro', 'Statsskuld', '% av BNP', 'Andel av BNP', 'Rumänien', NULL, 'MACRO_POINT'),
(108, '585d1593-cde6-42ff-af02-5411120e3228', 'DebtPcGdpSe', 'Statsskuld i % av BNP, Sverige',
        'https://ec.europa.eu/eurostat/api/dissemination/statistics/1.0/data/gov_10dd_edpt1?freq=A&unit=PC_GDP&sector=S13&na_item=GD&geo=se', 'Statsskuld', '% av BNP', 'Andel av BNP', 'Sverige', NULL, 'MACRO_POINT'),
(109, '117ad5c4-986f-4f2c-ac7d-800db7d69688', 'DebtPcGdpSi', 'Statsskuld i % av BNP, Slovenien',
        'https://ec.europa.eu/eurostat/api/dissemination/statistics/1.0/data/gov_10dd_edpt1?freq=A&unit=PC_GDP&sector=S13&na_item=GD&geo=si', 'Statsskuld', '% av BNP', 'Andel av BNP', 'Slovenien', NULL, 'MACRO_POINT'),
(110, '690b224a-cc19-49a9-96ee-c6b742d17948', 'DebtPcGdpSk', 'Statsskuld i % av BNP, Slovakien',
        'https://ec.europa.eu/eurostat/api/dissemination/statistics/1.0/data/gov_10dd_edpt1?freq=A&unit=PC_GDP&sector=S13&na_item=GD&geo=sk', 'Statsskuld', '% av BNP', 'Andel av BNP', 'Slovakien', NULL, 'MACRO_POINT');

SELECT setval('series_configuration_id_seq', 110);
