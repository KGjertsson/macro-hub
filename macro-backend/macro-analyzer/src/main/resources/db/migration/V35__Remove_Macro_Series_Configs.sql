-- macro-series moved to its own service, which has its own config rows and queue. ScrapeService refuses to start for a
-- config whose flow isn't registered, and a queue item without a config stays pending and fails on every run.
-- The 41 data tables stay until a later migration, once the new service has been verified.
DELETE FROM scrape_action_queue
WHERE dataset_name IN (SELECT name FROM series_configuration WHERE scrape_flow = 'MACRO_POINT');
DELETE FROM series_configuration WHERE scrape_flow = 'MACRO_POINT';
