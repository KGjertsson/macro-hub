/*
 * This file is generated by jOOQ.
 */
package com.kg.macroanalyzer.jooq.generated.tables.records;


import com.kg.macroanalyzer.jooq.generated.tables.IntGovBond_10YearNetherlands;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import javax.annotation.processing.Generated;

import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record6;
import org.jooq.Row6;
import org.jooq.impl.UpdatableRecordImpl;


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
public class IntGovBond_10YearNetherlandsRecord extends UpdatableRecordImpl<IntGovBond_10YearNetherlandsRecord> implements Record6<Integer, UUID, Double, LocalDate, LocalDateTime, LocalDateTime> {

    private static final long serialVersionUID = 1L;

    /**
     * Setter for <code>public.int_gov_bond_10_year_netherlands.id</code>.
     */
    public void setId(Integer value) {
        set(0, value);
    }

    /**
     * Getter for <code>public.int_gov_bond_10_year_netherlands.id</code>.
     */
    public Integer getId() {
        return (Integer) get(0);
    }

    /**
     * Setter for
     * <code>public.int_gov_bond_10_year_netherlands.global_id</code>.
     */
    public void setGlobalId(UUID value) {
        set(1, value);
    }

    /**
     * Getter for
     * <code>public.int_gov_bond_10_year_netherlands.global_id</code>.
     */
    public UUID getGlobalId() {
        return (UUID) get(1);
    }

    /**
     * Setter for <code>public.int_gov_bond_10_year_netherlands.value</code>.
     */
    public void setValue(Double value) {
        set(2, value);
    }

    /**
     * Getter for <code>public.int_gov_bond_10_year_netherlands.value</code>.
     */
    public Double getValue() {
        return (Double) get(2);
    }

    /**
     * Setter for <code>public.int_gov_bond_10_year_netherlands.date</code>.
     */
    public void setDate(LocalDate value) {
        set(3, value);
    }

    /**
     * Getter for <code>public.int_gov_bond_10_year_netherlands.date</code>.
     */
    public LocalDate getDate() {
        return (LocalDate) get(3);
    }

    /**
     * Setter for <code>public.int_gov_bond_10_year_netherlands.created</code>.
     */
    public void setCreated(LocalDateTime value) {
        set(4, value);
    }

    /**
     * Getter for <code>public.int_gov_bond_10_year_netherlands.created</code>.
     */
    public LocalDateTime getCreated() {
        return (LocalDateTime) get(4);
    }

    /**
     * Setter for <code>public.int_gov_bond_10_year_netherlands.updated</code>.
     */
    public void setUpdated(LocalDateTime value) {
        set(5, value);
    }

    /**
     * Getter for <code>public.int_gov_bond_10_year_netherlands.updated</code>.
     */
    public LocalDateTime getUpdated() {
        return (LocalDateTime) get(5);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    public Record1<Integer> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Record6 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row6<Integer, UUID, Double, LocalDate, LocalDateTime, LocalDateTime> fieldsRow() {
        return (Row6) super.fieldsRow();
    }

    @Override
    public Row6<Integer, UUID, Double, LocalDate, LocalDateTime, LocalDateTime> valuesRow() {
        return (Row6) super.valuesRow();
    }

    @Override
    public Field<Integer> field1() {
        return IntGovBond_10YearNetherlands.INT_GOV_BOND_10_YEAR_NETHERLANDS.ID;
    }

    @Override
    public Field<UUID> field2() {
        return IntGovBond_10YearNetherlands.INT_GOV_BOND_10_YEAR_NETHERLANDS.GLOBAL_ID;
    }

    @Override
    public Field<Double> field3() {
        return IntGovBond_10YearNetherlands.INT_GOV_BOND_10_YEAR_NETHERLANDS.VALUE;
    }

    @Override
    public Field<LocalDate> field4() {
        return IntGovBond_10YearNetherlands.INT_GOV_BOND_10_YEAR_NETHERLANDS.DATE;
    }

    @Override
    public Field<LocalDateTime> field5() {
        return IntGovBond_10YearNetherlands.INT_GOV_BOND_10_YEAR_NETHERLANDS.CREATED;
    }

    @Override
    public Field<LocalDateTime> field6() {
        return IntGovBond_10YearNetherlands.INT_GOV_BOND_10_YEAR_NETHERLANDS.UPDATED;
    }

    @Override
    public Integer component1() {
        return getId();
    }

    @Override
    public UUID component2() {
        return getGlobalId();
    }

    @Override
    public Double component3() {
        return getValue();
    }

    @Override
    public LocalDate component4() {
        return getDate();
    }

    @Override
    public LocalDateTime component5() {
        return getCreated();
    }

    @Override
    public LocalDateTime component6() {
        return getUpdated();
    }

    @Override
    public Integer value1() {
        return getId();
    }

    @Override
    public UUID value2() {
        return getGlobalId();
    }

    @Override
    public Double value3() {
        return getValue();
    }

    @Override
    public LocalDate value4() {
        return getDate();
    }

    @Override
    public LocalDateTime value5() {
        return getCreated();
    }

    @Override
    public LocalDateTime value6() {
        return getUpdated();
    }

    @Override
    public IntGovBond_10YearNetherlandsRecord value1(Integer value) {
        setId(value);
        return this;
    }

    @Override
    public IntGovBond_10YearNetherlandsRecord value2(UUID value) {
        setGlobalId(value);
        return this;
    }

    @Override
    public IntGovBond_10YearNetherlandsRecord value3(Double value) {
        setValue(value);
        return this;
    }

    @Override
    public IntGovBond_10YearNetherlandsRecord value4(LocalDate value) {
        setDate(value);
        return this;
    }

    @Override
    public IntGovBond_10YearNetherlandsRecord value5(LocalDateTime value) {
        setCreated(value);
        return this;
    }

    @Override
    public IntGovBond_10YearNetherlandsRecord value6(LocalDateTime value) {
        setUpdated(value);
        return this;
    }

    @Override
    public IntGovBond_10YearNetherlandsRecord values(Integer value1, UUID value2, Double value3, LocalDate value4, LocalDateTime value5, LocalDateTime value6) {
        value1(value1);
        value2(value2);
        value3(value3);
        value4(value4);
        value5(value5);
        value6(value6);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached IntGovBond_10YearNetherlandsRecord
     */
    public IntGovBond_10YearNetherlandsRecord() {
        super(IntGovBond_10YearNetherlands.INT_GOV_BOND_10_YEAR_NETHERLANDS);
    }

    /**
     * Create a detached, initialised IntGovBond_10YearNetherlandsRecord
     */
    public IntGovBond_10YearNetherlandsRecord(Integer id, UUID globalId, Double value, LocalDate date, LocalDateTime created, LocalDateTime updated) {
        super(IntGovBond_10YearNetherlands.INT_GOV_BOND_10_YEAR_NETHERLANDS);

        setId(id);
        setGlobalId(globalId);
        setValue(value);
        setDate(date);
        setCreated(created);
        setUpdated(updated);
        resetChangedOnNotNull();
    }
}
