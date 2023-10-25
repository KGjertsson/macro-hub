/*
 * This file is generated by jOOQ.
 */
package com.kg.macroanalyzer.jooq.generated.tables;


import com.kg.macroanalyzer.jooq.generated.Keys;
import com.kg.macroanalyzer.jooq.generated.Public;
import com.kg.macroanalyzer.jooq.generated.tables.records.IntGovBond_5YearJapanRecord;

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
public class IntGovBond_5YearJapan extends TableImpl<IntGovBond_5YearJapanRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>public.int_gov_bond_5_year_japan</code>
     */
    public static final IntGovBond_5YearJapan INT_GOV_BOND_5_YEAR_JAPAN = new IntGovBond_5YearJapan();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<IntGovBond_5YearJapanRecord> getRecordType() {
        return IntGovBond_5YearJapanRecord.class;
    }

    /**
     * The column <code>public.int_gov_bond_5_year_japan.id</code>.
     */
    public final TableField<IntGovBond_5YearJapanRecord, Integer> ID = createField(DSL.name("id"), SQLDataType.INTEGER.nullable(false).identity(true), this, "");

    /**
     * The column <code>public.int_gov_bond_5_year_japan.global_id</code>.
     */
    public final TableField<IntGovBond_5YearJapanRecord, UUID> GLOBAL_ID = createField(DSL.name("global_id"), SQLDataType.UUID.nullable(false), this, "");

    /**
     * The column <code>public.int_gov_bond_5_year_japan.value</code>.
     */
    public final TableField<IntGovBond_5YearJapanRecord, Double> VALUE = createField(DSL.name("value"), SQLDataType.DOUBLE.nullable(false), this, "");

    /**
     * The column <code>public.int_gov_bond_5_year_japan.date</code>.
     */
    public final TableField<IntGovBond_5YearJapanRecord, LocalDate> DATE = createField(DSL.name("date"), SQLDataType.LOCALDATE.nullable(false), this, "");

    /**
     * The column <code>public.int_gov_bond_5_year_japan.created</code>.
     */
    public final TableField<IntGovBond_5YearJapanRecord, LocalDateTime> CREATED = createField(DSL.name("created"), SQLDataType.LOCALDATETIME(6).defaultValue(DSL.field(DSL.raw("now()"), SQLDataType.LOCALDATETIME)), this, "");

    /**
     * The column <code>public.int_gov_bond_5_year_japan.updated</code>.
     */
    public final TableField<IntGovBond_5YearJapanRecord, LocalDateTime> UPDATED = createField(DSL.name("updated"), SQLDataType.LOCALDATETIME(6).defaultValue(DSL.field(DSL.raw("now()"), SQLDataType.LOCALDATETIME)), this, "");

    private IntGovBond_5YearJapan(Name alias, Table<IntGovBond_5YearJapanRecord> aliased) {
        this(alias, aliased, null);
    }

    private IntGovBond_5YearJapan(Name alias, Table<IntGovBond_5YearJapanRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table());
    }

    /**
     * Create an aliased <code>public.int_gov_bond_5_year_japan</code> table
     * reference
     */
    public IntGovBond_5YearJapan(String alias) {
        this(DSL.name(alias), INT_GOV_BOND_5_YEAR_JAPAN);
    }

    /**
     * Create an aliased <code>public.int_gov_bond_5_year_japan</code> table
     * reference
     */
    public IntGovBond_5YearJapan(Name alias) {
        this(alias, INT_GOV_BOND_5_YEAR_JAPAN);
    }

    /**
     * Create a <code>public.int_gov_bond_5_year_japan</code> table reference
     */
    public IntGovBond_5YearJapan() {
        this(DSL.name("int_gov_bond_5_year_japan"), null);
    }

    public <O extends Record> IntGovBond_5YearJapan(Table<O> child, ForeignKey<O, IntGovBond_5YearJapanRecord> key) {
        super(child, key, INT_GOV_BOND_5_YEAR_JAPAN);
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : Public.PUBLIC;
    }

    @Override
    public Identity<IntGovBond_5YearJapanRecord, Integer> getIdentity() {
        return (Identity<IntGovBond_5YearJapanRecord, Integer>) super.getIdentity();
    }

    @Override
    public UniqueKey<IntGovBond_5YearJapanRecord> getPrimaryKey() {
        return Keys.EURO_MARKET_5_YEAR_JAPAN_PKEY;
    }

    @Override
    public IntGovBond_5YearJapan as(String alias) {
        return new IntGovBond_5YearJapan(DSL.name(alias), this);
    }

    @Override
    public IntGovBond_5YearJapan as(Name alias) {
        return new IntGovBond_5YearJapan(alias, this);
    }

    @Override
    public IntGovBond_5YearJapan as(Table<?> alias) {
        return new IntGovBond_5YearJapan(alias.getQualifiedName(), this);
    }

    /**
     * Rename this table
     */
    @Override
    public IntGovBond_5YearJapan rename(String name) {
        return new IntGovBond_5YearJapan(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public IntGovBond_5YearJapan rename(Name name) {
        return new IntGovBond_5YearJapan(name, null);
    }

    /**
     * Rename this table
     */
    @Override
    public IntGovBond_5YearJapan rename(Table<?> name) {
        return new IntGovBond_5YearJapan(name.getQualifiedName(), null);
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
