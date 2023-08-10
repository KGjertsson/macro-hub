/*
 * This file is generated by jOOQ.
 */
package com.kg.macroanalyzer.jooq.generated.tables;


import com.kg.macroanalyzer.jooq.generated.Keys;
import com.kg.macroanalyzer.jooq.generated.Public;
import com.kg.macroanalyzer.jooq.generated.tables.records.EuroMarket_10YearEurRecord;

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
public class EuroMarket_10YearEur extends TableImpl<EuroMarket_10YearEurRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>public.euro_market_10_year_eur</code>
     */
    public static final EuroMarket_10YearEur EURO_MARKET_10_YEAR_EUR = new EuroMarket_10YearEur();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<EuroMarket_10YearEurRecord> getRecordType() {
        return EuroMarket_10YearEurRecord.class;
    }

    /**
     * The column <code>public.euro_market_10_year_eur.id</code>.
     */
    public final TableField<EuroMarket_10YearEurRecord, Integer> ID = createField(DSL.name("id"), SQLDataType.INTEGER.nullable(false).identity(true), this, "");

    /**
     * The column <code>public.euro_market_10_year_eur.global_id</code>.
     */
    public final TableField<EuroMarket_10YearEurRecord, UUID> GLOBAL_ID = createField(DSL.name("global_id"), SQLDataType.UUID.nullable(false), this, "");

    /**
     * The column <code>public.euro_market_10_year_eur.value</code>.
     */
    public final TableField<EuroMarket_10YearEurRecord, Double> VALUE = createField(DSL.name("value"), SQLDataType.DOUBLE.nullable(false), this, "");

    /**
     * The column <code>public.euro_market_10_year_eur.date</code>.
     */
    public final TableField<EuroMarket_10YearEurRecord, LocalDate> DATE = createField(DSL.name("date"), SQLDataType.LOCALDATE.nullable(false), this, "");

    /**
     * The column <code>public.euro_market_10_year_eur.created</code>.
     */
    public final TableField<EuroMarket_10YearEurRecord, LocalDateTime> CREATED = createField(DSL.name("created"), SQLDataType.LOCALDATETIME(6).defaultValue(DSL.field(DSL.raw("now()"), SQLDataType.LOCALDATETIME)), this, "");

    /**
     * The column <code>public.euro_market_10_year_eur.updated</code>.
     */
    public final TableField<EuroMarket_10YearEurRecord, LocalDateTime> UPDATED = createField(DSL.name("updated"), SQLDataType.LOCALDATETIME(6).defaultValue(DSL.field(DSL.raw("now()"), SQLDataType.LOCALDATETIME)), this, "");

    private EuroMarket_10YearEur(Name alias, Table<EuroMarket_10YearEurRecord> aliased) {
        this(alias, aliased, null);
    }

    private EuroMarket_10YearEur(Name alias, Table<EuroMarket_10YearEurRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table());
    }

    /**
     * Create an aliased <code>public.euro_market_10_year_eur</code> table
     * reference
     */
    public EuroMarket_10YearEur(String alias) {
        this(DSL.name(alias), EURO_MARKET_10_YEAR_EUR);
    }

    /**
     * Create an aliased <code>public.euro_market_10_year_eur</code> table
     * reference
     */
    public EuroMarket_10YearEur(Name alias) {
        this(alias, EURO_MARKET_10_YEAR_EUR);
    }

    /**
     * Create a <code>public.euro_market_10_year_eur</code> table reference
     */
    public EuroMarket_10YearEur() {
        this(DSL.name("euro_market_10_year_eur"), null);
    }

    public <O extends Record> EuroMarket_10YearEur(Table<O> child, ForeignKey<O, EuroMarket_10YearEurRecord> key) {
        super(child, key, EURO_MARKET_10_YEAR_EUR);
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : Public.PUBLIC;
    }

    @Override
    public Identity<EuroMarket_10YearEurRecord, Integer> getIdentity() {
        return (Identity<EuroMarket_10YearEurRecord, Integer>) super.getIdentity();
    }

    @Override
    public UniqueKey<EuroMarket_10YearEurRecord> getPrimaryKey() {
        return Keys.EURO_MARKET_10_YEAR_EUR_PKEY;
    }

    @Override
    public EuroMarket_10YearEur as(String alias) {
        return new EuroMarket_10YearEur(DSL.name(alias), this);
    }

    @Override
    public EuroMarket_10YearEur as(Name alias) {
        return new EuroMarket_10YearEur(alias, this);
    }

    @Override
    public EuroMarket_10YearEur as(Table<?> alias) {
        return new EuroMarket_10YearEur(alias.getQualifiedName(), this);
    }

    /**
     * Rename this table
     */
    @Override
    public EuroMarket_10YearEur rename(String name) {
        return new EuroMarket_10YearEur(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public EuroMarket_10YearEur rename(Name name) {
        return new EuroMarket_10YearEur(name, null);
    }

    /**
     * Rename this table
     */
    @Override
    public EuroMarket_10YearEur rename(Table<?> name) {
        return new EuroMarket_10YearEur(name.getQualifiedName(), null);
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
