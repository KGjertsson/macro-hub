-- regional-statistics moved to its own service, which has its own config row and queue. ScrapeService refuses to start
-- for a config whose flow isn't registered, and a queue item without a config stays pending and fails on every run.
-- The scb_* tables stay until a later migration, once the new service has been verified.
DELETE FROM scrape_action_queue WHERE dataset_name = 'ScbRegionalStatistics';
DELETE FROM series_configuration WHERE name = 'ScbRegionalStatistics';
