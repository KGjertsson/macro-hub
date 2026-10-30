-- Which registered scrape flow handles a config. ScrapeService routes queue items by this id and fails at startup if
-- a config names a flow nobody registered; /series-config only lists configs of the MACRO_POINT flow. New rows must
-- set it.
ALTER TABLE series_configuration
    ADD COLUMN IF NOT EXISTS scrape_flow VARCHAR(50);

UPDATE series_configuration SET scrape_flow = 'MEMBER_OF_PARLIAMENT' WHERE name = 'MembersOfParliament';
UPDATE series_configuration SET scrape_flow = 'MEMBER_ACTIVITY' WHERE name = 'MembersOfParliamentActivity';
UPDATE series_configuration SET scrape_flow = 'LOCAL_ELECTION'
WHERE name IN ('MunicipalElections', 'RegionElections', 'ParliamentElections');
UPDATE series_configuration SET scrape_flow = 'SCB_REGIONAL' WHERE name = 'ScbRegionalStatistics';
UPDATE series_configuration SET scrape_flow = 'MACRO_POINT' WHERE scrape_flow IS NULL;

ALTER TABLE series_configuration
    ALTER COLUMN scrape_flow SET NOT NULL;
