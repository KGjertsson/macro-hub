/*
 * This file is generated by jOOQ.
 */
package com.kg.macroanalyzer.jooq.generated.tables;


import com.kg.macroanalyzer.jooq.generated.Keys;
import com.kg.macroanalyzer.jooq.generated.Public;
import com.kg.macroanalyzer.jooq.generated.tables.records.EuroMarket_10YearGermanyRecord;

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
public class EuroMarket_10YearGermany extends TableImpl<EuroMarket_10YearGermanyRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>public.euro_market_10_year_germany</code>
     */
    public static final EuroMarket_10YearGermany EURO_MARKET_10_YEAR_GERMANY = new EuroMarket_10YearGermany();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<EuroMarket_10YearGermanyRecord> getRecordType() {
        return EuroMarket_10YearGermanyRecord.class;
    }

    /**
     * The column <code>public.euro_market_10_year_germany.id</code>.
     */
    public final TableField<EuroMarket_10YearGermanyRecord, Integer> ID = createField(DSL.name("id"), SQLDataType.INTEGER.nullable(false).identity(true), this, "");

    /**
     * The column <code>public.euro_market_10_year_germany.global_id</code>.
     */
    public final TableField<EuroMarket_10YearGermanyRecord, UUID> GLOBAL_ID = createField(DSL.name("global_id"), SQLDataType.UUID.nullable(false), this, "");

    /**
     * The column <code>public.euro_market_10_year_germany.value</code>.
     */
    public final TableField<EuroMarket_10YearGermanyRecord, Double> VALUE = createField(DSL.name("value"), SQLDataType.DOUBLE.nullable(false), this, "");

    /**
     * The column <code>public.euro_market_10_year_germany.date</code>.
     */
    public final TableField<EuroMarket_10YearGermanyRecord, LocalDate> DATE = createField(DSL.name("date"), SQLDataType.LOCALDATE.nullable(false), this, "");

    /**
     * The column <code>public.euro_market_10_year_germany.created</code>.
     */
    public final TableField<EuroMarket_10YearGermanyRecord, LocalDateTime> CREATED = createField(DSL.name("created"), SQLDataType.LOCALDATETIME(6).defaultValue(DSL.field(DSL.raw("now()"), SQLDataType.LOCALDATETIME)), this, "");

    /**
     * The column <code>public.euro_market_10_year_germany.updated</code>.
     */
    public final TableField<EuroMarket_10YearGermanyRecord, LocalDateTime> UPDATED = createField(DSL.name("updated"), SQLDataType.LOCALDATETIME(6).defaultValue(DSL.field(DSL.raw("now()"), SQLDataType.LOCALDATETIME)), this, "");

    private EuroMarket_10YearGermany(Name alias, Table<EuroMarket_10YearGermanyRecord> aliased) {
        this(alias, aliased, null);
    }

    private EuroMarket_10YearGermany(Name alias, Table<EuroMarket_10YearGermanyRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table());
    }

    /**
     * Create an aliased <code>public.euro_market_10_year_germany</code> table
     * reference
     */
    public EuroMarket_10YearGermany(String alias) {
        this(DSL.name(alias), EURO_MARKET_10_YEAR_GERMANY);
    }

    /**
     * Create an aliased <code>public.euro_market_10_year_germany</code> table
     * reference
     */
    public EuroMarket_10YearGermany(Name alias) {
        this(alias, EURO_MARKET_10_YEAR_GERMANY);
    }

    /**
     * Create a <code>public.euro_market_10_year_germany</code> table reference
     */
    public EuroMarket_10YearGermany() {
        this(DSL.name("euro_market_10_year_germany"), null);
    }

    public <O extends Record> EuroMarket_10YearGermany(Table<O> child, ForeignKey<O, EuroMarket_10YearGermanyRecord> key) {
        super(child, key, EURO_MARKET_10_YEAR_GERMANY);
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : Public.PUBLIC;
    }

    @Override
    public Identity<EuroMarket_10YearGermanyRecord, Integer> getIdentity() {
        return (Identity<EuroMarket_10YearGermanyRecord, Integer>) super.getIdentity();
    }

    @Override
    public UniqueKey<EuroMarket_10YearGermanyRecord> getPrimaryKey() {
        return Keys.EURO_MARKET_10_YEAR_GERMANY_PKEY;
    }

    @Override
    public EuroMarket_10YearGermany as(String alias) {
        return new EuroMarket_10YearGermany(DSL.name(alias), this);
    }

    @Override
    public EuroMarket_10YearGermany as(Name alias) {
        return new EuroMarket_10YearGermany(alias, this);
    }

    @Override
    public EuroMarket_10YearGermany as(Table<?> alias) {
        return new EuroMarket_10YearGermany(alias.getQualifiedName(), this);
    }

    /**
     * Rename this table
     */
    @Override
    public EuroMarket_10YearGermany rename(String name) {
        return new EuroMarket_10YearGermany(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public EuroMarket_10YearGermany rename(Name name) {
        return new EuroMarket_10YearGermany(name, null);
    }

    /**
     * Rename this table
     */
    @Override
    public EuroMarket_10YearGermany rename(Table<?> name) {
        return new EuroMarket_10YearGermany(name.getQualifiedName(), null);
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
