-- Metadata for the chart series picker: unit, group within the category, country/area and maturity.
-- Riksdagen and Valmyndigheten configs are not chart series and keep these columns null.
ALTER TABLE series_configuration
    ADD COLUMN IF NOT EXISTS unit            VARCHAR(50),
    ADD COLUMN IF NOT EXISTS subcategory     VARCHAR(100),
    ADD COLUMN IF NOT EXISTS country         VARCHAR(100),
    ADD COLUMN IF NOT EXISTS maturity_months INTEGER;

-- Swedish display names for the chart categories
UPDATE series_configuration SET category = 'Räntor' WHERE category = 'Ränta';
UPDATE series_configuration SET category = 'Statsobligationer' WHERE category = 'Statsobligation';
UPDATE series_configuration SET category = 'Statsskuld' WHERE category IN ('NationalDebtNominal', 'NationalDebtPctGDP');
UPDATE series_configuration SET category = 'Valuta' WHERE category = 'Valutakurs';
UPDATE series_configuration SET category = 'Nyckeltal USA' WHERE category = 'Makroekonomiska nyckeltal';

-- V18 overwrote the Austrian debt-to-GDP display name without the country (geo=at)
UPDATE series_configuration
SET display_name = 'Statsskuld i % av BNP, Österrike'
WHERE name = 'DebtPcGdpAt';

-- Riksbanken, FRED and Eurostat series. Units follow the source:
-- Riksbanken rates and yields are in percent, SEKUSDPMI is SEK per USD,
-- Eurostat gov_10dd_edpt1 is MIO_EUR or PC_GDP, and FRED units are given per series.
UPDATE series_configuration s
SET unit            = m.unit,
    subcategory     = m.subcategory,
    country         = m.country,
    maturity_months = m.maturity_months
FROM (VALUES
    -- Räntor
    ('PolicyRateSweden',             '%', 'Styrränta',         'Sverige',         NULL::INTEGER),
    ('FEDEffectiveFederalFundsRate', '%', 'Styrränta',         'USA',             NULL),
    ('EuroMarketRateDenmark3Month',  '%', 'Euromarknadsränta', 'Danmark',         3),
    ('EuroMarketRateEur3Month',      '%', 'Euromarknadsränta', 'Euroområdet',     3),
    ('EuroMarketRateGB3Month',       '%', 'Euromarknadsränta', 'Storbritannien',  3),
    ('EuroMarketRateJapan3Month',    '%', 'Euromarknadsränta', 'Japan',           3),
    ('EuroMarketRateNorway3Month',   '%', 'Euromarknadsränta', 'Norge',           3),
    ('EuroMarketRateUsa3Month',      '%', 'Euromarknadsränta', 'USA',             3),
    ('EuroMarketRateDenmark6Month',  '%', 'Euromarknadsränta', 'Danmark',         6),
    ('EuroMarketRateEur6Month',      '%', 'Euromarknadsränta', 'Euroområdet',     6),
    ('EuroMarketRateGB6Month',       '%', 'Euromarknadsränta', 'Storbritannien',  6),
    ('EuroMarketRateJapan6Month',    '%', 'Euromarknadsränta', 'Japan',           6),
    ('EuroMarketRateNorway6Month',   '%', 'Euromarknadsränta', 'Norge',           6),
    ('EuroMarketRateUsa6Month',      '%', 'Euromarknadsränta', 'USA',             6),
    -- Valuta
    ('UsdSekExchangeRate',           'SEK', 'Växelkurs',       NULL,              NULL),
    -- Statsobligationer
    ('GovernmentBillSweden1Month',   '%', 'Statsskuldväxel',   'Sverige',         1),
    ('GovernmentBillSweden3Month',   '%', 'Statsskuldväxel',   'Sverige',         3),
    ('GovernmentBillSweden6Month',   '%', 'Statsskuldväxel',   'Sverige',         6),
    ('GovernmentBillSweden12Month',  '%', 'Statsskuldväxel',   'Sverige',         12),
    ('GovernmentBondSweden2Year',    '%', 'Statsobligation',   'Sverige',         24),
    ('GovernmentBondSweden5Year',    '%', 'Statsobligation',   'Sverige',         60),
    ('GovernmentBondSweden7Year',    '%', 'Statsobligation',   'Sverige',         84),
    ('GovernmentBondSweden10Year',   '%', 'Statsobligation',   'Sverige',         120),
    ('IntGovBondsEur5Year',          '%', 'Statsobligation',   'Euroområdet',     60),
    ('IntGovBondsFrance5Year',       '%', 'Statsobligation',   'Frankrike',       60),
    ('IntGovBondsGB5Year',           '%', 'Statsobligation',   'Storbritannien',  60),
    ('IntGovBondsGermany5Year',      '%', 'Statsobligation',   'Tyskland',        60),
    ('IntGovBondsJapan5Year',        '%', 'Statsobligation',   'Japan',           60),
    ('IntGovBondsNetherlands5Year',  '%', 'Statsobligation',   'Nederländerna',   60),
    ('IntGovBondsUsa5Year',          '%', 'Statsobligation',   'USA',             60),
    ('IntGovBondsDenmark10Year',     '%', 'Statsobligation',   'Danmark',         120),
    ('IntGovBondsEur10Year',         '%', 'Statsobligation',   'Euroområdet',     120),
    ('IntGovBondsFinland10Year',     '%', 'Statsobligation',   'Finland',         120),
    ('IntGovBondsFrance10Year',      '%', 'Statsobligation',   'Frankrike',       120),
    ('IntGovBondsGB10Year',          '%', 'Statsobligation',   'Storbritannien',  120),
    ('IntGovBondsGermany10Year',     '%', 'Statsobligation',   'Tyskland',        120),
    ('IntGovBondsJapan10Year',       '%', 'Statsobligation',   'Japan',           120),
    ('IntGovBondsNetherlands10Year', '%', 'Statsobligation',   'Nederländerna',   120),
    ('IntGovBondsNorway10Year',      '%', 'Statsobligation',   'Norge',           120),
    ('IntGovBondsUsa10Year',         '%', 'Statsobligation',   'USA',             120),
    -- Nyckeltal USA (FRED)
    ('FEDUnemploymentRate',          '%',        'Arbetsmarknad', 'USA', NULL), -- UNRATE: percent
    ('FEDGPD',                       'Mdr USD',  'BNP',           'USA', NULL), -- FYGDP: billions of dollars
    ('FEDTotalPublicDebt',           'MUSD',     'Statsfinanser', 'USA', NULL), -- GFDEBTN: millions of dollars
    ('FEDPublicDebtAsGDPPercentage', '% av BNP', 'Statsfinanser', 'USA', NULL), -- GFDEGDQ188S: percent of GDP
    ('FEDSurplusOrDeficit',          'MUSD',     'Statsfinanser', 'USA', NULL), -- FYFSD: millions of dollars
    ('FEDSPX500',                    'punkter',  'Aktieindex',    'USA', NULL), -- SP500: index points
    ('FEDDJ',                        'punkter',  'Aktieindex',    'USA', NULL), -- DJIA: index points
    ('FEDN100',                      'punkter',  'Aktieindex',    'USA', NULL), -- NASDAQ100: index points
    ('FEDVIX',                       'index',    'Volatilitet',   'USA', NULL), -- VIXCLS: index
    ('FEDCPI',                       '%',        'Inflation',     'USA', NULL)  -- MEDCPIM158SFRBCLE: percent change at annual rate
) AS m (name, unit, subcategory, country, maturity_months)
WHERE s.name = m.name;

-- Statsskuld (Eurostat gov_10dd_edpt1): the area is the geo code in the series name
UPDATE series_configuration s
SET unit        = CASE WHEN s.name LIKE 'DebtMioEur%' THEN 'M€' ELSE '% av BNP' END,
    subcategory = CASE WHEN s.name LIKE 'DebtMioEur%' THEN 'Nominell' ELSE 'Andel av BNP' END,
    country     = g.country
FROM (VALUES
    ('At',        'Österrike'),
    ('Be',        'Belgien'),
    ('Bg',        'Bulgarien'),
    ('Cy',        'Cypern'),
    ('Cz',        'Tjeckien'),
    ('De',        'Tyskland'),
    ('Dk',        'Danmark'),
    ('Ea19',      'Euroområdet (EA19)'),
    ('Ea20',      'Euroområdet (EA20)'),
    ('Ee',        'Estland'),
    ('El',        'Grekland'),
    ('Es',        'Spanien'),
    ('Eu27_2020', 'Europeiska unionen (EU27)'),
    ('Fi',        'Finland'),
    ('Fr',        'Frankrike'),
    ('Hr',        'Kroatien'),
    ('Hu',        'Ungern'),
    ('Ie',        'Irland'),
    ('It',        'Italien'),
    ('Lt',        'Litauen'),
    ('Lu',        'Luxemburg'),
    ('Lv',        'Lettland'),
    ('Mt',        'Malta'),
    ('Nl',        'Nederländerna'),
    ('Pl',        'Polen'),
    ('Pt',        'Portugal'),
    ('Ro',        'Rumänien'),
    ('Se',        'Sverige'),
    ('Si',        'Slovenien'),
    ('Sk',        'Slovakien')
) AS g (geo, country)
WHERE s.name IN ('DebtMioEur' || g.geo, 'DebtPcGdp' || g.geo);
