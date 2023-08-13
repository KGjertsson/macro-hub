/*
 * This file is generated by jOOQ.
 */
package com.kg.macroanalyzer.jooq.generated.tables;


import com.kg.macroanalyzer.jooq.generated.Keys;
import com.kg.macroanalyzer.jooq.generated.Public;
import com.kg.macroanalyzer.jooq.generated.tables.records.EuroMarket_10YearDenmarkRecord;

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
public class EuroMarket_10YearDenmark extends TableImpl<EuroMarket_10YearDenmarkRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>public.euro_market_10_year_denmark</code>
     */
    public static final EuroMarket_10YearDenmark EURO_MARKET_10_YEAR_DENMARK = new EuroMarket_10YearDenmark();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<EuroMarket_10YearDenmarkRecord> getRecordType() {
        return EuroMarket_10YearDenmarkRecord.class;
    }

    /**
     * The column <code>public.euro_market_10_year_denmark.id</code>.
     */
    public final TableField<EuroMarket_10YearDenmarkRecord, Integer> ID = createField(DSL.name("id"), SQLDataType.INTEGER.nullable(false).identity(true), this, "");

    /**
     * The column <code>public.euro_market_10_year_denmark.global_id</code>.
     */
    public final TableField<EuroMarket_10YearDenmarkRecord, UUID> GLOBAL_ID = createField(DSL.name("global_id"), SQLDataType.UUID.nullable(false), this, "");

    /**
     * The column <code>public.euro_market_10_year_denmark.value</code>.
     */
    public final TableField<EuroMarket_10YearDenmarkRecord, Double> VALUE = createField(DSL.name("value"), SQLDataType.DOUBLE.nullable(false), this, "");

    /**
     * The column <code>public.euro_market_10_year_denmark.date</code>.
     */
    public final TableField<EuroMarket_10YearDenmarkRecord, LocalDate> DATE = createField(DSL.name("date"), SQLDataType.LOCALDATE.nullable(false), this, "");

    /**
     * The column <code>public.euro_market_10_year_denmark.created</code>.
     */
    public final TableField<EuroMarket_10YearDenmarkRecord, LocalDateTime> CREATED = createField(DSL.name("created"), SQLDataType.LOCALDATETIME(6).defaultValue(DSL.field(DSL.raw("now()"), SQLDataType.LOCALDATETIME)), this, "");

    /**
     * The column <code>public.euro_market_10_year_denmark.updated</code>.
     */
    public final TableField<EuroMarket_10YearDenmarkRecord, LocalDateTime> UPDATED = createField(DSL.name("updated"), SQLDataType.LOCALDATETIME(6).defaultValue(DSL.field(DSL.raw("now()"), SQLDataType.LOCALDATETIME)), this, "");

    private EuroMarket_10YearDenmark(Name alias, Table<EuroMarket_10YearDenmarkRecord> aliased) {
        this(alias, aliased, null);
    }

    private EuroMarket_10YearDenmark(Name alias, Table<EuroMarket_10YearDenmarkRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table());
    }

    /**
     * Create an aliased <code>public.euro_market_10_year_denmark</code> table
     * reference
     */
    public EuroMarket_10YearDenmark(String alias) {
        this(DSL.name(alias), EURO_MARKET_10_YEAR_DENMARK);
    }

    /**
     * Create an aliased <code>public.euro_market_10_year_denmark</code> table
     * reference
     */
    public EuroMarket_10YearDenmark(Name alias) {
        this(alias, EURO_MARKET_10_YEAR_DENMARK);
    }

    /**
     * Create a <code>public.euro_market_10_year_denmark</code> table reference
     */
    public EuroMarket_10YearDenmark() {
        this(DSL.name("euro_market_10_year_denmark"), null);
    }

    public <O extends Record> EuroMarket_10YearDenmark(Table<O> child, ForeignKey<O, EuroMarket_10YearDenmarkRecord> key) {
        super(child, key, EURO_MARKET_10_YEAR_DENMARK);
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : Public.PUBLIC;
    }

    @Override
    public Identity<EuroMarket_10YearDenmarkRecord, Integer> getIdentity() {
        return (Identity<EuroMarket_10YearDenmarkRecord, Integer>) super.getIdentity();
    }

    @Override
    public UniqueKey<EuroMarket_10YearDenmarkRecord> getPrimaryKey() {
        return Keys.EURO_MARKET_10_YEAR_DENMARK_PKEY;
    }

    @Override
    public EuroMarket_10YearDenmark as(String alias) {
        return new EuroMarket_10YearDenmark(DSL.name(alias), this);
    }

    @Override
    public EuroMarket_10YearDenmark as(Name alias) {
        return new EuroMarket_10YearDenmark(alias, this);
    }

    @Override
    public EuroMarket_10YearDenmark as(Table<?> alias) {
        return new EuroMarket_10YearDenmark(alias.getQualifiedName(), this);
    }

    /**
     * Rename this table
     */
    @Override
    public EuroMarket_10YearDenmark rename(String name) {
        return new EuroMarket_10YearDenmark(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public EuroMarket_10YearDenmark rename(Name name) {
        return new EuroMarket_10YearDenmark(name, null);
    }

    /**
     * Rename this table
     */
    @Override
    public EuroMarket_10YearDenmark rename(Table<?> name) {
        return new EuroMarket_10YearDenmark(name.getQualifiedName(), null);
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