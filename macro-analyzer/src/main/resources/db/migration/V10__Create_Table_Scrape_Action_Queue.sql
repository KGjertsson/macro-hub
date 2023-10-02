CREATE TABLE scrape_action_queue
(
    id           SERIAL PRIMARY KEY,
    dataset_name VARCHAR(100) NOT NULL,
    scrape_date  DATE         NOT NULL,
    created      TIMESTAMP DEFAULT NOW(),
    updated      TIMESTAMP DEFAULT NOW()
);
