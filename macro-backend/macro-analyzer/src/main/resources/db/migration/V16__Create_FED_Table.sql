CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE fed_series
(
    id           SERIAL PRIMARY KEY,
    global_id    uuid         NOT NULL,
    series_name  VARCHAR(100) NOT NULL,
    value        FLOAT NOT NULL,
    date         DATE  NOT NULL,
    created      TIMESTAMP DEFAULT NOW(),
    updated      TIMESTAMP DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS fed_series_name ON fed_series (series_name);

INSERT INTO series_configuration (global_id, name, display_name, country, period, scrape_url)
VALUES
    (
        uuid_generate_v4(),
        'FEDUnemploymentRate',
        'Arbetslöshet USA',
        '',
        '',
        'https://api.stlouisfed.org/fred/series/observations?series_id=UNRATE'
    ),
    (
        uuid_generate_v4(),
        'FEDGPD',
        'BNP USA',
        '',
        '',
        'https://api.stlouisfed.org/fred/series/observations?series_id=FYGDP'
    ),
    (
        uuid_generate_v4(),
        'FEDTotalPublicDebt',
        'Total Statsskuld USA',
        '',
        '',
        'https://api.stlouisfed.org/fred/series/observations?series_id=GFDEBTN'
    ),
    (
        uuid_generate_v4(),
        'FEDPublicDebtAsGDPPercentage',
        'Statsskuld som procent av BNP USA',
        '',
        '',
        'https://api.stlouisfed.org/fred/series/observations?series_id=GFDEGDQ188S'
    ),
    (
        uuid_generate_v4(),
        'FEDSurplusOrDeficit',
        'Federalt överskott eller underskott USA',
        '',
        '',
        'https://api.stlouisfed.org/fred/series/observations?series_id=FYFSD'
    ),
    (
        uuid_generate_v4(),
        'FEDEffectiveFederalFundsRate',
        'Styrränta USA',
        '',
        '',
        'https://api.stlouisfed.org/fred/series/observations?series_id=EFFR'
    ),
    (
        uuid_generate_v4(),
        'FEDSPX500',
        'S&P 500',
        '',
        '',
        'https://api.stlouisfed.org/fred/series/observations?series_id=SP500'
    ),
    (
        uuid_generate_v4(),
        'FEDDJ',
        'Dow Jones Industrial Average',
        '',
        '',
        'https://api.stlouisfed.org/fred/series/observations?series_id=DJIA'
    ),
    (
        uuid_generate_v4(),
        'FEDN100',
        'NASDAQ 100 Index',
        '',
        '',
        'https://api.stlouisfed.org/fred/series/observations?series_id=NASDAQ100'
    ),
    (
        uuid_generate_v4(),
        'FEDVIX',
        'CBOE Volatility Index: VIX',
        '',
        '',
        'https://api.stlouisfed.org/fred/series/observations?series_id=VIXCLS'
    ),
    (
        uuid_generate_v4(),
        'FEDCPI',
        'Median Consumer Price Index',
        '',
        '',
        'https://api.stlouisfed.org/fred/series/observations?series_id=MEDCPIM158SFRBCLE'
    )
