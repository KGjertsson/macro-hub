CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

INSERT INTO series_configuration (global_id, name, display_name, country, period, scrape_url)
VALUES (uuid_generate_v4(), 'PolicyRateSweden', 'Styrränta Sverige', '', '',
        'https://api.riksbank.se/swea/v1/Observations/SECBREPOEFF/1994-06-01'),
       (uuid_generate_v4(), 'UsdSekExchangeRate', 'USD/SEK valutakurs', '', '',
        'https://api.riksbank.se/swea/v1/Observations/SEKUSDPMI/1993-01-04'),
       (uuid_generate_v4(), 'GovernmentBondSweden2Year',
        'Svensk statsobligation 2-års löptid', '', '',
        'https://api.riksbank.se/swea/v1/Observations/SEGVB2YC/1987-01-07'),
       (uuid_generate_v4(), 'GovernmentBondSweden5Year',
        'Svensk statsobligation 5-års löptid', '', '',
        'https://api.riksbank.se/swea/v1/Observations/SEGVB5YC/1985-01-02'),
       (uuid_generate_v4(), 'GovernmentBondSweden7Year',
        'Svensk statsobligation 7-års löptid', '', '',
        'https://api.riksbank.se/swea/v1/Observations/SEGVB7YC/1987-01-02'),
       (uuid_generate_v4(), 'GovernmentBondSweden10Year',
        'Svensk statsobligation 10-års löptid', '', '',
        'https://api.riksbank.se/swea/v1/Observations/SEGVB10YC/1987-01-02'),
       (uuid_generate_v4(), 'GovernmentBillSweden1Month', 'Svensk statsskuldväxel 1-månads löptid',
        '', '',
        'https://api.riksbank.se/swea/v1/Observations/SETB1MBENCHC/1983-01-03'),
       (uuid_generate_v4(), 'GovernmentBillSweden3Month', 'Svensk statsskuldväxel 3-månads löptid',
        '', '',
        'https://api.riksbank.se/swea/v1/Observations/SETB3MBENCH/1983-01-03'),
       (uuid_generate_v4(), 'GovernmentBillSweden6Month', 'Svensk statsskuldväxel 6-månads löptid',
        '', '',
        'https://api.riksbank.se/swea/v1/Observations/SETB6MBENCH/1984-01-02'),
       (uuid_generate_v4(), 'GovernmentBillSweden12Month',
        'Svensk statsskuldväxel 12-månads löptid', '', '',
        'https://api.riksbank.se/swea/v1/Observations/SETB12MBENCH/1983-01-03'),
       (uuid_generate_v4(), 'EuroMarketRateDenmark3Month',
        'Euromarknadsränta Danmark 3-månaders löptid', '', '',
        'https://api.riksbank.se/swea/v1/Observations/EUDP3MDKK/1981-11-12'),
       (uuid_generate_v4(), 'EuroMarketRateEur3Month', 'Euromarknadsränta EUR 3-månaders löptid',
        '', '',
        'https://api.riksbank.se/swea/v1/Observations/EUDP3MEUR/1999-01-04'),
       (uuid_generate_v4(), 'EuroMarketRateGB3Month',
        'Euromarknadsränta Storbritannien 3-månaders löptid', '', '',
        'https://api.riksbank.se/swea/v1/Observations//EUDP3MGBP/1979-12-19'),
       (uuid_generate_v4(), 'EuroMarketRateJapan3Month',
        'Euromarknadsränta Japan 3-månaders löptid', '', '',
        'https://api.riksbank.se/swea/v1/Observations/EUDP3MJPY/1979-11-29'),
       (uuid_generate_v4(), 'EuroMarketRateNorway3Month',
        'Euromarknadsränta Norge 3-månaders löptid', '', '',
        'https://api.riksbank.se/swea/v1/Observations/EUDP3MNOK/1981-11-12'),
       (uuid_generate_v4(), 'EuroMarketRateUsa3Month', 'Euromarknadsränta USA 3-månaders löptid',
        '', '',
        'https://api.riksbank.se/swea/v1/Observations/EUDP3MUSD/1979-11-28'),
       (uuid_generate_v4(), 'EuroMarketRateDenmark6Month',
        'Euromarknadsränta Danmark 6-månaders löptid', '', '',
        'https://api.riksbank.se/swea/v1/Observations/EUDP6MDKK/1981-11-12'),
       (uuid_generate_v4(), 'EuroMarketRateEur6Month', 'Euromarknadsränta EUR 6-månaders löptid',
        '', '',
        'https://api.riksbank.se/swea/v1/Observations/EUDP6MEUR/1999-01-04'),
       (uuid_generate_v4(), 'EuroMarketRateGB6Month',
        'Euromarknadsränta Storbritannien 6-månaders löptid', '', '',
        'https://api.riksbank.se/swea/v1/Observations/EUDP6MGBP/1979-11-28'),
       (uuid_generate_v4(), 'EuroMarketRateJapan6Month',
        'Euromarknadsränta Japan 6-månaders löptid', '', '',
        'https://api.riksbank.se/swea/v1/Observations/EUDP3MJPY/1979-11-29'),
       (uuid_generate_v4(), 'EuroMarketRateNorway6Month',
        'Euromarknadsränta Norge 6-månaders löptid', '', '',
        'https://api.riksbank.se/swea/v1/Observations/EUDP6MNOK/1981-11-12'),
       (uuid_generate_v4(), 'EuroMarketRateUsa6Month', 'Euromarknadsränta USA 6-månaders löptid',
        '', '',
        'https://api.riksbank.se/swea/v1/Observations/EUDP6MUSD/1979-11-28'),
       (uuid_generate_v4(), 'IntGovBondsEur5Year',
        'Statsobligation EUR 5-års löptid', '', '',
        'https://api.riksbank.se/swea/v1/Observations/EMGVB5Y/1999-01-04'),
       (uuid_generate_v4(), 'IntGovBondsFrance5Year',
        'Statsobligation Frankrike 5-års löptid', '', '',
        'https://api.riksbank.se/swea/v1/Observations/FRGVB5Y/1988-02-08'),
       (uuid_generate_v4(), 'IntGovBondsGB5Year',
        'Statsobligation Storbritannien 5-års löptid', '', '',
        'https://api.riksbank.se/swea/v1/Observations/GBGVB5Y/1987-01-02'),
       (uuid_generate_v4(), 'IntGovBondsGermany5Year',
        'Statsobligation Tyskland 5-års löptid', '', '',
        'https://api.riksbank.se/swea/v1/Observations/DEGVB5Y/1987-02-09'),
       (uuid_generate_v4(), 'IntGovBondsJapan5Year',
        'Statsobligation Japan 5-års löptid', '', '',
        'https://api.riksbank.se/swea/v1/Observations/JPGVB5Y/1987-03-23'),
       (uuid_generate_v4(), 'IntGovBondsNetherlands5Year',
        'Statsobligation Nederländerna 5-års löptid', '', '',
        'https://api.riksbank.se/swea/v1/Observations/NLGVB5Y/1987-02-09'),
       (uuid_generate_v4(), 'IntGovBondsUsa5Year',
        'Statsobligation USA 5-års löptid', '', '',
        'https://api.riksbank.se/swea/v1/Observations/USGVB5Y/1987-02-02'),
       (uuid_generate_v4(), 'IntGovBondsDenmark10Year',
        'Statsobligation Danmark 10-års löptid', '', '',
        'https://api.riksbank.se/swea/v1/Observations/DKGVB10Y/1982-01-04'),
       (uuid_generate_v4(), 'IntGovBondsEur10Year',
        'Statsobligation EUR 10-års löptid', '', '',
        'https://api.riksbank.se/swea/v1/Observations/EMGVB10Y/1999-01-04'),
       (uuid_generate_v4(), 'IntGovBondsFinland10Year',
        'Statsobligation Finland 10-års löptid', '', '',
        'https://api.riksbank.se/swea/v1/Observations/FIGVB10Y/1990-04-02'),
       (uuid_generate_v4(), 'IntGovBondsFrance10Year',
        'Statsobligation Frankrike 10-års löptid', '', '',
        'https://api.riksbank.se/swea/v1/Observations/FRGVB10Y/1988-02-08'),
       (uuid_generate_v4(), 'IntGovBondsGB10Year',
        'Statsobligation Storbritannien 10-års löptid', '', '',
        'https://api.riksbank.se/swea/v1/Observations/GBGVB10Y/1987-01-02'),
       (uuid_generate_v4(), 'IntGovBondsGermany10Year',
        'Statsobligation Tyskland 10-års löptid', '', '',
        'https://api.riksbank.se/swea/v1/Observations/DEGVB10Y/1987-02-09'),
       (uuid_generate_v4(), 'IntGovBondsJapan10Year',
        'Statsobligation Japan 10-års löptid', '', '',
        'https://api.riksbank.se/swea/v1/Observations/JPGVB10Y/1987-01-05'),
       (uuid_generate_v4(), 'IntGovBondsNetherlands10Year',
        'Statsobligation Nederländerna 10-års löptid', '', '',
        'https://api.riksbank.se/swea/v1/Observations/NLGVB10Y/1987-02-09'),
       (uuid_generate_v4(), 'IntGovBondsNorway10Year',
        'Statsobligation Norge 10-års löptid', '', '',
        'https://api.riksbank.se/swea/v1/Observations/NOGVB10Y/1990-05-31'),
       (uuid_generate_v4(), 'IntGovBondsUsa10Year',
        'Statsobligation USA 10-års löptid', '', '',
        'https://api.riksbank.se/swea/v1/Observations/USGVB10Y/1991-01-02');