/*
 * This file is generated by jOOQ.
 */
package com.kg.macroanalyzer.jooq.generated.tables;


import com.kg.macroanalyzer.jooq.generated.Keys;
import com.kg.macroanalyzer.jooq.generated.Public;
import com.kg.macroanalyzer.jooq.generated.tables.records.EuroMarket_6MonthEurRecord;

import java.time.LocalDate;
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
public class EuroMarket_6MonthEur extends TableImpl<EuroMarket_6MonthEurRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>public.euro_market_6_month_eur</code>
     */
    public static final EuroMarket_6MonthEur EURO_MARKET_6_MONTH_EUR = new EuroMarket_6MonthEur();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<EuroMarket_6MonthEurRecord> getRecordType() {
        return EuroMarket_6MonthEurRecord.class;
    }

    /**
     * The column <code>public.euro_market_6_month_eur.id</code>.
     */
    public final TableField<EuroMarket_6MonthEurRecord, Integer> ID = createField(DSL.name("id"), SQLDataType.INTEGER.nullable(false).identity(true), this, "");

    /**
     * The column <code>public.euro_market_6_month_eur.global_id</code>.
     */
    public final TableField<EuroMarket_6MonthEurRecord, UUID> GLOBAL_ID = createField(DSL.name("global_id"), SQLDataType.UUID.nullable(false), this, "");

    /**
     * The column <code>public.euro_market_6_month_eur.value</code>.
     */
    public final TableField<EuroMarket_6MonthEurRecord, Double> VALUE = createField(DSL.name("value"), SQLDataType.DOUBLE.nullable(false), this, "");

    /**
     * The column <code>public.euro_market_6_month_eur.date</code>.
     */
    public final TableField<EuroMarket_6MonthEurRecord, LocalDate> DATE = createField(DSL.name("date"), SQLDataType.LOCALDATE.nullable(false), this, "");

    /**
     * The column <code>public.euro_market_6_month_eur.created</code>.
     */
    public final TableField<EuroMarket_6MonthEurRecord, LocalDateTime> CREATED = createField(DSL.name("created"), SQLDataType.LOCALDATETIME(6).defaultValue(DSL.field(DSL.raw("now()"), SQLDataType.LOCALDATETIME)), this, "");

    /**
     * The column <code>public.euro_market_6_month_eur.updated</code>.
     */
    public final TableField<EuroMarket_6MonthEurRecord, LocalDateTime> UPDATED = createField(DSL.name("updated"), SQLDataType.LOCALDATETIME(6).defaultValue(DSL.field(DSL.raw("now()"), SQLDataType.LOCALDATETIME)), this, "");

    private EuroMarket_6MonthEur(Name alias, Table<EuroMarket_6MonthEurRecord> aliased) {
        this(alias, aliased, null);
    }

    private EuroMarket_6MonthEur(Name alias, Table<EuroMarket_6MonthEurRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table());
    }

    /**
     * Create an aliased <code>public.euro_market_6_month_eur</code> table
     * reference
     */
    public EuroMarket_6MonthEur(String alias) {
        this(DSL.name(alias), EURO_MARKET_6_MONTH_EUR);
    }

    /**
     * Create an aliased <code>public.euro_market_6_month_eur</code> table
     * reference
     */
    public EuroMarket_6MonthEur(Name alias) {
        this(alias, EURO_MARKET_6_MONTH_EUR);
    }

    /**
     * Create a <code>public.euro_market_6_month_eur</code> table reference
     */
    public EuroMarket_6MonthEur() {
        this(DSL.name("euro_market_6_month_eur"), null);
    }

    public <O extends Record> EuroMarket_6MonthEur(Table<O> child, ForeignKey<O, EuroMarket_6MonthEurRecord> key) {
        super(child, key, EURO_MARKET_6_MONTH_EUR);
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : Public.PUBLIC;
    }

    @Override
    public Identity<EuroMarket_6MonthEurRecord, Integer> getIdentity() {
        return (Identity<EuroMarket_6MonthEurRecord, Integer>) super.getIdentity();
    }

    @Override
    public UniqueKey<EuroMarket_6MonthEurRecord> getPrimaryKey() {
        return Keys.EURO_MARKET_6_MONTH_EUR_PKEY;
    }

    @Override
    public EuroMarket_6MonthEur as(String alias) {
        return new EuroMarket_6MonthEur(DSL.name(alias), this);
    }

    @Override
    public EuroMarket_6MonthEur as(Name alias) {
        return new EuroMarket_6MonthEur(alias, this);
    }

    @Override
    public EuroMarket_6MonthEur as(Table<?> alias) {
        return new EuroMarket_6MonthEur(alias.getQualifiedName(), this);
    }

    /**
     * Rename this table
     */
    @Override
    public EuroMarket_6MonthEur rename(String name) {
        return new EuroMarket_6MonthEur(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public EuroMarket_6MonthEur rename(Name name) {
        return new EuroMarket_6MonthEur(name, null);
    }

    /**
     * Rename this table
     */
    @Override
    public EuroMarket_6MonthEur rename(Table<?> name) {
        return new EuroMarket_6MonthEur(name.getQualifiedName(), null);
    }

    // -------------------------------------------------------------------------
    // Row6 type methods
    // -------------------------------------------------------------------------

    @Override
    public Row6<Integer, UUID, Double, LocalDate, LocalDateTime, LocalDateTime> fieldsRow() {
        return (Row6) super.fieldsRow();
    }

    /**
     * Convenience mapping calling {@link SelectField#convertFrom(Function)}.
     */
    public <U> SelectField<U> mapping(Function6<? super Integer, ? super UUID, ? super Double, ? super LocalDate, ? super LocalDateTime, ? super LocalDateTime, ? extends U> from) {
        return convertFrom(Records.mapping(from));
    }

    /**
     * Convenience mapping calling {@link SelectField#convertFrom(Class,
     * Function)}.
     */
    public <U> SelectField<U> mapping(Class<U> toType, Function6<? super Integer, ? super UUID, ? super Double, ? super LocalDate, ? super LocalDateTime, ? super LocalDateTime, ? extends U> from) {
        return convertFrom(toType, Records.mapping(from));
    }
}