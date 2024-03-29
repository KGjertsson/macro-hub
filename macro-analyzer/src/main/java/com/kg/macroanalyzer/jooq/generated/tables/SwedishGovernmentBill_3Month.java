/*
 * This file is generated by jOOQ.
 */
package com.kg.macroanalyzer.jooq.generated.tables;


import com.kg.macroanalyzer.jooq.generated.Keys;
import com.kg.macroanalyzer.jooq.generated.Public;
import com.kg.macroanalyzer.jooq.generated.tables.records.SwedishGovernmentBill_3MonthRecord;

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
public class SwedishGovernmentBill_3Month extends TableImpl<SwedishGovernmentBill_3MonthRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of
     * <code>public.swedish_government_bill_3_month</code>
     */
    public static final SwedishGovernmentBill_3Month SWEDISH_GOVERNMENT_BILL_3_MONTH = new SwedishGovernmentBill_3Month();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<SwedishGovernmentBill_3MonthRecord> getRecordType() {
        return SwedishGovernmentBill_3MonthRecord.class;
    }

    /**
     * The column <code>public.swedish_government_bill_3_month.id</code>.
     */
    public final TableField<SwedishGovernmentBill_3MonthRecord, Integer> ID = createField(DSL.name("id"), SQLDataType.INTEGER.nullable(false).identity(true), this, "");

    /**
     * The column <code>public.swedish_government_bill_3_month.global_id</code>.
     */
    public final TableField<SwedishGovernmentBill_3MonthRecord, UUID> GLOBAL_ID = createField(DSL.name("global_id"), SQLDataType.UUID.nullable(false), this, "");

    /**
     * The column <code>public.swedish_government_bill_3_month.value</code>.
     */
    public final TableField<SwedishGovernmentBill_3MonthRecord, Double> VALUE = createField(DSL.name("value"), SQLDataType.DOUBLE.nullable(false), this, "");

    /**
     * The column <code>public.swedish_government_bill_3_month.date</code>.
     */
    public final TableField<SwedishGovernmentBill_3MonthRecord, LocalDate> DATE = createField(DSL.name("date"), SQLDataType.LOCALDATE.nullable(false), this, "");

    /**
     * The column <code>public.swedish_government_bill_3_month.created</code>.
     */
    public final TableField<SwedishGovernmentBill_3MonthRecord, LocalDateTime> CREATED = createField(DSL.name("created"), SQLDataType.LOCALDATETIME(6).defaultValue(DSL.field(DSL.raw("now()"), SQLDataType.LOCALDATETIME)), this, "");

    /**
     * The column <code>public.swedish_government_bill_3_month.updated</code>.
     */
    public final TableField<SwedishGovernmentBill_3MonthRecord, LocalDateTime> UPDATED = createField(DSL.name("updated"), SQLDataType.LOCALDATETIME(6).defaultValue(DSL.field(DSL.raw("now()"), SQLDataType.LOCALDATETIME)), this, "");

    private SwedishGovernmentBill_3Month(Name alias, Table<SwedishGovernmentBill_3MonthRecord> aliased) {
        this(alias, aliased, null);
    }

    private SwedishGovernmentBill_3Month(Name alias, Table<SwedishGovernmentBill_3MonthRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table());
    }

    /**
     * Create an aliased <code>public.swedish_government_bill_3_month</code>
     * table reference
     */
    public SwedishGovernmentBill_3Month(String alias) {
        this(DSL.name(alias), SWEDISH_GOVERNMENT_BILL_3_MONTH);
    }

    /**
     * Create an aliased <code>public.swedish_government_bill_3_month</code>
     * table reference
     */
    public SwedishGovernmentBill_3Month(Name alias) {
        this(alias, SWEDISH_GOVERNMENT_BILL_3_MONTH);
    }

    /**
     * Create a <code>public.swedish_government_bill_3_month</code> table
     * reference
     */
    public SwedishGovernmentBill_3Month() {
        this(DSL.name("swedish_government_bill_3_month"), null);
    }

    public <O extends Record> SwedishGovernmentBill_3Month(Table<O> child, ForeignKey<O, SwedishGovernmentBill_3MonthRecord> key) {
        super(child, key, SWEDISH_GOVERNMENT_BILL_3_MONTH);
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : Public.PUBLIC;
    }

    @Override
    public Identity<SwedishGovernmentBill_3MonthRecord, Integer> getIdentity() {
        return (Identity<SwedishGovernmentBill_3MonthRecord, Integer>) super.getIdentity();
    }

    @Override
    public UniqueKey<SwedishGovernmentBill_3MonthRecord> getPrimaryKey() {
        return Keys.SWEDISH_GOVERNMENT_BILL_3_MONTH_PKEY;
    }

    @Override
    public SwedishGovernmentBill_3Month as(String alias) {
        return new SwedishGovernmentBill_3Month(DSL.name(alias), this);
    }

    @Override
    public SwedishGovernmentBill_3Month as(Name alias) {
        return new SwedishGovernmentBill_3Month(alias, this);
    }

    @Override
    public SwedishGovernmentBill_3Month as(Table<?> alias) {
        return new SwedishGovernmentBill_3Month(alias.getQualifiedName(), this);
    }

    /**
     * Rename this table
     */
    @Override
    public SwedishGovernmentBill_3Month rename(String name) {
        return new SwedishGovernmentBill_3Month(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public SwedishGovernmentBill_3Month rename(Name name) {
        return new SwedishGovernmentBill_3Month(name, null);
    }

    /**
     * Rename this table
     */
    @Override
    public SwedishGovernmentBill_3Month rename(Table<?> name) {
        return new SwedishGovernmentBill_3Month(name.getQualifiedName(), null);
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
