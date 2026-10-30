-- The FRED national debt series belong with the Eurostat national debts (issue #25), in the same subcategories and
-- with display names in the same pattern. FEDSurplusOrDeficit is a deficit, not a debt, and stays in Nyckeltal USA.
UPDATE series_configuration
SET category     = 'Statsskuld',
    subcategory  = 'Nominell',
    display_name = 'Statsskuld i miljoner dollar, USA'
WHERE name = 'FEDTotalPublicDebt';

UPDATE series_configuration
SET category     = 'Statsskuld',
    subcategory  = 'Andel av BNP',
    display_name = 'Statsskuld i % av BNP, USA'
WHERE name = 'FEDPublicDebtAsGDPPercentage';
