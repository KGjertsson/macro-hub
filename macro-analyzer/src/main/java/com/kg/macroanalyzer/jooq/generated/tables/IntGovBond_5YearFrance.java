/*
 * This file is generated by jOOQ.
 */
package com.kg.macroanalyzer.jooq.generated.tables;


import com.kg.macroanalyzer.jooq.generated.Keys;
import com.kg.macroanalyzer.jooq.generated.Public;
import com.kg.macroanalyzer.jooq.generated.tables.records.IntGovBond_5YearFranceRecord;

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
public class IntGovBond_5YearFrance extends TableImpl<IntGovBond_5YearFranceRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>public.int_gov_bond_5_year_france</code>
     */
    public static final IntGovBond_5YearFrance INT_GOV_BOND_5_YEAR_FRANCE = new IntGovBond_5YearFrance();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<IntGovBond_5YearFranceRecord> getRecordType() {
        return IntGovBond_5YearFranceRecord.class;
    }

    /**
     * The column <code>public.int_gov_bond_5_year_france.id</code>.
     */
    public final TableField<IntGovBond_5YearFranceRecord, Integer> ID = createField(DSL.name("id"), SQLDataType.INTEGER.nullable(false).identity(true), this, "");

    /**
     * The column <code>public.int_gov_bond_5_year_france.global_id</code>.
     */
    public final TableField<IntGovBond_5YearFranceRecord, UUID> GLOBAL_ID = createField(DSL.name("global_id"), SQLDataType.UUID.nullable(false), this, "");

    /**
     * The column <code>public.int_gov_bond_5_year_france.value</code>.
     */
    public final TableField<IntGovBond_5YearFranceRecord, Double> VALUE = createField(DSL.name("value"), SQLDataType.DOUBLE.nullable(false), this, "");

    /**
     * The column <code>public.int_gov_bond_5_year_france.date</code>.
     */
    public final TableField<IntGovBond_5YearFranceRecord, LocalDate> DATE = createField(DSL.name("date"), SQLDataType.LOCALDATE.nullable(false), this, "");

    /**
     * The column <code>public.int_gov_bond_5_year_france.created</code>.
     */
    public final TableField<IntGovBond_5YearFranceRecord, LocalDateTime> CREATED = createField(DSL.name("created"), SQLDataType.LOCALDATETIME(6).defaultValue(DSL.field(DSL.raw("now()"), SQLDataType.LOCALDATETIME)), this, "");

    /**
     * The column <code>public.int_gov_bond_5_year_france.updated</code>.
     */
    public final TableField<IntGovBond_5YearFranceRecord, LocalDateTime> UPDATED = createField(DSL.name("updated"), SQLDataType.LOCALDATETIME(6).defaultValue(DSL.field(DSL.raw("now()"), SQLDataType.LOCALDATETIME)), this, "");

    private IntGovBond_5YearFrance(Name alias, Table<IntGovBond_5YearFranceRecord> aliased) {
        this(alias, aliased, null);
    }

    private IntGovBond_5YearFrance(Name alias, Table<IntGovBond_5YearFranceRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table());
    }

    /**
     * Create an aliased <code>public.int_gov_bond_5_year_france</code> table
     * reference
     */
    public IntGovBond_5YearFrance(String alias) {
        this(DSL.name(alias), INT_GOV_BOND_5_YEAR_FRANCE);
    }

    /**
     * Create an aliased <code>public.int_gov_bond_5_year_france</code> table
     * reference
     */
    public IntGovBond_5YearFrance(Name alias) {
        this(alias, INT_GOV_BOND_5_YEAR_FRANCE);
    }

    /**
     * Create a <code>public.int_gov_bond_5_year_france</code> table reference
     */
    public IntGovBond_5YearFrance() {
        this(DSL.name("int_gov_bond_5_year_france"), null);
    }

    public <O extends Record> IntGovBond_5YearFrance(Table<O> child, ForeignKey<O, IntGovBond_5YearFranceRecord> key) {
        super(child, key, INT_GOV_BOND_5_YEAR_FRANCE);
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : Public.PUBLIC;
    }

    @Override
    public Identity<IntGovBond_5YearFranceRecord, Integer> getIdentity() {
        return (Identity<IntGovBond_5YearFranceRecord, Integer>) super.getIdentity();
    }

    @Override
    public UniqueKey<IntGovBond_5YearFranceRecord> getPrimaryKey() {
        return Keys.EURO_MARKET_5_YEAR_FRANCE_PKEY;
    }

    @Override
    public IntGovBond_5YearFrance as(String alias) {
        return new IntGovBond_5YearFrance(DSL.name(alias), this);
    }

    @Override
    public IntGovBond_5YearFrance as(Name alias) {
        return new IntGovBond_5YearFrance(alias, this);
    }

    @Override
    public IntGovBond_5YearFrance as(Table<?> alias) {
        return new IntGovBond_5YearFrance(alias.getQualifiedName(), this);
    }

    /**
     * Rename this table
     */
    @Override
    public IntGovBond_5YearFrance rename(String name) {
        return new IntGovBond_5YearFrance(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public IntGovBond_5YearFrance rename(Name name) {
        return new IntGovBond_5YearFrance(name, null);
    }

    /**
     * Rename this table
     */
    @Override
    public IntGovBond_5YearFrance rename(Table<?> name) {
        return new IntGovBond_5YearFrance(name.getQualifiedName(), null);
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
