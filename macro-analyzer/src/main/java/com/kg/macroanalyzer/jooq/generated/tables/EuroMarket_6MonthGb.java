/*
 * This file is generated by jOOQ.
 */
package com.kg.macroanalyzer.jooq.generated.tables;


import com.kg.macroanalyzer.jooq.generated.Keys;
import com.kg.macroanalyzer.jooq.generated.Public;
import com.kg.macroanalyzer.jooq.generated.tables.records.EuroMarket_6MonthGbRecord;

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
public class EuroMarket_6MonthGb extends TableImpl<EuroMarket_6MonthGbRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>public.euro_market_6_month_gb</code>
     */
    public static final EuroMarket_6MonthGb EURO_MARKET_6_MONTH_GB = new EuroMarket_6MonthGb();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<EuroMarket_6MonthGbRecord> getRecordType() {
        return EuroMarket_6MonthGbRecord.class;
    }

    /**
     * The column <code>public.euro_market_6_month_gb.id</code>.
     */
    public final TableField<EuroMarket_6MonthGbRecord, Integer> ID = createField(DSL.name("id"), SQLDataType.INTEGER.nullable(false).identity(true), this, "");

    /**
     * The column <code>public.euro_market_6_month_gb.global_id</code>.
     */
    public final TableField<EuroMarket_6MonthGbRecord, UUID> GLOBAL_ID = createField(DSL.name("global_id"), SQLDataType.UUID.nullable(false), this, "");

    /**
     * The column <code>public.euro_market_6_month_gb.value</code>.
     */
    public final TableField<EuroMarket_6MonthGbRecord, Double> VALUE = createField(DSL.name("value"), SQLDataType.DOUBLE.nullable(false), this, "");

    /**
     * The column <code>public.euro_market_6_month_gb.date</code>.
     */
    public final TableField<EuroMarket_6MonthGbRecord, LocalDate> DATE = createField(DSL.name("date"), SQLDataType.LOCALDATE.nullable(false), this, "");

    /**
     * The column <code>public.euro_market_6_month_gb.created</code>.
     */
    public final TableField<EuroMarket_6MonthGbRecord, LocalDateTime> CREATED = createField(DSL.name("created"), SQLDataType.LOCALDATETIME(6).defaultValue(DSL.field(DSL.raw("now()"), SQLDataType.LOCALDATETIME)), this, "");

    /**
     * The column <code>public.euro_market_6_month_gb.updated</code>.
     */
    public final TableField<EuroMarket_6MonthGbRecord, LocalDateTime> UPDATED = createField(DSL.name("updated"), SQLDataType.LOCALDATETIME(6).defaultValue(DSL.field(DSL.raw("now()"), SQLDataType.LOCALDATETIME)), this, "");

    private EuroMarket_6MonthGb(Name alias, Table<EuroMarket_6MonthGbRecord> aliased) {
        this(alias, aliased, null);
    }

    private EuroMarket_6MonthGb(Name alias, Table<EuroMarket_6MonthGbRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table());
    }

    /**
     * Create an aliased <code>public.euro_market_6_month_gb</code> table
     * reference
     */
    public EuroMarket_6MonthGb(String alias) {
        this(DSL.name(alias), EURO_MARKET_6_MONTH_GB);
    }

    /**
     * Create an aliased <code>public.euro_market_6_month_gb</code> table
     * reference
     */
    public EuroMarket_6MonthGb(Name alias) {
        this(alias, EURO_MARKET_6_MONTH_GB);
    }

    /**
     * Create a <code>public.euro_market_6_month_gb</code> table reference
     */
    public EuroMarket_6MonthGb() {
        this(DSL.name("euro_market_6_month_gb"), null);
    }

    public <O extends Record> EuroMarket_6MonthGb(Table<O> child, ForeignKey<O, EuroMarket_6MonthGbRecord> key) {
        super(child, key, EURO_MARKET_6_MONTH_GB);
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : Public.PUBLIC;
    }

    @Override
    public Identity<EuroMarket_6MonthGbRecord, Integer> getIdentity() {
        return (Identity<EuroMarket_6MonthGbRecord, Integer>) super.getIdentity();
    }

    @Override
    public UniqueKey<EuroMarket_6MonthGbRecord> getPrimaryKey() {
        return Keys.EURO_MARKET_6_MONTH_GB_PKEY;
    }

    @Override
    public EuroMarket_6MonthGb as(String alias) {
        return new EuroMarket_6MonthGb(DSL.name(alias), this);
    }

    @Override
    public EuroMarket_6MonthGb as(Name alias) {
        return new EuroMarket_6MonthGb(alias, this);
    }

    @Override
    public EuroMarket_6MonthGb as(Table<?> alias) {
        return new EuroMarket_6MonthGb(alias.getQualifiedName(), this);
    }

    /**
     * Rename this table
     */
    @Override
    public EuroMarket_6MonthGb rename(String name) {
        return new EuroMarket_6MonthGb(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public EuroMarket_6MonthGb rename(Name name) {
        return new EuroMarket_6MonthGb(name, null);
    }

    /**
     * Rename this table
     */
    @Override
    public EuroMarket_6MonthGb rename(Table<?> name) {
        return new EuroMarket_6MonthGb(name.getQualifiedName(), null);
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
