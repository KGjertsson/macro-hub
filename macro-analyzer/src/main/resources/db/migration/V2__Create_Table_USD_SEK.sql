CREATE TABLE exchange_usd_sek
(
    id           SERIAL PRIMARY KEY,
    global_id    uuid      NOT NULL,
    usd_sek      FLOAT     NOT NULL,
    usd_sek_date TIMESTAMP NOT NULL,
    created      TIMESTAMP DEFAULT NOW(),
    updated      TIMESTAMP DEFAULT NOW()
);
