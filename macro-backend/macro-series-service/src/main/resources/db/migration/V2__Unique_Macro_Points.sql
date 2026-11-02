-- One row per series and date in all 41 tables, so that a scrape's upsert replaces a revised value (the writer's
-- ON CONFLICT needs these constraints). Duplicates keep the newest row: the latest created, then the highest id. They
-- come from identical copies inserted within a second on 2026-10-26 and from revisions, which were inserted as new rows.
DO $$
DECLARE
    target record;
BEGIN
    FOR target IN
        SELECT * FROM (VALUES
            ('euro_market_3_month_denmark', 'date'),
            ('euro_market_3_month_eur', 'date'),
            ('euro_market_3_month_gb', 'date'),
            ('euro_market_3_month_japan', 'date'),
            ('euro_market_3_month_norway', 'date'),
            ('euro_market_3_month_usa', 'date'),
            ('euro_market_6_month_denmark', 'date'),
            ('euro_market_6_month_eur', 'date'),
            ('euro_market_6_month_gb', 'date'),
            ('euro_market_6_month_japan', 'date'),
            ('euro_market_6_month_norway', 'date'),
            ('euro_market_6_month_usa', 'date'),
            ('eurostat_national_debt_series', 'series_name, date'),
            ('exchange_usd_sek', 'usd_sek_date'),
            ('fed_series', 'series_name, date'),
            ('int_gov_bond_10_year_denmark', 'date'),
            ('int_gov_bond_10_year_eur', 'date'),
            ('int_gov_bond_10_year_finland', 'date'),
            ('int_gov_bond_10_year_france', 'date'),
            ('int_gov_bond_10_year_gb', 'date'),
            ('int_gov_bond_10_year_germany', 'date'),
            ('int_gov_bond_10_year_japan', 'date'),
            ('int_gov_bond_10_year_netherlands', 'date'),
            ('int_gov_bond_10_year_norway', 'date'),
            ('int_gov_bond_10_year_usa', 'date'),
            ('int_gov_bond_5_year_eur', 'date'),
            ('int_gov_bond_5_year_france', 'date'),
            ('int_gov_bond_5_year_gb', 'date'),
            ('int_gov_bond_5_year_germany', 'date'),
            ('int_gov_bond_5_year_japan', 'date'),
            ('int_gov_bond_5_year_netherlands', 'date'),
            ('int_gov_bond_5_year_usa', 'date'),
            ('policy_rate_sweden', 'policy_rate_date'),
            ('swedish_government_bill_12_month', 'date'),
            ('swedish_government_bill_1_month', 'date'),
            ('swedish_government_bill_3_month', 'date'),
            ('swedish_government_bill_6_month', 'date'),
            ('swedish_government_bonds_10_year', 'date'),
            ('swedish_government_bonds_2_year', 'date'),
            ('swedish_government_bonds_5_year', 'date'),
            ('swedish_government_bonds_7_year', 'date')
        ) AS targets (table_name, key_columns)
    LOOP
        EXECUTE format(
            'DELETE FROM %1$I WHERE id IN (
                 SELECT id FROM (
                     SELECT id, row_number() OVER (PARTITION BY %2$s ORDER BY created DESC NULLS LAST, id DESC) AS position
                     FROM %1$I
                 ) ranked
                 WHERE position > 1
             )',
            target.table_name, target.key_columns
        );
        EXECUTE format(
            'ALTER TABLE %1$I ADD CONSTRAINT %2$I UNIQUE (%3$s)',
            target.table_name,
            target.table_name || '_' || replace(target.key_columns, ', ', '_') || '_key',
            target.key_columns
        );
    END LOOP;
END
$$;
