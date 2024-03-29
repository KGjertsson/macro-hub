/*
 * This file is generated by jOOQ.
 */
package com.kg.macroanalyzer.jooq.generated.tables;


import com.kg.macroanalyzer.jooq.generated.Keys;
import com.kg.macroanalyzer.jooq.generated.Public;
import com.kg.macroanalyzer.jooq.generated.tables.records.EuroMarket_3MonthDenmarkRecord;

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
public class EuroMarket_3MonthDenmark extends TableImpl<EuroMarket_3MonthDenmarkRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>public.euro_market_3_month_denmark</code>
     */
    public static final EuroMarket_3MonthDenmark EURO_MARKET_3_MONTH_DENMARK = new EuroMarket_3MonthDenmark();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<EuroMarket_3MonthDenmarkRecord> getRecordType() {
        return EuroMarket_3MonthDenmarkRecord.class;
    }

    /**
     * The column <code>public.euro_market_3_month_denmark.id</code>.
     */
    public final TableField<EuroMarket_3MonthDenmarkRecord, Integer> ID = createField(DSL.name("id"), SQLDataType.INTEGER.nullable(false).identity(true), this, "");

    /**
     * The column <code>public.euro_market_3_month_denmark.global_id</code>.
     */
    public final TableField<EuroMarket_3MonthDenmarkRecord, UUID> GLOBAL_ID = createField(DSL.name("global_id"), SQLDataType.UUID.nullable(false), this, "");

    /**
     * The column <code>public.euro_market_3_month_denmark.value</code>.
     */
    public final TableField<EuroMarket_3MonthDenmarkRecord, Double> VALUE = createField(DSL.name("value"), SQLDataType.DOUBLE.nullable(false), this, "");

    /**
     * The column <code>public.euro_market_3_month_denmark.date</code>.
     */
    public final TableField<EuroMarket_3MonthDenmarkRecord, LocalDate> DATE = createField(DSL.name("date"), SQLDataType.LOCALDATE.nullable(false), this, "");

    /**
     * The column <code>public.euro_market_3_month_denmark.created</code>.
     */
    public final TableField<EuroMarket_3MonthDenmarkRecord, LocalDateTime> CREATED = createField(DSL.name("created"), SQLDataType.LOCALDATETIME(6).defaultValue(DSL.field(DSL.raw("now()"), SQLDataType.LOCALDATETIME)), this, "");

    /**
     * The column <code>public.euro_market_3_month_denmark.updated</code>.
     */
    public final TableField<EuroMarket_3MonthDenmarkRecord, LocalDateTime> UPDATED = createField(DSL.name("updated"), SQLDataType.LOCALDATETIME(6).defaultValue(DSL.field(DSL.raw("now()"), SQLDataType.LOCALDATETIME)), this, "");

    private EuroMarket_3MonthDenmark(Name alias, Table<EuroMarket_3MonthDenmarkRecord> aliased) {
        this(alias, aliased, null);
    }

    private EuroMarket_3MonthDenmark(Name alias, Table<EuroMarket_3MonthDenmarkRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table());
    }

    /**
     * Create an aliased <code>public.euro_market_3_month_denmark</code> table
     * reference
     */
    public EuroMarket_3MonthDenmark(String alias) {
        this(DSL.name(alias), EURO_MARKET_3_MONTH_DENMARK);
    }

    /**
     * Create an aliased <code>public.euro_market_3_month_denmark</code> table
     * reference
     */
    public EuroMarket_3MonthDenmark(Name alias) {
        this(alias, EURO_MARKET_3_MONTH_DENMARK);
    }

    /**
     * Create a <code>public.euro_market_3_month_denmark</code> table reference
     */
    public EuroMarket_3MonthDenmark() {
        this(DSL.name("euro_market_3_month_denmark"), null);
    }

    public <O extends Record> EuroMarket_3MonthDenmark(Table<O> child, ForeignKey<O, EuroMarket_3MonthDenmarkRecord> key) {
        super(child, key, EURO_MARKET_3_MONTH_DENMARK);
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : Public.PUBLIC;
    }

    @Override
    public Identity<EuroMarket_3MonthDenmarkRecord, Integer> getIdentity() {
        return (Identity<EuroMarket_3MonthDenmarkRecord, Integer>) super.getIdentity();
    }

    @Override
    public UniqueKey<EuroMarket_3MonthDenmarkRecord> getPrimaryKey() {
        return Keys.EURO_MARKET_3_MONTH_DENMARK_PKEY;
    }

    @Override
    public EuroMarket_3MonthDenmark as(String alias) {
        return new EuroMarket_3MonthDenmark(DSL.name(alias), this);
    }

    @Override
    public EuroMarket_3MonthDenmark as(Name alias) {
        return new EuroMarket_3MonthDenmark(alias, this);
    }

    @Override
    public EuroMarket_3MonthDenmark as(Table<?> alias) {
        return new EuroMarket_3MonthDenmark(alias.getQualifiedName(), this);
    }

    /**
     * Rename this table
     */
    @Override
    public EuroMarket_3MonthDenmark rename(String name) {
        return new EuroMarket_3MonthDenmark(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public EuroMarket_3MonthDenmark rename(Name name) {
        return new EuroMarket_3MonthDenmark(name, null);
    }

    /**
     * Rename this table
     */
    @Override
    public EuroMarket_3MonthDenmark rename(Table<?> name) {
        return new EuroMarket_3MonthDenmark(name.getQualifiedName(), null);
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
