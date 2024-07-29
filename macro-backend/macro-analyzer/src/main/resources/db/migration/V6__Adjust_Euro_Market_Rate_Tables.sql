DROP TABLE euro_market_5_year_denmark;
DROP TABLE euro_market_5_year_norway;

CREATE TABLE euro_market_5_year_netherlands
(
    id        SERIAL PRIMARY KEY,
    global_id uuid  NOT NULL,
    value     FLOAT NOT NULL,
    date      DATE  NOT NULL,
    created   TIMESTAMP DEFAULT NOW(),
    updated   TIMESTAMP DEFAULT NOW()
);

CREATE TABLE euro_market_5_year_france
(
    id        SERIAL PRIMARY KEY,
    global_id uuid  NOT NULL,
    value     FLOAT NOT NULL,
    date      DATE  NOT NULL,
    created   TIMESTAMP DEFAULT NOW(),
    updated   TIMESTAMP DEFAULT NOW()
);

CREATE TABLE euro_market_5_year_germany
(
    id        SERIAL PRIMARY KEY,
    global_id uuid  NOT NULL,
    value     FLOAT NOT NULL,
    date      DATE  NOT NULL,
    created   TIMESTAMP DEFAULT NOW(),
    updated   TIMESTAMP DEFAULT NOW()
);
