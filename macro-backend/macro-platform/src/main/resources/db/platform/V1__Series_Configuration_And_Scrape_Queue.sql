-- The tables macro-platform's scrape core reads and writes. A service that turns on the platform Flyway run
-- (macro.platform.flyway.enabled) gets them from here, before its own migrations seed its series_configuration rows.
-- The monolith still has them from its own V1 to V32 and doesn't run these.
CREATE TABLE series_configuration
(
    id              SERIAL PRIMARY KEY,
    global_id       UUID         NOT NULL,
    name            VARCHAR(100) NOT NULL,
    display_name    VARCHAR(200) NOT NULL,
    created         TIMESTAMP DEFAULT NOW(),
    updated         TIMESTAMP DEFAULT NOW(),
    scrape_url      VARCHAR(200),
    category        VARCHAR(200),
    unit            VARCHAR(50),
    subcategory     VARCHAR(100),
    country         VARCHAR(100),
    maturity_months INTEGER,
    -- Which registered scrape flow handles this config. ScrapeService routes queue items by this id and fails at
    -- startup if a config names a flow nobody registered
    scrape_flow     VARCHAR(50)  NOT NULL
);

-- status: 0 pending, 1 done
CREATE TABLE scrape_action_queue
(
    id           SERIAL PRIMARY KEY,
    dataset_name VARCHAR(100) NOT NULL,
    scrape_date  TIMESTAMP    NOT NULL,
    created      TIMESTAMP DEFAULT NOW(),
    updated      TIMESTAMP DEFAULT NOW(),
    status       INTEGER      NOT NULL DEFAULT 0
);
