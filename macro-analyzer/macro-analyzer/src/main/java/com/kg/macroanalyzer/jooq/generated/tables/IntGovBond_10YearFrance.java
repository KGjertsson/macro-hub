/*
 * This file is generated by jOOQ.
 */
package com.kg.macroanalyzer.jooq.generated.tables;


import com.kg.macroanalyzer.jooq.generated.Keys;
import com.kg.macroanalyzer.jooq.generated.Public;
import com.kg.macroanalyzer.jooq.generated.tables.records.IntGovBond_10YearFranceRecord;

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
public class IntGovBond_10YearFrance extends TableImpl<IntGovBond_10YearFranceRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>public.int_gov_bond_10_year_france</code>
     */
    public static final IntGovBond_10YearFrance INT_GOV_BOND_10_YEAR_FRANCE = new IntGovBond_10YearFrance();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<IntGovBond_10YearFranceRecord> getRecordType() {
        return IntGovBond_10YearFranceRecord.class;
    }

    /**
     * The column <code>public.int_gov_bond_10_year_france.id</code>.
     */
    public final TableField<IntGovBond_10YearFranceRecord, Integer> ID = createField(DSL.name("id"), SQLDataType.INTEGER.nullable(false).identity(true), this, "");

    /**
     * The column <code>public.int_gov_bond_10_year_france.global_id</code>.
     */
    public final TableField<IntGovBond_10YearFranceRecord, UUID> GLOBAL_ID = createField(DSL.name("global_id"), SQLDataType.UUID.nullable(false), this, "");

    /**
     * The column <code>public.int_gov_bond_10_year_france.value</code>.
     */
    public final TableField<IntGovBond_10YearFranceRecord, Double> VALUE = createField(DSL.name("value"), SQLDataType.DOUBLE.nullable(false), this, "");

    /**
     * The column <code>public.int_gov_bond_10_year_france.date</code>.
     */
    public final TableField<IntGovBond_10YearFranceRecord, LocalDate> DATE = createField(DSL.name("date"), SQLDataType.LOCALDATE.nullable(false), this, "");

    /**
     * The column <code>public.int_gov_bond_10_year_france.created</code>.
     */
    public final TableField<IntGovBond_10YearFranceRecord, LocalDateTime> CREATED = createField(DSL.name("created"), SQLDataType.LOCALDATETIME(6).defaultValue(DSL.field(DSL.raw("now()"), SQLDataType.LOCALDATETIME)), this, "");

    /**
     * The column <code>public.int_gov_bond_10_year_france.updated</code>.
     */
    public final TableField<IntGovBond_10YearFranceRecord, LocalDateTime> UPDATED = createField(DSL.name("updated"), SQLDataType.LOCALDATETIME(6).defaultValue(DSL.field(DSL.raw("now()"), SQLDataType.LOCALDATETIME)), this, "");

    private IntGovBond_10YearFrance(Name alias, Table<IntGovBond_10YearFranceRecord> aliased) {
        this(alias, aliased, null);
    }

    private IntGovBond_10YearFrance(Name alias, Table<IntGovBond_10YearFranceRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table());
    }

    /**
     * Create an aliased <code>public.int_gov_bond_10_year_france</code> table
     * reference
     */
    public IntGovBond_10YearFrance(String alias) {
        this(DSL.name(alias), INT_GOV_BOND_10_YEAR_FRANCE);
    }

    /**
     * Create an aliased <code>public.int_gov_bond_10_year_france</code> table
     * reference
     */
    public IntGovBond_10YearFrance(Name alias) {
        this(alias, INT_GOV_BOND_10_YEAR_FRANCE);
    }

    /**
     * Create a <code>public.int_gov_bond_10_year_france</code> table reference
     */
    public IntGovBond_10YearFrance() {
        this(DSL.name("int_gov_bond_10_year_france"), null);
    }

    public <O extends Record> IntGovBond_10YearFrance(Table<O> child, ForeignKey<O, IntGovBond_10YearFranceRecord> key) {
        super(child, key, INT_GOV_BOND_10_YEAR_FRANCE);
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : Public.PUBLIC;
    }

    @Override
    public Identity<IntGovBond_10YearFranceRecord, Integer> getIdentity() {
        return (Identity<IntGovBond_10YearFranceRecord, Integer>) super.getIdentity();
    }

    @Override
    public UniqueKey<IntGovBond_10YearFranceRecord> getPrimaryKey() {
        return Keys.EURO_MARKET_10_YEAR_FRANCE_PKEY;
    }

    @Override
    public IntGovBond_10YearFrance as(String alias) {
        return new IntGovBond_10YearFrance(DSL.name(alias), this);
    }

    @Override
    public IntGovBond_10YearFrance as(Name alias) {
        return new IntGovBond_10YearFrance(alias, this);
    }

    @Override
    public IntGovBond_10YearFrance as(Table<?> alias) {
        return new IntGovBond_10YearFrance(alias.getQualifiedName(), this);
    }

    /**
     * Rename this table
     */
    @Override
    public IntGovBond_10YearFrance rename(String name) {
        return new IntGovBond_10YearFrance(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public IntGovBond_10YearFrance rename(Name name) {
        return new IntGovBond_10YearFrance(name, null);
    }

    /**
     * Rename this table
     */
    @Override
    public IntGovBond_10YearFrance rename(Table<?> name) {
        return new IntGovBond_10YearFrance(name.getQualifiedName(), null);
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
