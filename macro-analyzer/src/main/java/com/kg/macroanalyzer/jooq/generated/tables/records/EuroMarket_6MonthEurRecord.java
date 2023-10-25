/*
 * This file is generated by jOOQ.
 */
package com.kg.macroanalyzer.jooq.generated.tables.records;


import com.kg.macroanalyzer.jooq.generated.tables.EuroMarket_6MonthEur;

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
public class EuroMarket_6MonthEurRecord extends UpdatableRecordImpl<EuroMarket_6MonthEurRecord> implements Record6<Integer, UUID, Double, LocalDate, LocalDateTime, LocalDateTime> {

    private static final long serialVersionUID = 1L;

    /**
     * Setter for <code>public.euro_market_6_month_eur.id</code>.
     */
    public void setId(Integer value) {
        set(0, value);
    }

    /**
     * Getter for <code>public.euro_market_6_month_eur.id</code>.
     */
    public Integer getId() {
        return (Integer) get(0);
    }

    /**
     * Setter for <code>public.euro_market_6_month_eur.global_id</code>.
     */
    public void setGlobalId(UUID value) {
        set(1, value);
    }

    /**
     * Getter for <code>public.euro_market_6_month_eur.global_id</code>.
     */
    public UUID getGlobalId() {
        return (UUID) get(1);
    }

    /**
     * Setter for <code>public.euro_market_6_month_eur.value</code>.
     */
    public void setValue(Double value) {
        set(2, value);
    }

    /**
     * Getter for <code>public.euro_market_6_month_eur.value</code>.
     */
    public Double getValue() {
        return (Double) get(2);
    }

    /**
     * Setter for <code>public.euro_market_6_month_eur.date</code>.
     */
    public void setDate(LocalDate value) {
        set(3, value);
    }

    /**
     * Getter for <code>public.euro_market_6_month_eur.date</code>.
     */
    public LocalDate getDate() {
        return (LocalDate) get(3);
    }

    /**
     * Setter for <code>public.euro_market_6_month_eur.created</code>.
     */
    public void setCreated(LocalDateTime value) {
        set(4, value);
    }

    /**
     * Getter for <code>public.euro_market_6_month_eur.created</code>.
     */
    public LocalDateTime getCreated() {
        return (LocalDateTime) get(4);
    }

    /**
     * Setter for <code>public.euro_market_6_month_eur.updated</code>.
     */
    public void setUpdated(LocalDateTime value) {
        set(5, value);
    }

    /**
     * Getter for <code>public.euro_market_6_month_eur.updated</code>.
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
        return EuroMarket_6MonthEur.EURO_MARKET_6_MONTH_EUR.ID;
    }

    @Override
    public Field<UUID> field2() {
        return EuroMarket_6MonthEur.EURO_MARKET_6_MONTH_EUR.GLOBAL_ID;
    }

    @Override
    public Field<Double> field3() {
        return EuroMarket_6MonthEur.EURO_MARKET_6_MONTH_EUR.VALUE;
    }

    @Override
    public Field<LocalDate> field4() {
        return EuroMarket_6MonthEur.EURO_MARKET_6_MONTH_EUR.DATE;
    }

    @Override
    public Field<LocalDateTime> field5() {
        return EuroMarket_6MonthEur.EURO_MARKET_6_MONTH_EUR.CREATED;
    }

    @Override
    public Field<LocalDateTime> field6() {
        return EuroMarket_6MonthEur.EURO_MARKET_6_MONTH_EUR.UPDATED;
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
    public EuroMarket_6MonthEurRecord value1(Integer value) {
        setId(value);
        return this;
    }

    @Override
    public EuroMarket_6MonthEurRecord value2(UUID value) {
        setGlobalId(value);
        return this;
    }

    @Override
    public EuroMarket_6MonthEurRecord value3(Double value) {
        setValue(value);
        return this;
    }

    @Override
    public EuroMarket_6MonthEurRecord value4(LocalDate value) {
        setDate(value);
        return this;
    }

    @Override
    public EuroMarket_6MonthEurRecord value5(LocalDateTime value) {
        setCreated(value);
        return this;
    }

    @Override
    public EuroMarket_6MonthEurRecord value6(LocalDateTime value) {
        setUpdated(value);
        return this;
    }

    @Override
    public EuroMarket_6MonthEurRecord values(Integer value1, UUID value2, Double value3, LocalDate value4, LocalDateTime value5, LocalDateTime value6) {
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
     * Create a detached EuroMarket_6MonthEurRecord
     */
    public EuroMarket_6MonthEurRecord() {
        super(EuroMarket_6MonthEur.EURO_MARKET_6_MONTH_EUR);
    }

    /**
     * Create a detached, initialised EuroMarket_6MonthEurRecord
     */
    public EuroMarket_6MonthEurRecord(Integer id, UUID globalId, Double value, LocalDate date, LocalDateTime created, LocalDateTime updated) {
        super(EuroMarket_6MonthEur.EURO_MARKET_6_MONTH_EUR);

        setId(id);
        setGlobalId(globalId);
        setValue(value);
        setDate(date);
        setCreated(created);
        setUpdated(updated);
        resetChangedOnNotNull();
    }
}
