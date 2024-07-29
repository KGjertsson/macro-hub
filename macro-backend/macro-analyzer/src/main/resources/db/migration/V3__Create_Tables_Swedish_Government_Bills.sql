CREATE TABLE swedish_government_bill_1_month
(
    id        SERIAL PRIMARY KEY,
    global_id uuid  NOT NULL,
    value     FLOAT NOT NULL,
    date      DATE  NOT NULL,
    created   TIMESTAMP DEFAULT NOW(),
    updated   TIMESTAMP DEFAULT NOW()
);

CREATE TABLE swedish_government_bill_3_month
(
    id        SERIAL PRIMARY KEY,
    global_id uuid  NOT NULL,
    value     FLOAT NOT NULL,
    date      DATE  NOT NULL,
    created   TIMESTAMP DEFAULT NOW(),
    updated   TIMESTAMP DEFAULT NOW()
);

CREATE TABLE swedish_government_bill_6_month
(
    id        SERIAL PRIMARY KEY,
    global_id uuid  NOT NULL,
    value     FLOAT NOT NULL,
    date      DATE  NOT NULL,
    created   TIMESTAMP DEFAULT NOW(),
    updated   TIMESTAMP DEFAULT NOW()
);

CREATE TABLE swedish_government_bill_12_month
(
    id        SERIAL PRIMARY KEY,
    global_id uuid  NOT NULL,
    value     FLOAT NOT NULL,
    date      DATE  NOT NULL,
    created   TIMESTAMP DEFAULT NOW(),
    updated   TIMESTAMP DEFAULT NOW()
);
