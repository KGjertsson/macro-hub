-- Which (indicator, geo_version, year) combinations have values. Kept by the persister in the same transaction as
-- scb_area_value, so /regional-statistics/indicators doesn't have to scan every area value
CREATE TABLE IF NOT EXISTS scb_availability
(
    indicator_id TEXT     NOT NULL REFERENCES scb_indicator (id) ON DELETE CASCADE,
    geo_version  TEXT     NOT NULL CHECK (geo_version IN ('DeSO2018', 'DeSO2025', 'RegSO2020', 'RegSO2025')),
    year         SMALLINT NOT NULL,
    PRIMARY KEY (indicator_id, geo_version, year)
);

INSERT INTO scb_availability (indicator_id, geo_version, year)
SELECT DISTINCT indicator_id, geo_version, year
FROM scb_area_value
;
