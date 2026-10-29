-- SCB statistics per DeSO and RegSO, fetched from the PxWeb API v2 (CC0)
INSERT INTO series_configuration (
    global_id,
    name,
    display_name,
    scrape_url,
    category
)
VALUES
    (uuid_generate_v4(), 'ScbRegionalStatistics', 'SCB områdesstatistik', 'https://statistikdatabasen.scb.se/api/v2', 'SCB')
;

-- Curated indicators. numerator and denominator are lists of sources:
--   [{"table": "TAB6574", "contents": "000007Y7", "select": {"Alder": ["65-69", "70-74"], "Kon": ["1+2"]}}]
-- Every dimension of the table except Region and Tid must be selected ("contents" selects ContentsCode).
-- Values of the codes selected in a dimension are summed. Sources are merged by year and geo version, a later
-- source replacing an earlier one. With a denominator the value is sum(numerator) / sum(denominator) * multiplier.
-- A changed selection is only re-fetched when the indicator has no rows, so delete its scb_area_value rows too
CREATE TABLE IF NOT EXISTS scb_indicator
(
    id          TEXT PRIMARY KEY,
    label       TEXT    NOT NULL,
    description TEXT    NOT NULL,
    category    TEXT    NOT NULL,
    unit        TEXT    NOT NULL,
    decimals    INTEGER NOT NULL,
    levels      TEXT[]  NOT NULL,
    numerator   JSONB   NOT NULL,
    denominator JSONB,
    multiplier  NUMERIC NOT NULL DEFAULT 1,
    sort_order  INTEGER NOT NULL
);

-- area_code is stored without SCB's _DeSO2025/_RegSO2025 suffix so it matches the boundary files
CREATE TABLE IF NOT EXISTS scb_area_value
(
    indicator_id TEXT     NOT NULL REFERENCES scb_indicator (id) ON DELETE CASCADE,
    year         SMALLINT NOT NULL,
    geo_version  TEXT     NOT NULL CHECK (geo_version IN ('DeSO2018', 'DeSO2025', 'RegSO2020', 'RegSO2025')),
    area_code    TEXT     NOT NULL,
    value        NUMERIC  NOT NULL,
    PRIMARY KEY (indicator_id, geo_version, year, area_code)
);

-- SCB's "updated" stamp per table; indicators whose tables are all unchanged are not fetched again
CREATE TABLE IF NOT EXISTS scb_table_state
(
    table_id TEXT PRIMARY KEY,
    updated  TIMESTAMPTZ NOT NULL
);

INSERT INTO scb_indicator (id, label, description, category, unit, decimals, levels, numerator, denominator, multiplier, sort_order)
VALUES
    -- DeSO and RegSO
    ('population', 'Folkmängd', 'Antal folkbokförda personer den 31 december.',
     'Befolkning', 'antal', 0, '{DeSO,RegSO}',
     '[{"table": "TAB6574", "contents": "000007Y7", "select": {"Alder": ["totalt"], "Kon": ["1+2"]}}]',
     NULL, 1, 10),
    ('share_0_19', 'Andel 0–19 år', 'Andel av befolkningen som är 0–19 år.',
     'Befolkning', '%', 1, '{DeSO,RegSO}',
     '[{"table": "TAB6574", "contents": "000007Y7", "select": {"Alder": ["-4", "5-9", "10-14", "15-19"], "Kon": ["1+2"]}}]',
     '[{"table": "TAB6574", "contents": "000007Y7", "select": {"Alder": ["totalt"], "Kon": ["1+2"]}}]',
     100, 20),
    ('share_65_plus', 'Andel 65 år och äldre', 'Andel av befolkningen som är 65 år eller äldre.',
     'Befolkning', '%', 1, '{DeSO,RegSO}',
     '[{"table": "TAB6574", "contents": "000007Y7", "select": {"Alder": ["65-69", "70-74", "75-79", "80-"], "Kon": ["1+2"]}}]',
     '[{"table": "TAB6574", "contents": "000007Y7", "select": {"Alder": ["totalt"], "Kon": ["1+2"]}}]',
     100, 30),
    ('share_foreign_background', 'Andel med utländsk bakgrund',
     'Andel av befolkningen som är födda utomlands eller har två utrikes födda föräldrar.',
     'Befolkning', '%', 1, '{DeSO,RegSO}',
     '[{"table": "TAB6571", "contents": "000007Y4", "select": {"UtlBakgrund": ["1"], "Kon": ["1+2"]}}]',
     '[{"table": "TAB6571", "contents": "000007Y4", "select": {"UtlBakgrund": ["SA"], "Kon": ["1+2"]}}]',
     100, 40),
    ('share_born_outside_europe', 'Andel födda utanför Europa',
     'Andel av befolkningen som är född utanför Europa (inklusive uppgift saknas).',
     'Befolkning', '%', 1, '{DeSO,RegSO}',
     '[{"table": "TAB6572", "contents": "000007Y5", "select": {"Fodelseregion": ["öv"], "Kon": ["1+2"]}}]',
     '[{"table": "TAB6572", "contents": "000007Y5", "select": {"Fodelseregion": ["tot"], "Kon": ["1+2"]}}]',
     100, 50),
    ('households', 'Antal hushåll', 'Antal hushåll den 31 december.',
     'Befolkning', 'antal', 0, '{DeSO,RegSO}',
     '[{"table": "TAB6568", "contents": "000007Y1", "select": {"Hushallstyp": ["TOTALT"]}}]',
     NULL, 1, 60),
    ('mean_net_income', 'Medelnettoinkomst', 'Medelvärde av nettoinkomsten för personer 20 år och äldre, tusental kronor i fasta priser.',
     'Inkomst', 'tkr', 1, '{DeSO,RegSO}',
     '[{"table": "TAB6683", "contents": "000008A4", "select": {"Inkomstkomponenter": ["240"], "Kon": ["1+2"]}}]',
     NULL, 1, 110),
    ('share_low_economic_standard', 'Andel med låg ekonomisk standard',
     'Andel av befolkningen i hushåll med en disponibel inkomst per konsumtionsenhet under 60 procent av medianen i riket.',
     'Inkomst', '%', 0, '{DeSO,RegSO}',
     '[{"table": "TAB6685", "contents": "000008AC", "select": {"Alder": ["tot"]}}]',
     NULL, 1, 120),
    ('share_high_economic_standard', 'Andel med hög ekonomisk standard',
     'Andel av befolkningen i hushåll med en disponibel inkomst per konsumtionsenhet över 200 procent av medianen i riket.',
     'Inkomst', '%', 0, '{DeSO,RegSO}',
     '[{"table": "TAB6685", "contents": "000008AD", "select": {"Alder": ["tot"]}}]',
     NULL, 1, 130),
    ('employment_rate_20_64', 'Andel förvärvsarbetande 20–64 år', 'Andel sysselsatta av befolkningen 20–64 år.',
     'Arbete', '%', 1, '{DeSO,RegSO}',
     '[{"table": "TAB6680", "contents": "0000089X", "select": {"Alder": ["20-64"], "Kon": ["1+2"]}}]',
     '[{"table": "TAB6680", "contents": "0000089Y", "select": {"Alder": ["20-64"], "Kon": ["1+2"]}}]',
     100, 210),
    ('share_post_secondary', 'Andel med eftergymnasial utbildning',
     'Andel av befolkningen 25–64 år (25–65 år från 2023) med eftergymnasial utbildning.',
     'Utbildning', '%', 1, '{DeSO,RegSO}',
     '[{"table": "TAB5956", "contents": "000005MO", "select": {"UtbildningsNiva": ["5", "6"]}},
       {"table": "TAB6534", "contents": "000007Z6", "select": {"UtbildningsNiva": ["5", "6"]}}]',
     '[{"table": "TAB5956", "contents": "000005MO", "select": {"UtbildningsNiva": ["21", "3+4", "5", "6", "US"]}},
       {"table": "TAB6534", "contents": "000007Z6", "select": {"UtbildningsNiva": ["21", "3+4", "5", "6", "US"]}}]',
     100, 310),
    ('share_rental', 'Andel boende i hyresrätt', 'Andel av befolkningen som bor i hyresrätt.',
     'Boende', '%', 1, '{DeSO,RegSO}',
     '[{"table": "TAB6253", "contents": "000007DQ", "select": {"Upplatelseform": ["HYRESRÄTT"]}}]',
     '[{"table": "TAB6253", "contents": "000007DQ", "select": {"Upplatelseform": ["TOTALT"]}}]',
     100, 410),
    ('share_condominium', 'Andel boende i bostadsrätt', 'Andel av befolkningen som bor i bostadsrätt.',
     'Boende', '%', 1, '{DeSO,RegSO}',
     '[{"table": "TAB6253", "contents": "000007DQ", "select": {"Upplatelseform": ["BOSTADSRÄTT"]}}]',
     '[{"table": "TAB6253", "contents": "000007DQ", "select": {"Upplatelseform": ["TOTALT"]}}]',
     100, 420),
    ('share_owner_occupied', 'Andel boende i äganderätt', 'Andel av befolkningen som bor i äganderätt.',
     'Boende', '%', 1, '{DeSO,RegSO}',
     '[{"table": "TAB6253", "contents": "000007DQ", "select": {"Upplatelseform": ["ÄG/ANDEL"]}}]',
     '[{"table": "TAB6253", "contents": "000007DQ", "select": {"Upplatelseform": ["TOTALT"]}}]',
     100, 430),
    ('share_small_house', 'Andel boende i småhus', 'Andel av befolkningen som bor i småhus.',
     'Boende', '%', 1, '{DeSO,RegSO}',
     '[{"table": "TAB6065", "contents": "0000077F", "select": {"Hustyp": ["SMÅHUS"]}}]',
     '[{"table": "TAB6065", "contents": "0000077F", "select": {"Hustyp": ["TOTALT"]}}]',
     100, 440),
    ('cars_per_1000', 'Personbilar per 1 000 invånare',
     'Personbilar i trafik registrerade på folkbokförda personer, per 1 000 invånare.',
     'Boende', 'per 1000 inv', 0, '{DeSO,RegSO}',
     '[{"table": "TAB6091", "contents": "000005Z2", "select": {"Bestand": ["ITRAF"]}},
       {"table": "TAB6589", "contents": "000007ZL", "select": {"Bestand": ["ITRAF"]}}]',
     '[{"table": "TAB6574", "contents": "000007Y7", "select": {"Alder": ["totalt"], "Kon": ["1+2"]}}]',
     1000, 450),
    -- RegSO only
    ('socioeconomic_index', 'Socioekonomiskt index',
     'SCB:s index över utbildning, ekonomisk standard och försörjningsstöd/långtidsarbetslöshet. Högre värde betyder sämre socioekonomiska förutsättningar.',
     'Socioekonomi', 'index', 1, '{RegSO}',
     '[{"table": "TAB6586", "contents": "000008IT", "select": {}}]',
     NULL, 1, 510),
    ('share_social_assistance', 'Andel med försörjningsstöd',
     'Andel av befolkningen 20–64 år med försörjningsstöd men utan övriga ersättningar.',
     'Socioekonomi', '%', 1, '{RegSO}',
     '[{"table": "TAB5750", "contents": "000004X5", "select": {"Kon": ["1+2"], "Bakgrund": ["tot20-64"]}}]',
     NULL, 1, 520),
    ('ill_health_days', 'Ohälsotal', 'Antal dagar med ersättning från sjukförsäkringen per person 20–64 år.',
     'Socioekonomi', 'dagar', 1, '{RegSO}',
     '[{"table": "TAB5751", "contents": "000004X8", "select": {"Kon": ["1+2"], "Bakgrund": ["tot20-64"]}}]',
     NULL, 1, 530),
    ('median_disposable_income', 'Median disponibel inkomst', 'Median av disponibel inkomst för personer 20–64 år, i prisbasbelopp.',
     'Inkomst', 'prisbasbelopp', 1, '{RegSO}',
     '[{"table": "TAB5750", "contents": "000004X4", "select": {"Kon": ["1+2"], "Bakgrund": ["tot20-64"]}}]',
     NULL, 1, 140),
    ('self_sufficiency_20_64', 'Självförsörjningsgrad 20–64 år', 'Andel självförsörjande av befolkningen 20–64 år.',
     'Arbete', '%', 1, '{RegSO}',
     '[{"table": "TAB6766", "contents": "000008GN", "select": {"Kon": ["1+2"], "Alder": ["20-64"], "Fodelseregion": ["samt"]}}]',
     NULL, 1, 220),
    ('upper_secondary_eligibility', 'Behörighet till gymnasiet', 'Andel av eleverna som är behöriga till gymnasiet.',
     'Utbildning', '%', 1, '{RegSO}',
     '[{"table": "TAB5725", "contents": "000004VC", "select": {"Bakgrund": ["1+2"]}}]',
     NULL, 1, 320),
    ('net_migration', 'Flyttnetto', 'Inrikes och utrikes flyttnetto i procent av befolkningen.',
     'Befolkning', '%', 2, '{RegSO}',
     '[{"table": "TAB5724", "select": {"ContentsCode": ["000004V8", "000004V9"], "Bakgrund": ["1+2"]}}]',
     NULL, 1, 70),
    ('turnout_riksdag', 'Valdeltagande riksdagsval', 'Andel röstberättigade 18 år och äldre som röstade i valet till riksdagen.',
     'Val', '%', 1, '{RegSO}',
     '[{"table": "TAB5748", "contents": "000004WU", "select": {"Bakgrund": ["tot18-"], "Kon": ["1+2"]}}]',
     NULL, 1, 610),
    ('turnout_region', 'Valdeltagande regionval', 'Andel röstberättigade 18 år och äldre som röstade i valet till regionfullmäktige.',
     'Val', '%', 1, '{RegSO}',
     '[{"table": "TAB5748", "contents": "000004WV", "select": {"Bakgrund": ["tot18-"], "Kon": ["1+2"]}}]',
     NULL, 1, 620),
    ('turnout_municipal', 'Valdeltagande kommunval', 'Andel röstberättigade 18 år och äldre som röstade i valet till kommunfullmäktige.',
     'Val', '%', 1, '{RegSO}',
     '[{"table": "TAB5748", "contents": "000004WW", "select": {"Bakgrund": ["tot18-"], "Kon": ["1+2"]}}]',
     NULL, 1, 630)
;
