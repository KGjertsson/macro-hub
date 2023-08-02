CREATE TABLE policy_rate_sweden
(
    id               SERIAL PRIMARY KEY,
    global_id        uuid  NOT NULL,
    policy_rate      FLOAT NOT NULL,
    policy_rate_date DATE  NOT NULL,
    created          TIMESTAMP DEFAULT NOW(),
    updated          TIMESTAMP DEFAULT NOW()
);
