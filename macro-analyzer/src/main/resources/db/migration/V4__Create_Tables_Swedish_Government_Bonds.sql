CREATE TABLE swedish_government_bonds_2_month
(
    id        SERIAL PRIMARY KEY,
    global_id uuid  NOT NULL,
    value     FLOAT NOT NULL,
    date      DATE  NOT NULL,
    created   TIMESTAMP DEFAULT NOW(),
    updated   TIMESTAMP DEFAULT NOW()
);

CREATE TABLE swedish_government_bonds_5_month
(
    id        SERIAL PRIMARY KEY,
    global_id uuid  NOT NULL,
    value     FLOAT NOT NULL,
    date      DATE  NOT NULL,
    created   TIMESTAMP DEFAULT NOW(),
    updated   TIMESTAMP DEFAULT NOW()
);

CREATE TABLE swedish_government_bonds_7_month
(
    id        SERIAL PRIMARY KEY,
    global_id uuid  NOT NULL,
    value     FLOAT NOT NULL,
    date      DATE  NOT NULL,
    created   TIMESTAMP DEFAULT NOW(),
    updated   TIMESTAMP DEFAULT NOW()
);

CREATE TABLE swedish_government_bonds_10_month
(
    id        SERIAL PRIMARY KEY,
    global_id uuid  NOT NULL,
    value     FLOAT NOT NULL,
    date      DATE  NOT NULL,
    created   TIMESTAMP DEFAULT NOW(),
    updated   TIMESTAMP DEFAULT NOW()
);
