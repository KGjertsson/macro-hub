ALTER TABLE series_configuration
    DROP COLUMN IF EXISTS country,
    DROP COLUMN IF EXISTS period,
    ADD COLUMN IF NOT EXISTS category VARCHAR(200);

UPDATE series_configuration
SET category = 'Ränta'
WHERE name in (
    'PolicyRateSweden',
    'EuroMarketRateDenmark3Month',
    'EuroMarketRateEur3Month',
    'EuroMarketRateGB3Month',
    'EuroMarketRateJapan3Month',
    'EuroMarketRateNorway3Month',
    'EuroMarketRateUsa3Month',
    'EuroMarketRateDenmark6Month',
    'EuroMarketRateEur6Month',
    'EuroMarketRateGB6Month',
    'EuroMarketRateJapan6Month',
    'EuroMarketRateNorway6Month',
    'EuroMarketRateUsa6Month',
    'FEDEffectiveFederalFundsRate'
);

UPDATE series_configuration
SET category = 'Valutakurs'
WHERE name in ('UsdSekExchangeRate');

UPDATE series_configuration
SET category = 'Statsobligation'
WHERE name in (
    'GovernmentBondSweden2Year',
    'GovernmentBondSweden5Year',
    'GovernmentBondSweden7Year',
    'GovernmentBondSweden10Year',
    'GovernmentBillSweden1Month',
    'GovernmentBillSweden3Month',
    'GovernmentBillSweden6Month',
    'GovernmentBillSweden12Month',
    'IntGovBondsEur5Year',
    'IntGovBondsFrance5Year',
    'IntGovBondsGB5Year',
    'IntGovBondsGermany5Year',
    'IntGovBondsJapan5Year',
    'IntGovBondsNetherlands5Year',
    'IntGovBondsUsa5Year',
    'IntGovBondsDenmark10Year',
    'IntGovBondsEur10Year',
    'IntGovBondsFinland10Year',
    'IntGovBondsFrance10Year',
    'IntGovBondsGB10Year',
    'IntGovBondsGermany10Year',
    'IntGovBondsJapan10Year',
    'IntGovBondsNetherlands10Year',
    'IntGovBondsNorway10Year',
    'IntGovBondsUsa10Year'
);

UPDATE series_configuration
SET category = 'Makroekonomiska nyckeltal'
WHERE name in (
    'FEDUnemploymentRate',
    'FEDGPD',
    'FEDTotalPublicDebt',
    'FEDPublicDebtAsGDPPercentage',
    'FEDSurplusOrDeficit'
);

UPDATE series_configuration
SET category = 'Makroekonomiska nyckeltal'
WHERE name in (
    'FEDSPX500',
    'FEDDJ',
    'FEDN100',
    'FEDVIX',
    'FEDCPI'
);
