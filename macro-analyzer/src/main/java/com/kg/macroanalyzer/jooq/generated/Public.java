/*
 * This file is generated by jOOQ.
 */
package com.kg.macroanalyzer.jooq.generated;


import com.kg.macroanalyzer.jooq.generated.tables.EuroMarket_3MonthDenmark;
import com.kg.macroanalyzer.jooq.generated.tables.EuroMarket_3MonthEur;
import com.kg.macroanalyzer.jooq.generated.tables.EuroMarket_3MonthGb;
import com.kg.macroanalyzer.jooq.generated.tables.EuroMarket_3MonthJapan;
import com.kg.macroanalyzer.jooq.generated.tables.EuroMarket_3MonthNorway;
import com.kg.macroanalyzer.jooq.generated.tables.EuroMarket_3MonthUsa;
import com.kg.macroanalyzer.jooq.generated.tables.EuroMarket_6MonthDenmark;
import com.kg.macroanalyzer.jooq.generated.tables.EuroMarket_6MonthEur;
import com.kg.macroanalyzer.jooq.generated.tables.EuroMarket_6MonthGb;
import com.kg.macroanalyzer.jooq.generated.tables.EuroMarket_6MonthJapan;
import com.kg.macroanalyzer.jooq.generated.tables.EuroMarket_6MonthNorway;
import com.kg.macroanalyzer.jooq.generated.tables.EuroMarket_6MonthUsa;
import com.kg.macroanalyzer.jooq.generated.tables.ExchangeUsdSek;
import com.kg.macroanalyzer.jooq.generated.tables.FlywaySchemaHistory;
import com.kg.macroanalyzer.jooq.generated.tables.IntGovBond_10YearDenmark;
import com.kg.macroanalyzer.jooq.generated.tables.IntGovBond_10YearEur;
import com.kg.macroanalyzer.jooq.generated.tables.IntGovBond_10YearFinland;
import com.kg.macroanalyzer.jooq.generated.tables.IntGovBond_10YearFrance;
import com.kg.macroanalyzer.jooq.generated.tables.IntGovBond_10YearGb;
import com.kg.macroanalyzer.jooq.generated.tables.IntGovBond_10YearGermany;
import com.kg.macroanalyzer.jooq.generated.tables.IntGovBond_10YearJapan;
import com.kg.macroanalyzer.jooq.generated.tables.IntGovBond_10YearNetherlands;
import com.kg.macroanalyzer.jooq.generated.tables.IntGovBond_10YearNorway;
import com.kg.macroanalyzer.jooq.generated.tables.IntGovBond_10YearUsa;
import com.kg.macroanalyzer.jooq.generated.tables.IntGovBond_5YearEur;
import com.kg.macroanalyzer.jooq.generated.tables.IntGovBond_5YearFrance;
import com.kg.macroanalyzer.jooq.generated.tables.IntGovBond_5YearGb;
import com.kg.macroanalyzer.jooq.generated.tables.IntGovBond_5YearGermany;
import com.kg.macroanalyzer.jooq.generated.tables.IntGovBond_5YearJapan;
import com.kg.macroanalyzer.jooq.generated.tables.IntGovBond_5YearNetherlands;
import com.kg.macroanalyzer.jooq.generated.tables.IntGovBond_5YearUsa;
import com.kg.macroanalyzer.jooq.generated.tables.PolicyRateSweden;
import com.kg.macroanalyzer.jooq.generated.tables.ScrapeActionQueue;
import com.kg.macroanalyzer.jooq.generated.tables.SwedishGovernmentBill_12Month;
import com.kg.macroanalyzer.jooq.generated.tables.SwedishGovernmentBill_1Month;
import com.kg.macroanalyzer.jooq.generated.tables.SwedishGovernmentBill_3Month;
import com.kg.macroanalyzer.jooq.generated.tables.SwedishGovernmentBill_6Month;
import com.kg.macroanalyzer.jooq.generated.tables.SwedishGovernmentBonds_10Year;
import com.kg.macroanalyzer.jooq.generated.tables.SwedishGovernmentBonds_2Year;
import com.kg.macroanalyzer.jooq.generated.tables.SwedishGovernmentBonds_5Year;
import com.kg.macroanalyzer.jooq.generated.tables.SwedishGovernmentBonds_7Year;

import java.util.Arrays;
import java.util.List;

import org.jooq.Catalog;
import org.jooq.Sequence;
import org.jooq.Table;
import org.jooq.impl.SchemaImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Public extends SchemaImpl {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>public</code>
     */
    public static final Public PUBLIC = new Public();

    /**
     * The table <code>public.euro_market_3_month_denmark</code>.
     */
    public final EuroMarket_3MonthDenmark EURO_MARKET_3_MONTH_DENMARK = EuroMarket_3MonthDenmark.EURO_MARKET_3_MONTH_DENMARK;

    /**
     * The table <code>public.euro_market_3_month_eur</code>.
     */
    public final EuroMarket_3MonthEur EURO_MARKET_3_MONTH_EUR = EuroMarket_3MonthEur.EURO_MARKET_3_MONTH_EUR;

    /**
     * The table <code>public.euro_market_3_month_gb</code>.
     */
    public final EuroMarket_3MonthGb EURO_MARKET_3_MONTH_GB = EuroMarket_3MonthGb.EURO_MARKET_3_MONTH_GB;

    /**
     * The table <code>public.euro_market_3_month_japan</code>.
     */
    public final EuroMarket_3MonthJapan EURO_MARKET_3_MONTH_JAPAN = EuroMarket_3MonthJapan.EURO_MARKET_3_MONTH_JAPAN;

    /**
     * The table <code>public.euro_market_3_month_norway</code>.
     */
    public final EuroMarket_3MonthNorway EURO_MARKET_3_MONTH_NORWAY = EuroMarket_3MonthNorway.EURO_MARKET_3_MONTH_NORWAY;

    /**
     * The table <code>public.euro_market_3_month_usa</code>.
     */
    public final EuroMarket_3MonthUsa EURO_MARKET_3_MONTH_USA = EuroMarket_3MonthUsa.EURO_MARKET_3_MONTH_USA;

    /**
     * The table <code>public.euro_market_6_month_denmark</code>.
     */
    public final EuroMarket_6MonthDenmark EURO_MARKET_6_MONTH_DENMARK = EuroMarket_6MonthDenmark.EURO_MARKET_6_MONTH_DENMARK;

    /**
     * The table <code>public.euro_market_6_month_eur</code>.
     */
    public final EuroMarket_6MonthEur EURO_MARKET_6_MONTH_EUR = EuroMarket_6MonthEur.EURO_MARKET_6_MONTH_EUR;

    /**
     * The table <code>public.euro_market_6_month_gb</code>.
     */
    public final EuroMarket_6MonthGb EURO_MARKET_6_MONTH_GB = EuroMarket_6MonthGb.EURO_MARKET_6_MONTH_GB;

    /**
     * The table <code>public.euro_market_6_month_japan</code>.
     */
    public final EuroMarket_6MonthJapan EURO_MARKET_6_MONTH_JAPAN = EuroMarket_6MonthJapan.EURO_MARKET_6_MONTH_JAPAN;

    /**
     * The table <code>public.euro_market_6_month_norway</code>.
     */
    public final EuroMarket_6MonthNorway EURO_MARKET_6_MONTH_NORWAY = EuroMarket_6MonthNorway.EURO_MARKET_6_MONTH_NORWAY;

    /**
     * The table <code>public.euro_market_6_month_usa</code>.
     */
    public final EuroMarket_6MonthUsa EURO_MARKET_6_MONTH_USA = EuroMarket_6MonthUsa.EURO_MARKET_6_MONTH_USA;

    /**
     * The table <code>public.exchange_usd_sek</code>.
     */
    public final ExchangeUsdSek EXCHANGE_USD_SEK = ExchangeUsdSek.EXCHANGE_USD_SEK;

    /**
     * The table <code>public.flyway_schema_history</code>.
     */
    public final FlywaySchemaHistory FLYWAY_SCHEMA_HISTORY = FlywaySchemaHistory.FLYWAY_SCHEMA_HISTORY;

    /**
     * The table <code>public.int_gov_bond_10_year_denmark</code>.
     */
    public final IntGovBond_10YearDenmark INT_GOV_BOND_10_YEAR_DENMARK = IntGovBond_10YearDenmark.INT_GOV_BOND_10_YEAR_DENMARK;

    /**
     * The table <code>public.int_gov_bond_10_year_eur</code>.
     */
    public final IntGovBond_10YearEur INT_GOV_BOND_10_YEAR_EUR = IntGovBond_10YearEur.INT_GOV_BOND_10_YEAR_EUR;

    /**
     * The table <code>public.int_gov_bond_10_year_finland</code>.
     */
    public final IntGovBond_10YearFinland INT_GOV_BOND_10_YEAR_FINLAND = IntGovBond_10YearFinland.INT_GOV_BOND_10_YEAR_FINLAND;

    /**
     * The table <code>public.int_gov_bond_10_year_france</code>.
     */
    public final IntGovBond_10YearFrance INT_GOV_BOND_10_YEAR_FRANCE = IntGovBond_10YearFrance.INT_GOV_BOND_10_YEAR_FRANCE;

    /**
     * The table <code>public.int_gov_bond_10_year_gb</code>.
     */
    public final IntGovBond_10YearGb INT_GOV_BOND_10_YEAR_GB = IntGovBond_10YearGb.INT_GOV_BOND_10_YEAR_GB;

    /**
     * The table <code>public.int_gov_bond_10_year_germany</code>.
     */
    public final IntGovBond_10YearGermany INT_GOV_BOND_10_YEAR_GERMANY = IntGovBond_10YearGermany.INT_GOV_BOND_10_YEAR_GERMANY;

    /**
     * The table <code>public.int_gov_bond_10_year_japan</code>.
     */
    public final IntGovBond_10YearJapan INT_GOV_BOND_10_YEAR_JAPAN = IntGovBond_10YearJapan.INT_GOV_BOND_10_YEAR_JAPAN;

    /**
     * The table <code>public.int_gov_bond_10_year_netherlands</code>.
     */
    public final IntGovBond_10YearNetherlands INT_GOV_BOND_10_YEAR_NETHERLANDS = IntGovBond_10YearNetherlands.INT_GOV_BOND_10_YEAR_NETHERLANDS;

    /**
     * The table <code>public.int_gov_bond_10_year_norway</code>.
     */
    public final IntGovBond_10YearNorway INT_GOV_BOND_10_YEAR_NORWAY = IntGovBond_10YearNorway.INT_GOV_BOND_10_YEAR_NORWAY;

    /**
     * The table <code>public.int_gov_bond_10_year_usa</code>.
     */
    public final IntGovBond_10YearUsa INT_GOV_BOND_10_YEAR_USA = IntGovBond_10YearUsa.INT_GOV_BOND_10_YEAR_USA;

    /**
     * The table <code>public.int_gov_bond_5_year_eur</code>.
     */
    public final IntGovBond_5YearEur INT_GOV_BOND_5_YEAR_EUR = IntGovBond_5YearEur.INT_GOV_BOND_5_YEAR_EUR;

    /**
     * The table <code>public.int_gov_bond_5_year_france</code>.
     */
    public final IntGovBond_5YearFrance INT_GOV_BOND_5_YEAR_FRANCE = IntGovBond_5YearFrance.INT_GOV_BOND_5_YEAR_FRANCE;

    /**
     * The table <code>public.int_gov_bond_5_year_gb</code>.
     */
    public final IntGovBond_5YearGb INT_GOV_BOND_5_YEAR_GB = IntGovBond_5YearGb.INT_GOV_BOND_5_YEAR_GB;

    /**
     * The table <code>public.int_gov_bond_5_year_germany</code>.
     */
    public final IntGovBond_5YearGermany INT_GOV_BOND_5_YEAR_GERMANY = IntGovBond_5YearGermany.INT_GOV_BOND_5_YEAR_GERMANY;

    /**
     * The table <code>public.int_gov_bond_5_year_japan</code>.
     */
    public final IntGovBond_5YearJapan INT_GOV_BOND_5_YEAR_JAPAN = IntGovBond_5YearJapan.INT_GOV_BOND_5_YEAR_JAPAN;

    /**
     * The table <code>public.int_gov_bond_5_year_netherlands</code>.
     */
    public final IntGovBond_5YearNetherlands INT_GOV_BOND_5_YEAR_NETHERLANDS = IntGovBond_5YearNetherlands.INT_GOV_BOND_5_YEAR_NETHERLANDS;

    /**
     * The table <code>public.int_gov_bond_5_year_usa</code>.
     */
    public final IntGovBond_5YearUsa INT_GOV_BOND_5_YEAR_USA = IntGovBond_5YearUsa.INT_GOV_BOND_5_YEAR_USA;

    /**
     * The table <code>public.policy_rate_sweden</code>.
     */
    public final PolicyRateSweden POLICY_RATE_SWEDEN = PolicyRateSweden.POLICY_RATE_SWEDEN;

    /**
     * The table <code>public.scrape_action_queue</code>.
     */
    public final ScrapeActionQueue SCRAPE_ACTION_QUEUE = ScrapeActionQueue.SCRAPE_ACTION_QUEUE;

    /**
     * The table <code>public.swedish_government_bill_12_month</code>.
     */
    public final SwedishGovernmentBill_12Month SWEDISH_GOVERNMENT_BILL_12_MONTH = SwedishGovernmentBill_12Month.SWEDISH_GOVERNMENT_BILL_12_MONTH;

    /**
     * The table <code>public.swedish_government_bill_1_month</code>.
     */
    public final SwedishGovernmentBill_1Month SWEDISH_GOVERNMENT_BILL_1_MONTH = SwedishGovernmentBill_1Month.SWEDISH_GOVERNMENT_BILL_1_MONTH;

    /**
     * The table <code>public.swedish_government_bill_3_month</code>.
     */
    public final SwedishGovernmentBill_3Month SWEDISH_GOVERNMENT_BILL_3_MONTH = SwedishGovernmentBill_3Month.SWEDISH_GOVERNMENT_BILL_3_MONTH;

    /**
     * The table <code>public.swedish_government_bill_6_month</code>.
     */
    public final SwedishGovernmentBill_6Month SWEDISH_GOVERNMENT_BILL_6_MONTH = SwedishGovernmentBill_6Month.SWEDISH_GOVERNMENT_BILL_6_MONTH;

    /**
     * The table <code>public.swedish_government_bonds_10_year</code>.
     */
    public final SwedishGovernmentBonds_10Year SWEDISH_GOVERNMENT_BONDS_10_YEAR = SwedishGovernmentBonds_10Year.SWEDISH_GOVERNMENT_BONDS_10_YEAR;

    /**
     * The table <code>public.swedish_government_bonds_2_year</code>.
     */
    public final SwedishGovernmentBonds_2Year SWEDISH_GOVERNMENT_BONDS_2_YEAR = SwedishGovernmentBonds_2Year.SWEDISH_GOVERNMENT_BONDS_2_YEAR;

    /**
     * The table <code>public.swedish_government_bonds_5_year</code>.
     */
    public final SwedishGovernmentBonds_5Year SWEDISH_GOVERNMENT_BONDS_5_YEAR = SwedishGovernmentBonds_5Year.SWEDISH_GOVERNMENT_BONDS_5_YEAR;

    /**
     * The table <code>public.swedish_government_bonds_7_year</code>.
     */
    public final SwedishGovernmentBonds_7Year SWEDISH_GOVERNMENT_BONDS_7_YEAR = SwedishGovernmentBonds_7Year.SWEDISH_GOVERNMENT_BONDS_7_YEAR;

    /**
     * No further instances allowed
     */
    private Public() {
        super("public", null);
    }


    @Override
    public Catalog getCatalog() {
        return DefaultCatalog.DEFAULT_CATALOG;
    }

    @Override
    public final List<Sequence<?>> getSequences() {
        return Arrays.asList(
            Sequences.EURO_MARKET_10_YEAR_DENMARK_ID_SEQ,
            Sequences.EURO_MARKET_10_YEAR_EUR_ID_SEQ,
            Sequences.EURO_MARKET_10_YEAR_FINLAND_ID_SEQ,
            Sequences.EURO_MARKET_10_YEAR_FRANCE_ID_SEQ,
            Sequences.EURO_MARKET_10_YEAR_GB_ID_SEQ,
            Sequences.EURO_MARKET_10_YEAR_GERMANY_ID_SEQ,
            Sequences.EURO_MARKET_10_YEAR_JAPAN_ID_SEQ,
            Sequences.EURO_MARKET_10_YEAR_NETHERLANDS_ID_SEQ,
            Sequences.EURO_MARKET_10_YEAR_NORWAY_ID_SEQ,
            Sequences.EURO_MARKET_10_YEAR_USA_ID_SEQ,
            Sequences.EURO_MARKET_5_YEAR_EUR_ID_SEQ,
            Sequences.EURO_MARKET_5_YEAR_FRANCE_ID_SEQ,
            Sequences.EURO_MARKET_5_YEAR_GB_ID_SEQ,
            Sequences.EURO_MARKET_5_YEAR_GERMANY_ID_SEQ,
            Sequences.EURO_MARKET_5_YEAR_JAPAN_ID_SEQ,
            Sequences.EURO_MARKET_5_YEAR_NETHERLANDS_ID_SEQ,
            Sequences.EURO_MARKET_5_YEAR_USA_ID_SEQ,
            Sequences.SWEDISH_GOVERNMENT_BONDS_10_MONTH_ID_SEQ,
            Sequences.SWEDISH_GOVERNMENT_BONDS_2_MONTH_ID_SEQ,
            Sequences.SWEDISH_GOVERNMENT_BONDS_5_MONTH_ID_SEQ,
            Sequences.SWEDISH_GOVERNMENT_BONDS_7_MONTH_ID_SEQ
        );
    }

    @Override
    public final List<Table<?>> getTables() {
        return Arrays.asList(
            EuroMarket_3MonthDenmark.EURO_MARKET_3_MONTH_DENMARK,
            EuroMarket_3MonthEur.EURO_MARKET_3_MONTH_EUR,
            EuroMarket_3MonthGb.EURO_MARKET_3_MONTH_GB,
            EuroMarket_3MonthJapan.EURO_MARKET_3_MONTH_JAPAN,
            EuroMarket_3MonthNorway.EURO_MARKET_3_MONTH_NORWAY,
            EuroMarket_3MonthUsa.EURO_MARKET_3_MONTH_USA,
            EuroMarket_6MonthDenmark.EURO_MARKET_6_MONTH_DENMARK,
            EuroMarket_6MonthEur.EURO_MARKET_6_MONTH_EUR,
            EuroMarket_6MonthGb.EURO_MARKET_6_MONTH_GB,
            EuroMarket_6MonthJapan.EURO_MARKET_6_MONTH_JAPAN,
            EuroMarket_6MonthNorway.EURO_MARKET_6_MONTH_NORWAY,
            EuroMarket_6MonthUsa.EURO_MARKET_6_MONTH_USA,
            ExchangeUsdSek.EXCHANGE_USD_SEK,
            FlywaySchemaHistory.FLYWAY_SCHEMA_HISTORY,
            IntGovBond_10YearDenmark.INT_GOV_BOND_10_YEAR_DENMARK,
            IntGovBond_10YearEur.INT_GOV_BOND_10_YEAR_EUR,
            IntGovBond_10YearFinland.INT_GOV_BOND_10_YEAR_FINLAND,
            IntGovBond_10YearFrance.INT_GOV_BOND_10_YEAR_FRANCE,
            IntGovBond_10YearGb.INT_GOV_BOND_10_YEAR_GB,
            IntGovBond_10YearGermany.INT_GOV_BOND_10_YEAR_GERMANY,
            IntGovBond_10YearJapan.INT_GOV_BOND_10_YEAR_JAPAN,
            IntGovBond_10YearNetherlands.INT_GOV_BOND_10_YEAR_NETHERLANDS,
            IntGovBond_10YearNorway.INT_GOV_BOND_10_YEAR_NORWAY,
            IntGovBond_10YearUsa.INT_GOV_BOND_10_YEAR_USA,
            IntGovBond_5YearEur.INT_GOV_BOND_5_YEAR_EUR,
            IntGovBond_5YearFrance.INT_GOV_BOND_5_YEAR_FRANCE,
            IntGovBond_5YearGb.INT_GOV_BOND_5_YEAR_GB,
            IntGovBond_5YearGermany.INT_GOV_BOND_5_YEAR_GERMANY,
            IntGovBond_5YearJapan.INT_GOV_BOND_5_YEAR_JAPAN,
            IntGovBond_5YearNetherlands.INT_GOV_BOND_5_YEAR_NETHERLANDS,
            IntGovBond_5YearUsa.INT_GOV_BOND_5_YEAR_USA,
            PolicyRateSweden.POLICY_RATE_SWEDEN,
            ScrapeActionQueue.SCRAPE_ACTION_QUEUE,
            SwedishGovernmentBill_12Month.SWEDISH_GOVERNMENT_BILL_12_MONTH,
            SwedishGovernmentBill_1Month.SWEDISH_GOVERNMENT_BILL_1_MONTH,
            SwedishGovernmentBill_3Month.SWEDISH_GOVERNMENT_BILL_3_MONTH,
            SwedishGovernmentBill_6Month.SWEDISH_GOVERNMENT_BILL_6_MONTH,
            SwedishGovernmentBonds_10Year.SWEDISH_GOVERNMENT_BONDS_10_YEAR,
            SwedishGovernmentBonds_2Year.SWEDISH_GOVERNMENT_BONDS_2_YEAR,
            SwedishGovernmentBonds_5Year.SWEDISH_GOVERNMENT_BONDS_5_YEAR,
            SwedishGovernmentBonds_7Year.SWEDISH_GOVERNMENT_BONDS_7_YEAR
        );
    }
}
