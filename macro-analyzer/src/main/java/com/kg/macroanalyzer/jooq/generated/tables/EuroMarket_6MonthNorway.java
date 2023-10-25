/*
 * This file is generated by jOOQ.
 */
package com.kg.macroanalyzer.jooq.generated.tables;


import com.kg.macroanalyzer.jooq.generated.Keys;
import com.kg.macroanalyzer.jooq.generated.Public;
import com.kg.macroanalyzer.jooq.generated.tables.records.EuroMarket_6MonthNorwayRecord;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.function.Function;

import javax.annotation.processing.Generated;

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
@Generated(
    value = {
        "https://www.jooq.org",
        "jOOQ version:3.18.5",
        "catalog version:com.kg.macroanalyzer.configuration.FlywayCatalogVersion",
        "schema version:com.kg.macroanalyzer.configuration.FlywayCatalogVersion"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class EuroMarket_6MonthNorway extends TableImpl<EuroMarket_6MonthNorwayRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>public.euro_market_6_month_norway</code>
     */
    public static final EuroMarket_6MonthNorway EURO_MARKET_6_MONTH_NORWAY = new EuroMarket_6MonthNorway();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<EuroMarket_6MonthNorwayRecord> getRecordType() {
        return EuroMarket_6MonthNorwayRecord.class;
    }

    /**
     * The column <code>public.euro_market_6_month_norway.id</code>.
     */
    public final TableField<EuroMarket_6MonthNorwayRecord, Integer> ID = createField(DSL.name("id"), SQLDataType.INTEGER.nullable(false).identity(true), this, "");

    /**
     * The column <code>public.euro_market_6_month_norway.global_id</code>.
     */
    public final TableField<EuroMarket_6MonthNorwayRecord, UUID> GLOBAL_ID = createField(DSL.name("global_id"), SQLDataType.UUID.nullable(false), this, "");

    /**
     * The column <code>public.euro_market_6_month_norway.value</code>.
     */
    public final TableField<EuroMarket_6MonthNorwayRecord, Double> VALUE = createField(DSL.name("value"), SQLDataType.DOUBLE.nullable(false), this, "");

    /**
     * The column <code>public.euro_market_6_month_norway.date</code>.
     */
    public final TableField<EuroMarket_6MonthNorwayRecord, LocalDate> DATE = createField(DSL.name("date"), SQLDataType.LOCALDATE.nullable(false), this, "");

    /**
     * The column <code>public.euro_market_6_month_norway.created</code>.
     */
    public final TableField<EuroMarket_6MonthNorwayRecord, LocalDateTime> CREATED = createField(DSL.name("created"), SQLDataType.LOCALDATETIME(6).defaultValue(DSL.field(DSL.raw("now()"), SQLDataType.LOCALDATETIME)), this, "");

    /**
     * The column <code>public.euro_market_6_month_norway.updated</code>.
     */
    public final TableField<EuroMarket_6MonthNorwayRecord, LocalDateTime> UPDATED = createField(DSL.name("updated"), SQLDataType.LOCALDATETIME(6).defaultValue(DSL.field(DSL.raw("now()"), SQLDataType.LOCALDATETIME)), this, "");

    private EuroMarket_6MonthNorway(Name alias, Table<EuroMarket_6MonthNorwayRecord> aliased) {
        this(alias, aliased, null);
    }

    private EuroMarket_6MonthNorway(Name alias, Table<EuroMarket_6MonthNorwayRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table());
    }

    /**
     * Create an aliased <code>public.euro_market_6_month_norway</code> table
     * reference
     */
    public EuroMarket_6MonthNorway(String alias) {
        this(DSL.name(alias), EURO_MARKET_6_MONTH_NORWAY);
    }

    /**
     * Create an aliased <code>public.euro_market_6_month_norway</code> table
     * reference
     */
    public EuroMarket_6MonthNorway(Name alias) {
        this(alias, EURO_MARKET_6_MONTH_NORWAY);
    }

    /**
     * Create a <code>public.euro_market_6_month_norway</code> table reference
     */
    public EuroMarket_6MonthNorway() {
        this(DSL.name("euro_market_6_month_norway"), null);
    }

    public <O extends Record> EuroMarket_6MonthNorway(Table<O> child, ForeignKey<O, EuroMarket_6MonthNorwayRecord> key) {
        super(child, key, EURO_MARKET_6_MONTH_NORWAY);
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : Public.PUBLIC;
    }

    @Override
    public Identity<EuroMarket_6MonthNorwayRecord, Integer> getIdentity() {
        return (Identity<EuroMarket_6MonthNorwayRecord, Integer>) super.getIdentity();
    }

    @Override
    public UniqueKey<EuroMarket_6MonthNorwayRecord> getPrimaryKey() {
        return Keys.EURO_MARKET_6_MONTH_NORWAY_PKEY;
    }

    @Override
    public EuroMarket_6MonthNorway as(String alias) {
        return new EuroMarket_6MonthNorway(DSL.name(alias), this);
    }

    @Override
    public EuroMarket_6MonthNorway as(Name alias) {
        return new EuroMarket_6MonthNorway(alias, this);
    }

    @Override
    public EuroMarket_6MonthNorway as(Table<?> alias) {
        return new EuroMarket_6MonthNorway(alias.getQualifiedName(), this);
    }

    /**
     * Rename this table
     */
    @Override
    public EuroMarket_6MonthNorway rename(String name) {
        return new EuroMarket_6MonthNorway(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public EuroMarket_6MonthNorway rename(Name name) {
        return new EuroMarket_6MonthNorway(name, null);
    }

    /**
     * Rename this table
     */
    @Override
    public EuroMarket_6MonthNorway rename(Table<?> name) {
        return new EuroMarket_6MonthNorway(name.getQualifiedName(), null);
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
