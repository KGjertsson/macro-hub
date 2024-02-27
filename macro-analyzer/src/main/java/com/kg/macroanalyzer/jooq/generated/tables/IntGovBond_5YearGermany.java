/*
 * This file is generated by jOOQ.
 */
package com.kg.macroanalyzer.jooq.generated.tables;


import com.kg.macroanalyzer.jooq.generated.Keys;
import com.kg.macroanalyzer.jooq.generated.Public;
import com.kg.macroanalyzer.jooq.generated.tables.records.IntGovBond_5YearGermanyRecord;

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
public class IntGovBond_5YearGermany extends TableImpl<IntGovBond_5YearGermanyRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>public.int_gov_bond_5_year_germany</code>
     */
    public static final IntGovBond_5YearGermany INT_GOV_BOND_5_YEAR_GERMANY = new IntGovBond_5YearGermany();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<IntGovBond_5YearGermanyRecord> getRecordType() {
        return IntGovBond_5YearGermanyRecord.class;
    }

    /**
     * The column <code>public.int_gov_bond_5_year_germany.id</code>.
     */
    public final TableField<IntGovBond_5YearGermanyRecord, Integer> ID = createField(DSL.name("id"), SQLDataType.INTEGER.nullable(false).identity(true), this, "");

    /**
     * The column <code>public.int_gov_bond_5_year_germany.global_id</code>.
     */
    public final TableField<IntGovBond_5YearGermanyRecord, UUID> GLOBAL_ID = createField(DSL.name("global_id"), SQLDataType.UUID.nullable(false), this, "");

    /**
     * The column <code>public.int_gov_bond_5_year_germany.value</code>.
     */
    public final TableField<IntGovBond_5YearGermanyRecord, Double> VALUE = createField(DSL.name("value"), SQLDataType.DOUBLE.nullable(false), this, "");

    /**
     * The column <code>public.int_gov_bond_5_year_germany.date</code>.
     */
    public final TableField<IntGovBond_5YearGermanyRecord, LocalDate> DATE = createField(DSL.name("date"), SQLDataType.LOCALDATE.nullable(false), this, "");

    /**
     * The column <code>public.int_gov_bond_5_year_germany.created</code>.
     */
    public final TableField<IntGovBond_5YearGermanyRecord, LocalDateTime> CREATED = createField(DSL.name("created"), SQLDataType.LOCALDATETIME(6).defaultValue(DSL.field(DSL.raw("now()"), SQLDataType.LOCALDATETIME)), this, "");

    /**
     * The column <code>public.int_gov_bond_5_year_germany.updated</code>.
     */
    public final TableField<IntGovBond_5YearGermanyRecord, LocalDateTime> UPDATED = createField(DSL.name("updated"), SQLDataType.LOCALDATETIME(6).defaultValue(DSL.field(DSL.raw("now()"), SQLDataType.LOCALDATETIME)), this, "");

    private IntGovBond_5YearGermany(Name alias, Table<IntGovBond_5YearGermanyRecord> aliased) {
        this(alias, aliased, null);
    }

    private IntGovBond_5YearGermany(Name alias, Table<IntGovBond_5YearGermanyRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table());
    }

    /**
     * Create an aliased <code>public.int_gov_bond_5_year_germany</code> table
     * reference
     */
    public IntGovBond_5YearGermany(String alias) {
        this(DSL.name(alias), INT_GOV_BOND_5_YEAR_GERMANY);
    }

    /**
     * Create an aliased <code>public.int_gov_bond_5_year_germany</code> table
     * reference
     */
    public IntGovBond_5YearGermany(Name alias) {
        this(alias, INT_GOV_BOND_5_YEAR_GERMANY);
    }

    /**
     * Create a <code>public.int_gov_bond_5_year_germany</code> table reference
     */
    public IntGovBond_5YearGermany() {
        this(DSL.name("int_gov_bond_5_year_germany"), null);
    }

    public <O extends Record> IntGovBond_5YearGermany(Table<O> child, ForeignKey<O, IntGovBond_5YearGermanyRecord> key) {
        super(child, key, INT_GOV_BOND_5_YEAR_GERMANY);
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : Public.PUBLIC;
    }

    @Override
    public Identity<IntGovBond_5YearGermanyRecord, Integer> getIdentity() {
        return (Identity<IntGovBond_5YearGermanyRecord, Integer>) super.getIdentity();
    }

    @Override
    public UniqueKey<IntGovBond_5YearGermanyRecord> getPrimaryKey() {
        return Keys.EURO_MARKET_5_YEAR_GERMANY_PKEY;
    }

    @Override
    public IntGovBond_5YearGermany as(String alias) {
        return new IntGovBond_5YearGermany(DSL.name(alias), this);
    }

    @Override
    public IntGovBond_5YearGermany as(Name alias) {
        return new IntGovBond_5YearGermany(alias, this);
    }

    @Override
    public IntGovBond_5YearGermany as(Table<?> alias) {
        return new IntGovBond_5YearGermany(alias.getQualifiedName(), this);
    }

    /**
     * Rename this table
     */
    @Override
    public IntGovBond_5YearGermany rename(String name) {
        return new IntGovBond_5YearGermany(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public IntGovBond_5YearGermany rename(Name name) {
        return new IntGovBond_5YearGermany(name, null);
    }

    /**
     * Rename this table
     */
    @Override
    public IntGovBond_5YearGermany rename(Table<?> name) {
        return new IntGovBond_5YearGermany(name.getQualifiedName(), null);
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
