/*
 * This file is generated by jOOQ.
 */
package com.kg.macroanalyzer.jooq.generated.tables;


import com.kg.macroanalyzer.jooq.generated.Keys;
import com.kg.macroanalyzer.jooq.generated.Public;
import com.kg.macroanalyzer.jooq.generated.tables.records.ExchangeUsdSekRecord;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.function.Function;

import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Function6;
import org.jooq.Identity;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Records;
import org.jooq.Row6;
import org.jooq.Schema;
import org.jooq.SelectField;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.TableOptions;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class ExchangeUsdSek extends TableImpl<ExchangeUsdSekRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>public.exchange_usd_sek</code>
     */
    public static final ExchangeUsdSek EXCHANGE_USD_SEK = new ExchangeUsdSek();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<ExchangeUsdSekRecord> getRecordType() {
        return ExchangeUsdSekRecord.class;
    }

    /**
     * The column <code>public.exchange_usd_sek.id</code>.
     */
    public final TableField<ExchangeUsdSekRecord, Integer> ID = createField(DSL.name("id"), SQLDataType.INTEGER.nullable(false).identity(true), this, "");

    /**
     * The column <code>public.exchange_usd_sek.global_id</code>.
     */
    public final TableField<ExchangeUsdSekRecord, UUID> GLOBAL_ID = createField(DSL.name("global_id"), SQLDataType.UUID.nullable(false), this, "");

    /**
     * The column <code>public.exchange_usd_sek.usd_sek</code>.
     */
    public final TableField<ExchangeUsdSekRecord, Double> USD_SEK = createField(DSL.name("usd_sek"), SQLDataType.DOUBLE.nullable(false), this, "");

    /**
     * The column <code>public.exchange_usd_sek.usd_sek_date</code>.
     */
    public final TableField<ExchangeUsdSekRecord, LocalDateTime> USD_SEK_DATE = createField(DSL.name("usd_sek_date"), SQLDataType.LOCALDATETIME(6).nullable(false), this, "");

    /**
     * The column <code>public.exchange_usd_sek.created</code>.
     */
    public final TableField<ExchangeUsdSekRecord, LocalDateTime> CREATED = createField(DSL.name("created"), SQLDataType.LOCALDATETIME(6).defaultValue(DSL.field(DSL.raw("now()"), SQLDataType.LOCALDATETIME)), this, "");

    /**
     * The column <code>public.exchange_usd_sek.updated</code>.
     */
    public final TableField<ExchangeUsdSekRecord, LocalDateTime> UPDATED = createField(DSL.name("updated"), SQLDataType.LOCALDATETIME(6).defaultValue(DSL.field(DSL.raw("now()"), SQLDataType.LOCALDATETIME)), this, "");

    private ExchangeUsdSek(Name alias, Table<ExchangeUsdSekRecord> aliased) {
        this(alias, aliased, null);
    }

    private ExchangeUsdSek(Name alias, Table<ExchangeUsdSekRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table());
    }

    /**
     * Create an aliased <code>public.exchange_usd_sek</code> table reference
     */
    public ExchangeUsdSek(String alias) {
        this(DSL.name(alias), EXCHANGE_USD_SEK);
    }

    /**
     * Create an aliased <code>public.exchange_usd_sek</code> table reference
     */
    public ExchangeUsdSek(Name alias) {
        this(alias, EXCHANGE_USD_SEK);
    }

    /**
     * Create a <code>public.exchange_usd_sek</code> table reference
     */
    public ExchangeUsdSek() {
        this(DSL.name("exchange_usd_sek"), null);
    }

    public <O extends Record> ExchangeUsdSek(Table<O> child, ForeignKey<O, ExchangeUsdSekRecord> key) {
        super(child, key, EXCHANGE_USD_SEK);
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : Public.PUBLIC;
    }

    @Override
    public Identity<ExchangeUsdSekRecord, Integer> getIdentity() {
        return (Identity<ExchangeUsdSekRecord, Integer>) super.getIdentity();
    }

    @Override
    public UniqueKey<ExchangeUsdSekRecord> getPrimaryKey() {
        return Keys.EXCHANGE_USD_SEK_PKEY;
    }

    @Override
    public ExchangeUsdSek as(String alias) {
        return new ExchangeUsdSek(DSL.name(alias), this);
    }

    @Override
    public ExchangeUsdSek as(Name alias) {
        return new ExchangeUsdSek(alias, this);
    }

    @Override
    public ExchangeUsdSek as(Table<?> alias) {
        return new ExchangeUsdSek(alias.getQualifiedName(), this);
    }

    /**
     * Rename this table
     */
    @Override
    public ExchangeUsdSek rename(String name) {
        return new ExchangeUsdSek(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public ExchangeUsdSek rename(Name name) {
        return new ExchangeUsdSek(name, null);
    }

    /**
     * Rename this table
     */
    @Override
    public ExchangeUsdSek rename(Table<?> name) {
        return new ExchangeUsdSek(name.getQualifiedName(), null);
    }

    // -------------------------------------------------------------------------
    // Row6 type methods
    // -------------------------------------------------------------------------

    @Override
    public Row6<Integer, UUID, Double, LocalDateTime, LocalDateTime, LocalDateTime> fieldsRow() {
        return (Row6) super.fieldsRow();
    }

    /**
     * Convenience mapping calling {@link SelectField#convertFrom(Function)}.
     */
    public <U> SelectField<U> mapping(Function6<? super Integer, ? super UUID, ? super Double, ? super LocalDateTime, ? super LocalDateTime, ? super LocalDateTime, ? extends U> from) {
        return convertFrom(Records.mapping(from));
    }

    /**
     * Convenience mapping calling {@link SelectField#convertFrom(Class,
     * Function)}.
     */
    public <U> SelectField<U> mapping(Class<U> toType, Function6<? super Integer, ? super UUID, ? super Double, ? super LocalDateTime, ? super LocalDateTime, ? super LocalDateTime, ? extends U> from) {
        return convertFrom(toType, Records.mapping(from));
    }
}
