-- V15 pointed the Japan 6-month euro market rate at the 3-month series (EUDP3MJPY).
-- EUDP6MJPY starts on 1979-11-29 according to the Riksbanken SWEA /Series metadata.
UPDATE series_configuration
SET scrape_url = 'https://api.riksbank.se/swea/v1/Observations/EUDP6MJPY/1979-11-29'
WHERE name = 'EuroMarketRateJapan6Month';

-- The stored points are 3-month values. A scrape fetches from the start date in scrape_url and
-- only skips points already stored, so an empty table is refilled on the next scrape.
DELETE FROM euro_market_6_month_japan;

-- V15 had a double slash in the UK 3-month URL
UPDATE series_configuration
SET scrape_url = 'https://api.riksbank.se/swea/v1/Observations/EUDP3MGBP/1979-12-19'
WHERE name = 'EuroMarketRateGB3Month';

-- EA19 is the euro area before Croatia joined in 2023; keep only EA20.
-- Nothing references these rows by foreign key: data and queue rows refer to the series by name.
DELETE FROM eurostat_national_debt_series WHERE series_name IN ('DebtMioEurEa19', 'DebtPcGdpEa19');
DELETE FROM scrape_action_queue WHERE dataset_name IN ('DebtMioEurEa19', 'DebtPcGdpEa19');
DELETE FROM series_configuration WHERE name IN ('DebtMioEurEa19', 'DebtPcGdpEa19');

-- One country label for the euro area, shared with the Riksbanken EUR series
UPDATE series_configuration
SET country = 'Euroområdet'
WHERE name IN ('DebtMioEurEa20', 'DebtPcGdpEa20');
