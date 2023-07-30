/*
 * This file is generated by jOOQ.
 */
package com.kg.macroanalyzer.jooq.generated;


import com.kg.macroanalyzer.jooq.generated.tables.ExchangeUsdSek;
import com.kg.macroanalyzer.jooq.generated.tables.FlywaySchemaHistory;
import com.kg.macroanalyzer.jooq.generated.tables.PolicyRateSweden;
import com.kg.macroanalyzer.jooq.generated.tables.records.ExchangeUsdSekRecord;
import com.kg.macroanalyzer.jooq.generated.tables.records.FlywaySchemaHistoryRecord;
import com.kg.macroanalyzer.jooq.generated.tables.records.PolicyRateSwedenRecord;

import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.Internal;


/**
 * A class modelling foreign key relationships and constraints of tables in
 * public.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Keys {

    // -------------------------------------------------------------------------
    // UNIQUE and PRIMARY KEY definitions
    // -------------------------------------------------------------------------

    public static final UniqueKey<ExchangeUsdSekRecord> EXCHANGE_USD_SEK_PKEY = Internal.createUniqueKey(ExchangeUsdSek.EXCHANGE_USD_SEK, DSL.name("exchange_usd_sek_pkey"), new TableField[] { ExchangeUsdSek.EXCHANGE_USD_SEK.ID }, true);
    public static final UniqueKey<FlywaySchemaHistoryRecord> FLYWAY_SCHEMA_HISTORY_PK = Internal.createUniqueKey(FlywaySchemaHistory.FLYWAY_SCHEMA_HISTORY, DSL.name("flyway_schema_history_pk"), new TableField[] { FlywaySchemaHistory.FLYWAY_SCHEMA_HISTORY.INSTALLED_RANK }, true);
    public static final UniqueKey<PolicyRateSwedenRecord> POLICY_RATE_SWEDEN_PKEY = Internal.createUniqueKey(PolicyRateSweden.POLICY_RATE_SWEDEN, DSL.name("policy_rate_sweden_pkey"), new TableField[] { PolicyRateSweden.POLICY_RATE_SWEDEN.ID }, true);
}
