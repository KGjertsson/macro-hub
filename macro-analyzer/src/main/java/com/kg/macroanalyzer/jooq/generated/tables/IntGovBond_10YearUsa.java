/*
 * This file is generated by jOOQ.
 */
package com.kg.macroanalyzer.jooq.generated.tables;


import com.kg.macroanalyzer.jooq.generated.Keys;
import com.kg.macroanalyzer.jooq.generated.Public;
import com.kg.macroanalyzer.jooq.generated.tables.records.IntGovBond_10YearUsaRecord;

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
public class IntGovBond_10YearUsa extends TableImpl<IntGovBond_10YearUsaRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>public.int_gov_bond_10_year_usa</code>
     */
    public static final IntGovBond_10YearUsa INT_GOV_BOND_10_YEAR_USA = new IntGovBond_10YearUsa();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<IntGovBond_10YearUsaRecord> getRecordType() {
        return IntGovBond_10YearUsaRecord.class;
    }

    /**
     * The column <code>public.int_gov_bond_10_year_usa.id</code>.
     */
    public final TableField<IntGovBond_10YearUsaRecord, Integer> ID = createField(DSL.name("id"), SQLDataType.INTEGER.nullable(false).identity(true), this, "");

    /**
     * The column <code>public.int_gov_bond_10_year_usa.global_id</code>.
     */
    public final TableField<IntGovBond_10YearUsaRecord, UUID> GLOBAL_ID = createField(DSL.name("global_id"), SQLDataType.UUID.nullable(false), this, "");

    /**
     * The column <code>public.int_gov_bond_10_year_usa.value</code>.
     */
    public final TableField<IntGovBond_10YearUsaRecord, Double> VALUE = createField(DSL.name("value"), SQLDataType.DOUBLE.nullable(false), this, "");

    /**
     * The column <code>public.int_gov_bond_10_year_usa.date</code>.
     */
    public final TableField<IntGovBond_10YearUsaRecord, LocalDate> DATE = createField(DSL.name("date"), SQLDataType.LOCALDATE.nullable(false), this, "");

    /**
     * The column <code>public.int_gov_bond_10_year_usa.created</code>.
     */
    public final TableField<IntGovBond_10YearUsaRecord, LocalDateTime> CREATED = createField(DSL.name("created"), SQLDataType.LOCALDATETIME(6).defaultValue(DSL.field(DSL.raw("now()"), SQLDataType.LOCALDATETIME)), this, "");

    /**
     * The column <code>public.int_gov_bond_10_year_usa.updated</code>.
     */
    public final TableField<IntGovBond_10YearUsaRecord, LocalDateTime> UPDATED = createField(DSL.name("updated"), SQLDataType.LOCALDATETIME(6).defaultValue(DSL.field(DSL.raw("now()"), SQLDataType.LOCALDATETIME)), this, "");

    private IntGovBond_10YearUsa(Name alias, Table<IntGovBond_10YearUsaRecord> aliased) {
        this(alias, aliased, null);
    }

    private IntGovBond_10YearUsa(Name alias, Table<IntGovBond_10YearUsaRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table());
    }

    /**
     * Create an aliased <code>public.int_gov_bond_10_year_usa</code> table
     * reference
     */
    public IntGovBond_10YearUsa(String alias) {
        this(DSL.name(alias), INT_GOV_BOND_10_YEAR_USA);
    }

    /**
     * Create an aliased <code>public.int_gov_bond_10_year_usa</code> table
     * reference
     */
    public IntGovBond_10YearUsa(Name alias) {
        this(alias, INT_GOV_BOND_10_YEAR_USA);
    }

    /**
     * Create a <code>public.int_gov_bond_10_year_usa</code> table reference
     */
    public IntGovBond_10YearUsa() {
        this(DSL.name("int_gov_bond_10_year_usa"), null);
    }

    public <O extends Record> IntGovBond_10YearUsa(Table<O> child, ForeignKey<O, IntGovBond_10YearUsaRecord> key) {
        super(child, key, INT_GOV_BOND_10_YEAR_USA);
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : Public.PUBLIC;
    }

    @Override
    public Identity<IntGovBond_10YearUsaRecord, Integer> getIdentity() {
        return (Identity<IntGovBond_10YearUsaRecord, Integer>) super.getIdentity();
    }

    @Override
    public UniqueKey<IntGovBond_10YearUsaRecord> getPrimaryKey() {
        return Keys.EURO_MARKET_10_YEAR_USA_PKEY;
    }

    @Override
    public IntGovBond_10YearUsa as(String alias) {
        return new IntGovBond_10YearUsa(DSL.name(alias), this);
    }

    @Override
    public IntGovBond_10YearUsa as(Name alias) {
        return new IntGovBond_10YearUsa(alias, this);
    }

    @Override
    public IntGovBond_10YearUsa as(Table<?> alias) {
        return new IntGovBond_10YearUsa(alias.getQualifiedName(), this);
    }

    /**
     * Rename this table
     */
    @Override
    public IntGovBond_10YearUsa rename(String name) {
        return new IntGovBond_10YearUsa(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public IntGovBond_10YearUsa rename(Name name) {
        return new IntGovBond_10YearUsa(name, null);
    }

    /**
     * Rename this table
     */
    @Override
    public IntGovBond_10YearUsa rename(Table<?> name) {
        return new IntGovBond_10YearUsa(name.getQualifiedName(), null);
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
