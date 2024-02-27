/*
 * This file is generated by jOOQ.
 */
package com.kg.macroanalyzer.jooq.generated.tables;


import com.kg.macroanalyzer.jooq.generated.Keys;
import com.kg.macroanalyzer.jooq.generated.Public;
import com.kg.macroanalyzer.jooq.generated.tables.records.IntGovBond_10YearNetherlandsRecord;

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
public class IntGovBond_10YearNetherlands extends TableImpl<IntGovBond_10YearNetherlandsRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of
     * <code>public.int_gov_bond_10_year_netherlands</code>
     */
    public static final IntGovBond_10YearNetherlands INT_GOV_BOND_10_YEAR_NETHERLANDS = new IntGovBond_10YearNetherlands();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<IntGovBond_10YearNetherlandsRecord> getRecordType() {
        return IntGovBond_10YearNetherlandsRecord.class;
    }

    /**
     * The column <code>public.int_gov_bond_10_year_netherlands.id</code>.
     */
    public final TableField<IntGovBond_10YearNetherlandsRecord, Integer> ID = createField(DSL.name("id"), SQLDataType.INTEGER.nullable(false).identity(true), this, "");

    /**
     * The column
     * <code>public.int_gov_bond_10_year_netherlands.global_id</code>.
     */
    public final TableField<IntGovBond_10YearNetherlandsRecord, UUID> GLOBAL_ID = createField(DSL.name("global_id"), SQLDataType.UUID.nullable(false), this, "");

    /**
     * The column <code>public.int_gov_bond_10_year_netherlands.value</code>.
     */
    public final TableField<IntGovBond_10YearNetherlandsRecord, Double> VALUE = createField(DSL.name("value"), SQLDataType.DOUBLE.nullable(false), this, "");

    /**
     * The column <code>public.int_gov_bond_10_year_netherlands.date</code>.
     */
    public final TableField<IntGovBond_10YearNetherlandsRecord, LocalDate> DATE = createField(DSL.name("date"), SQLDataType.LOCALDATE.nullable(false), this, "");

    /**
     * The column <code>public.int_gov_bond_10_year_netherlands.created</code>.
     */
    public final TableField<IntGovBond_10YearNetherlandsRecord, LocalDateTime> CREATED = createField(DSL.name("created"), SQLDataType.LOCALDATETIME(6).defaultValue(DSL.field(DSL.raw("now()"), SQLDataType.LOCALDATETIME)), this, "");

    /**
     * The column <code>public.int_gov_bond_10_year_netherlands.updated</code>.
     */
    public final TableField<IntGovBond_10YearNetherlandsRecord, LocalDateTime> UPDATED = createField(DSL.name("updated"), SQLDataType.LOCALDATETIME(6).defaultValue(DSL.field(DSL.raw("now()"), SQLDataType.LOCALDATETIME)), this, "");

    private IntGovBond_10YearNetherlands(Name alias, Table<IntGovBond_10YearNetherlandsRecord> aliased) {
        this(alias, aliased, null);
    }

    private IntGovBond_10YearNetherlands(Name alias, Table<IntGovBond_10YearNetherlandsRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table());
    }

    /**
     * Create an aliased <code>public.int_gov_bond_10_year_netherlands</code>
     * table reference
     */
    public IntGovBond_10YearNetherlands(String alias) {
        this(DSL.name(alias), INT_GOV_BOND_10_YEAR_NETHERLANDS);
    }

    /**
     * Create an aliased <code>public.int_gov_bond_10_year_netherlands</code>
     * table reference
     */
    public IntGovBond_10YearNetherlands(Name alias) {
        this(alias, INT_GOV_BOND_10_YEAR_NETHERLANDS);
    }

    /**
     * Create a <code>public.int_gov_bond_10_year_netherlands</code> table
     * reference
     */
    public IntGovBond_10YearNetherlands() {
        this(DSL.name("int_gov_bond_10_year_netherlands"), null);
    }

    public <O extends Record> IntGovBond_10YearNetherlands(Table<O> child, ForeignKey<O, IntGovBond_10YearNetherlandsRecord> key) {
        super(child, key, INT_GOV_BOND_10_YEAR_NETHERLANDS);
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : Public.PUBLIC;
    }

    @Override
    public Identity<IntGovBond_10YearNetherlandsRecord, Integer> getIdentity() {
        return (Identity<IntGovBond_10YearNetherlandsRecord, Integer>) super.getIdentity();
    }

    @Override
    public UniqueKey<IntGovBond_10YearNetherlandsRecord> getPrimaryKey() {
        return Keys.EURO_MARKET_10_YEAR_NETHERLANDS_PKEY;
    }

    @Override
    public IntGovBond_10YearNetherlands as(String alias) {
        return new IntGovBond_10YearNetherlands(DSL.name(alias), this);
    }

    @Override
    public IntGovBond_10YearNetherlands as(Name alias) {
        return new IntGovBond_10YearNetherlands(alias, this);
    }

    @Override
    public IntGovBond_10YearNetherlands as(Table<?> alias) {
        return new IntGovBond_10YearNetherlands(alias.getQualifiedName(), this);
    }

    /**
     * Rename this table
     */
    @Override
    public IntGovBond_10YearNetherlands rename(String name) {
        return new IntGovBond_10YearNetherlands(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public IntGovBond_10YearNetherlands rename(Name name) {
        return new IntGovBond_10YearNetherlands(name, null);
    }

    /**
     * Rename this table
     */
    @Override
    public IntGovBond_10YearNetherlands rename(Table<?> name) {
        return new IntGovBond_10YearNetherlands(name.getQualifiedName(), null);
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
