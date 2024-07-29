CREATE TABLE series_configuration
(
    id           SERIAL PRIMARY KEY,
    global_id    uuid         NOT NULL,
    name         VARCHAR(100) NOT NULL,
    display_name VARCHAR(200) NOT NULL,
    country      VARCHAR(100),
    period       VARCHAR(100),
    created      TIMESTAMP DEFAULT NOW(),
    updated      TIMESTAMP DEFAULT NOW()
);
