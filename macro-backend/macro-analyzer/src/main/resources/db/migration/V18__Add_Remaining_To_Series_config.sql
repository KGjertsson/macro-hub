CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE eurostat_national_debt_series
(
    id           SERIAL PRIMARY KEY,
    global_id    uuid         NOT NULL,
    series_name  VARCHAR(100) NOT NULL,
    value        FLOAT NOT NULL,
    date         DATE  NOT NULL,
    created      TIMESTAMP DEFAULT NOW(),
    updated      TIMESTAMP DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS eurostat_national_debt_series_name ON eurostat_national_debt_series (series_name);

INSERT INTO series_configuration (
    global_id,
    name,
    display_name,
    scrape_url,
    category
)
VALUES
    (uuid_generate_v4(), 'DebtMioEurAt', 'Statsskuld i miljoner euro, Österrike', 'https://ec.europa.eu/eurostat/api/dissemination/statistics/1.0/data/gov_10dd_edpt1', 'NationalDebtNominal'),
    (uuid_generate_v4(), 'DebtMioEurBe', 'Statsskuld i miljoner euro, Belgien', 'https://ec.europa.eu/eurostat/api/dissemination/statistics/1.0/data/gov_10dd_edpt1', 'NationalDebtNominal'),
    (uuid_generate_v4(), 'DebtMioEurBg', 'Statsskuld i miljoner euro, Bulgarien', 'https://ec.europa.eu/eurostat/api/dissemination/statistics/1.0/data/gov_10dd_edpt1', 'NationalDebtNominal'),
    (uuid_generate_v4(), 'DebtMioEurCy', 'Statsskuld i miljoner euro, Cypern', 'https://ec.europa.eu/eurostat/api/dissemination/statistics/1.0/data/gov_10dd_edpt1', 'NationalDebtNominal'),
    (uuid_generate_v4(), 'DebtMioEurCz', 'Statsskuld i miljoner euro, Tjeckien', 'https://ec.europa.eu/eurostat/api/dissemination/statistics/1.0/data/gov_10dd_edpt1', 'NationalDebtNominal'),
    (uuid_generate_v4(), 'DebtMioEurDe', 'Statsskuld i miljoner euro, Tyskland', 'https://ec.europa.eu/eurostat/api/dissemination/statistics/1.0/data/gov_10dd_edpt1', 'NationalDebtNominal'),
    (uuid_generate_v4(), 'DebtMioEurDk', 'Statsskuld i miljoner euro, Danmark', 'https://ec.europa.eu/eurostat/api/dissemination/statistics/1.0/data/gov_10dd_edpt1', 'NationalDebtNominal'),
    (uuid_generate_v4(), 'DebtMioEurEa19', 'Statsskuld i miljoner euro, Euroområdet (EA19)', 'https://ec.europa.eu/eurostat/api/dissemination/statistics/1.0/data/gov_10dd_edpt1', 'NationalDebtNominal'),
    (uuid_generate_v4(), 'DebtMioEurEa20', 'Statsskuld i miljoner euro, Euroområdet (EA20)', 'https://ec.europa.eu/eurostat/api/dissemination/statistics/1.0/data/gov_10dd_edpt1', 'NationalDebtNominal'),
    (uuid_generate_v4(), 'DebtMioEurEe', 'Statsskuld i miljoner euro, Estland', 'https://ec.europa.eu/eurostat/api/dissemination/statistics/1.0/data/gov_10dd_edpt1', 'NationalDebtNominal'),
    (uuid_generate_v4(), 'DebtMioEurEl', 'Statsskuld i miljoner euro, Grekland', 'https://ec.europa.eu/eurostat/api/dissemination/statistics/1.0/data/gov_10dd_edpt1', 'NationalDebtNominal'),
    (uuid_generate_v4(), 'DebtMioEurEs', 'Statsskuld i miljoner euro, Spanien', 'https://ec.europa.eu/eurostat/api/dissemination/statistics/1.0/data/gov_10dd_edpt1', 'NationalDebtNominal'),
    (uuid_generate_v4(), 'DebtMioEurEu27_2020', 'Statsskuld i miljoner euro, Europeiska unionen (EU27, från 2020)', 'https://ec.europa.eu/eurostat/api/dissemination/statistics/1.0/data/gov_10dd_edpt1', 'NationalDebtNominal'),
    (uuid_generate_v4(), 'DebtMioEurFi', 'Statsskuld i miljoner euro, Finland', 'https://ec.europa.eu/eurostat/api/dissemination/statistics/1.0/data/gov_10dd_edpt1', 'NationalDebtNominal'),
    (uuid_generate_v4(), 'DebtMioEurFr', 'Statsskuld i miljoner euro, Frankrike', 'https://ec.europa.eu/eurostat/api/dissemination/statistics/1.0/data/gov_10dd_edpt1?freq=A&unit=MIO_EUR&sector=S13&na_item=GD&geo=fr', 'NationalDebtNominal'),
    (uuid_generate_v4(), 'DebtMioEurHr', 'Statsskuld i miljoner euro, Kroatien', 'https://ec.europa.eu/eurostat/api/dissemination/statistics/1.0/data/gov_10dd_edpt1', 'NationalDebtNominal'),
    (uuid_generate_v4(), 'DebtMioEurHu', 'Statsskuld i miljoner euro, Ungern', 'https://ec.europa.eu/eurostat/api/dissemination/statistics/1.0/data/gov_10dd_edpt1', 'NationalDebtNominal'),
    (uuid_generate_v4(), 'DebtMioEurIe', 'Statsskuld i miljoner euro, Irland', 'https://ec.europa.eu/eurostat/api/dissemination/statistics/1.0/data/gov_10dd_edpt1', 'NationalDebtNominal'),
    (uuid_generate_v4(), 'DebtMioEurIt', 'Statsskuld i miljoner euro, Italien', 'https://ec.europa.eu/eurostat/api/dissemination/statistics/1.0/data/gov_10dd_edpt1', 'NationalDebtNominal'),
    (uuid_generate_v4(), 'DebtMioEurLt', 'Statsskuld i miljoner euro, Litauen', 'https://ec.europa.eu/eurostat/api/dissemination/statistics/1.0/data/gov_10dd_edpt1', 'NationalDebtNominal'),
    (uuid_generate_v4(), 'DebtMioEurLu', 'Statsskuld i miljoner euro, Luxemburg', 'https://ec.europa.eu/eurostat/api/dissemination/statistics/1.0/data/gov_10dd_edpt1', 'NationalDebtNominal'),
    (uuid_generate_v4(), 'DebtMioEurLv', 'Statsskuld i miljoner euro, Lettland', 'https://ec.europa.eu/eurostat/api/dissemination/statistics/1.0/data/gov_10dd_edpt1', 'NationalDebtNominal'),
    (uuid_generate_v4(), 'DebtMioEurMt', 'Statsskuld i miljoner euro, Malta', 'https://ec.europa.eu/eurostat/api/dissemination/statistics/1.0/data/gov_10dd_edpt1', 'NationalDebtNominal'),
    (uuid_generate_v4(), 'DebtMioEurNl', 'Statsskuld i miljoner euro, Nederländerna', 'https://ec.europa.eu/eurostat/api/dissemination/statistics/1.0/data/gov_10dd_edpt1', 'NationalDebtNominal'),
    (uuid_generate_v4(), 'DebtMioEurPl', 'Statsskuld i miljoner euro, Polen', 'https://ec.europa.eu/eurostat/api/dissemination/statistics/1.0/data/gov_10dd_edpt1', 'NationalDebtNominal'),
    (uuid_generate_v4(), 'DebtMioEurPt', 'Statsskuld i miljoner euro, Portugal', 'https://ec.europa.eu/eurostat/api/dissemination/statistics/1.0/data/gov_10dd_edpt1', 'NationalDebtNominal'),
    (uuid_generate_v4(), 'DebtMioEurRo', 'Statsskuld i miljoner euro, Rumänien', 'https://ec.europa.eu/eurostat/api/dissemination/statistics/1.0/data/gov_10dd_edpt1', 'NationalDebtNominal'),
    (uuid_generate_v4(), 'DebtMioEurSe', 'Statsskuld i miljoner euro, Sverige', 'https://ec.europa.eu/eurostat/api/dissemination/statistics/1.0/data/gov_10dd_edpt1', 'NationalDebtNominal'),
    (uuid_generate_v4(), 'DebtMioEurSi', 'Statsskuld i miljoner euro, Slovenien', 'https://ec.europa.eu/eurostat/api/dissemination/statistics/1.0/data/gov_10dd_edpt1', 'NationalDebtNominal'),
    (uuid_generate_v4(), 'DebtMioEurSk', 'Statsskuld i miljoner euro, Slovakien', 'https://ec.europa.eu/eurostat/api/dissemination/statistics/1.0/data/gov_10dd_edpt1', 'NationalDebtNominal')
;


INSERT INTO series_configuration (
    global_id,
    name,
    display_name,
    scrape_url,
    category
)
VALUES
    (uuid_generate_v4(), 'DebtPcGdpAt', 'Statsskuld i % av BNP, Österrike', 'https://ec.europa.eu/eurostat/api/dissemination/statistics/1.0/data/gov_10dd_edpt1', 'NationalDebtPctGDP'),
    (uuid_generate_v4(), 'DebtPcGdpBe', 'Statsskuld i % av BNP, Belgien', 'https://ec.europa.eu/eurostat/api/dissemination/statistics/1.0/data/gov_10dd_edpt1', 'NationalDebtPctGDP'),
    (uuid_generate_v4(), 'DebtPcGdpBg', 'Statsskuld i % av BNP, Bulgarien', 'https://ec.europa.eu/eurostat/api/dissemination/statistics/1.0/data/gov_10dd_edpt1', 'NationalDebtPctGDP'),
    (uuid_generate_v4(), 'DebtPcGdpCy', 'Statsskuld i % av BNP, Cypern', 'https://ec.europa.eu/eurostat/api/dissemination/statistics/1.0/data/gov_10dd_edpt1', 'NationalDebtPctGDP'),
    (uuid_generate_v4(), 'DebtPcGdpCz', 'Statsskuld i % av BNP, Tjeckien', 'https://ec.europa.eu/eurostat/api/dissemination/statistics/1.0/data/gov_10dd_edpt1', 'NationalDebtPctGDP'),
    (uuid_generate_v4(), 'DebtPcGdpDe', 'Statsskuld i % av BNP, Tyskland', 'https://ec.europa.eu/eurostat/api/dissemination/statistics/1.0/data/gov_10dd_edpt1', 'NationalDebtPctGDP'),
    (uuid_generate_v4(), 'DebtPcGdpDk', 'Statsskuld i % av BNP, Danmark', 'https://ec.europa.eu/eurostat/api/dissemination/statistics/1.0/data/gov_10dd_edpt1', 'NationalDebtPctGDP'),
    (uuid_generate_v4(), 'DebtPcGdpEa19', 'Statsskuld i % av BNP, Euroområdet (EA19)', 'https://ec.europa.eu/eurostat/api/dissemination/statistics/1.0/data/gov_10dd_edpt1', 'NationalDebtPctGDP'),
    (uuid_generate_v4(), 'DebtPcGdpEa20', 'Statsskuld i % av BNP, Euroområdet (EA20)', 'https://ec.europa.eu/eurostat/api/dissemination/statistics/1.0/data/gov_10dd_edpt1', 'NationalDebtPctGDP'),
    (uuid_generate_v4(), 'DebtPcGdpEe', 'Statsskuld i % av BNP, Estland', 'https://ec.europa.eu/eurostat/api/dissemination/statistics/1.0/data/gov_10dd_edpt1', 'NationalDebtPctGDP'),
    (uuid_generate_v4(), 'DebtPcGdpEl', 'Statsskuld i % av BNP, Grekland', 'https://ec.europa.eu/eurostat/api/dissemination/statistics/1.0/data/gov_10dd_edpt1', 'NationalDebtPctGDP'),
    (uuid_generate_v4(), 'DebtPcGdpEs', 'Statsskuld i % av BNP, Spanien', 'https://ec.europa.eu/eurostat/api/dissemination/statistics/1.0/data/gov_10dd_edpt1', 'NationalDebtPctGDP'),
    (uuid_generate_v4(), 'DebtPcGdpEu27_2020', 'Statsskuld i % av BNP, Europeiska unionen (EU27, från 2020)', 'https://ec.europa.eu/eurostat/api/dissemination/statistics/1.0/data/gov_10dd_edpt1', 'NationalDebtPctGDP'),
    (uuid_generate_v4(), 'DebtPcGdpFi', 'Statsskuld i % av BNP, Finland', 'https://ec.europa.eu/eurostat/api/dissemination/statistics/1.0/data/gov_10dd_edpt1', 'NationalDebtPctGDP'),
    (uuid_generate_v4(), 'DebtPcGdpFr', 'Statsskuld i % av BNP, Frankrike', 'https://ec.europa.eu/eurostat/api/dissemination/statistics/1.0/data/gov_10dd_edpt1', 'NationalDebtPctGDP'),
    (uuid_generate_v4(), 'DebtPcGdpHr', 'Statsskuld i % av BNP, Kroatien', 'https://ec.europa.eu/eurostat/api/dissemination/statistics/1.0/data/gov_10dd_edpt1', 'NationalDebtPctGDP'),
    (uuid_generate_v4(), 'DebtPcGdpHu', 'Statsskuld i % av BNP, Ungern', 'https://ec.europa.eu/eurostat/api/dissemination/statistics/1.0/data/gov_10dd_edpt1', 'NationalDebtPctGDP'),
    (uuid_generate_v4(), 'DebtPcGdpIe', 'Statsskuld i % av BNP, Irland', 'https://ec.europa.eu/eurostat/api/dissemination/statistics/1.0/data/gov_10dd_edpt1', 'NationalDebtPctGDP'),
    (uuid_generate_v4(), 'DebtPcGdpIt', 'Statsskuld i % av BNP, Italien', 'https://ec.europa.eu/eurostat/api/dissemination/statistics/1.0/data/gov_10dd_edpt1', 'NationalDebtPctGDP'),
    (uuid_generate_v4(), 'DebtPcGdpLt', 'Statsskuld i % av BNP, Litauen', 'https://ec.europa.eu/eurostat/api/dissemination/statistics/1.0/data/gov_10dd_edpt1', 'NationalDebtPctGDP'),
    (uuid_generate_v4(), 'DebtPcGdpLu', 'Statsskuld i % av BNP, Luxemburg', 'https://ec.europa.eu/eurostat/api/dissemination/statistics/1.0/data/gov_10dd_edpt1', 'NationalDebtPctGDP'),
    (uuid_generate_v4(), 'DebtPcGdpLv', 'Statsskuld i % av BNP, Lettland', 'https://ec.europa.eu/eurostat/api/dissemination/statistics/1.0/data/gov_10dd_edpt1', 'NationalDebtPctGDP'),
    (uuid_generate_v4(), 'DebtPcGdpMt', 'Statsskuld i % av BNP, Malta', 'https://ec.europa.eu/eurostat/api/dissemination/statistics/1.0/data/gov_10dd_edpt1', 'NationalDebtPctGDP'),
    (uuid_generate_v4(), 'DebtPcGdpNl', 'Statsskuld i % av BNP, Nederländerna', 'https://ec.europa.eu/eurostat/api/dissemination/statistics/1.0/data/gov_10dd_edpt1', 'NationalDebtPctGDP'),
    (uuid_generate_v4(), 'DebtPcGdpPl', 'Statsskuld i % av BNP, Polen', 'https://ec.europa.eu/eurostat/api/dissemination/statistics/1.0/data/gov_10dd_edpt1', 'NationalDebtPctGDP'),
    (uuid_generate_v4(), 'DebtPcGdpPt', 'Statsskuld i % av BNP, Portugal', 'https://ec.europa.eu/eurostat/api/dissemination/statistics/1.0/data/gov_10dd_edpt1', 'NationalDebtPctGDP'),
    (uuid_generate_v4(), 'DebtPcGdpRo', 'Statsskuld i % av BNP, Rumänien', 'https://ec.europa.eu/eurostat/api/dissemination/statistics/1.0/data/gov_10dd_edpt1', 'NationalDebtPctGDP'),
    (uuid_generate_v4(), 'DebtPcGdpSe', 'Statsskuld i % av BNP, Sverige', 'https://ec.europa.eu/eurostat/api/dissemination/statistics/1.0/data/gov_10dd_edpt1', 'NationalDebtPctGDP'),
    (uuid_generate_v4(), 'DebtPcGdpSi', 'Statsskuld i % av BNP, Slovenien', 'https://ec.europa.eu/eurostat/api/dissemination/statistics/1.0/data/gov_10dd_edpt1', 'NationalDebtPctGDP'),
    (uuid_generate_v4(), 'DebtPcGdpSk', 'Statsskuld i % av BNP, Slovakien', 'https://ec.europa.eu/eurostat/api/dissemination/statistics/1.0/data/gov_10dd_edpt1', 'NationalDebtPctGDP')
;
