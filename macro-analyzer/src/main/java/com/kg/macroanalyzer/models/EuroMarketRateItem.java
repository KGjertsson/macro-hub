package com.kg.macroanalyzer.models;

import lombok.Builder;
import org.jooq.Record;

import java.time.LocalDate;

import static com.kg.macroanalyzer.jooq.generated.Tables.*;


@Builder
public record EuroMarketRateItem(Double value, LocalDate date) {


    public static EuroMarketRateItem ofEuroMarketRate3MonthDenmark(Record record) {
        return EuroMarketRateItem.builder()
                .value(record.getValue(EURO_MARKET_3_MONTH_DENMARK.VALUE))
                .date(record.getValue(EURO_MARKET_3_MONTH_DENMARK.DATE))
                .build();
    }

    public static EuroMarketRateItem ofEuroMarketRate3MonthEur(Record record) {
        return EuroMarketRateItem.builder()
                .value(record.getValue(EURO_MARKET_3_MONTH_EUR.VALUE))
                .date(record.getValue(EURO_MARKET_3_MONTH_EUR.DATE))
                .build();
    }

    public static EuroMarketRateItem ofEuroMarketRate3MonthGb(Record record) {
        return EuroMarketRateItem.builder()
                .value(record.getValue(EURO_MARKET_3_MONTH_GB.VALUE))
                .date(record.getValue(EURO_MARKET_3_MONTH_GB.DATE))
                .build();
    }

    public static EuroMarketRateItem ofEuroMarketRate3MonthJapan(Record record) {
        return EuroMarketRateItem.builder()
                .value(record.getValue(EURO_MARKET_3_MONTH_JAPAN.VALUE))
                .date(record.getValue(EURO_MARKET_3_MONTH_JAPAN.DATE))
                .build();
    }

    public static EuroMarketRateItem ofEuroMarketRate3MonthNorway(Record record) {
        return EuroMarketRateItem.builder()
                .value(record.getValue(EURO_MARKET_3_MONTH_NORWAY.VALUE))
                .date(record.getValue(EURO_MARKET_3_MONTH_NORWAY.DATE))
                .build();
    }

    public static EuroMarketRateItem ofEuroMarketRate3MonthUsa(Record record) {
        return EuroMarketRateItem.builder()
                .value(record.getValue(EURO_MARKET_3_MONTH_USA.VALUE))
                .date(record.getValue(EURO_MARKET_3_MONTH_USA.DATE))
                .build();
    }

    public static EuroMarketRateItem ofEuroMarketRate6MonthDenmark(Record record) {
        return EuroMarketRateItem.builder()
                .value(record.getValue(EURO_MARKET_6_MONTH_DENMARK.VALUE))
                .date(record.getValue(EURO_MARKET_6_MONTH_DENMARK.DATE))
                .build();
    }

    public static EuroMarketRateItem ofEuroMarketRate6MonthEur(Record record) {
        return EuroMarketRateItem.builder()
                .value(record.getValue(EURO_MARKET_6_MONTH_EUR.VALUE))
                .date(record.getValue(EURO_MARKET_6_MONTH_EUR.DATE))
                .build();
    }

    public static EuroMarketRateItem ofEuroMarketRate6MonthGb(Record record) {
        return EuroMarketRateItem.builder()
                .value(record.getValue(EURO_MARKET_6_MONTH_GB.VALUE))
                .date(record.getValue(EURO_MARKET_6_MONTH_GB.DATE))
                .build();
    }

    public static EuroMarketRateItem ofEuroMarketRate6MonthJapan(Record record) {
        return EuroMarketRateItem.builder()
                .value(record.getValue(EURO_MARKET_6_MONTH_JAPAN.VALUE))
                .date(record.getValue(EURO_MARKET_6_MONTH_JAPAN.DATE))
                .build();
    }

    public static EuroMarketRateItem ofEuroMarketRate6MonthNorway(Record record) {
        return EuroMarketRateItem.builder()
                .value(record.getValue(EURO_MARKET_6_MONTH_NORWAY.VALUE))
                .date(record.getValue(EURO_MARKET_6_MONTH_NORWAY.DATE))
                .build();
    }

    public static EuroMarketRateItem ofEuroMarketRate6MonthUsa(Record record) {
        return EuroMarketRateItem.builder()
                .value(record.getValue(EURO_MARKET_6_MONTH_USA.VALUE))
                .date(record.getValue(EURO_MARKET_6_MONTH_USA.DATE))
                .build();
    }

    public static EuroMarketRateItem ofEuroMarketRate5YearEur(Record record) {
        return EuroMarketRateItem.builder()
                .value(record.getValue(EURO_MARKET_5_YEAR_EUR.VALUE))
                .date(record.getValue(EURO_MARKET_5_YEAR_EUR.DATE))
                .build();
    }

    public static EuroMarketRateItem ofEuroMarketRate5YearGb(Record record) {
        return EuroMarketRateItem.builder()
                .value(record.getValue(EURO_MARKET_5_YEAR_GB.VALUE))
                .date(record.getValue(EURO_MARKET_5_YEAR_GB.DATE))
                .build();
    }

    public static EuroMarketRateItem ofEuroMarketRate5YearJapan(Record record) {
        return EuroMarketRateItem.builder()
                .value(record.getValue(EURO_MARKET_5_YEAR_JAPAN.VALUE))
                .date(record.getValue(EURO_MARKET_5_YEAR_JAPAN.DATE))
                .build();
    }

    public static EuroMarketRateItem ofEuroMarketRate5YearUsa(Record record) {
        return EuroMarketRateItem.builder()
                .value(record.getValue(EURO_MARKET_5_YEAR_USA.VALUE))
                .date(record.getValue(EURO_MARKET_5_YEAR_USA.DATE))
                .build();
    }

    public static EuroMarketRateItem ofEuroMarketRate5YearFrance(Record record) {
        return EuroMarketRateItem.builder()
                .value(record.getValue(EURO_MARKET_5_YEAR_FRANCE.VALUE))
                .date(record.getValue(EURO_MARKET_5_YEAR_FRANCE.DATE))
                .build();
    }

    public static EuroMarketRateItem ofEuroMarketRate5YearGermany(Record record) {
        return EuroMarketRateItem.builder()
                .value(record.getValue(EURO_MARKET_5_YEAR_GERMANY.VALUE))
                .date(record.getValue(EURO_MARKET_5_YEAR_GERMANY.DATE))
                .build();
    }

    public static EuroMarketRateItem ofEuroMarketRate5YearNetherlands(Record record) {
        return EuroMarketRateItem.builder()
                .value(record.getValue(EURO_MARKET_5_YEAR_NETHERLANDS.VALUE))
                .date(record.getValue(EURO_MARKET_5_YEAR_NETHERLANDS.DATE))
                .build();
    }

    public static EuroMarketRateItem ofEuroMarketRate10YearDenmark(Record record) {
        return EuroMarketRateItem.builder()
                .value(record.getValue(EURO_MARKET_10_YEAR_DENMARK.VALUE))
                .date(record.getValue(EURO_MARKET_10_YEAR_DENMARK.DATE))
                .build();
    }

    public static EuroMarketRateItem ofEuroMarketRate10YearEur(Record record) {
        return EuroMarketRateItem.builder()
                .value(record.getValue(EURO_MARKET_10_YEAR_EUR.VALUE))
                .date(record.getValue(EURO_MARKET_10_YEAR_EUR.DATE))
                .build();
    }

    public static EuroMarketRateItem ofEuroMarketRate10YearFinland(Record record) {
        return EuroMarketRateItem.builder()
                .value(record.getValue(EURO_MARKET_10_YEAR_FINLAND.VALUE))
                .date(record.getValue(EURO_MARKET_10_YEAR_FINLAND.DATE))
                .build();
    }

    public static EuroMarketRateItem ofEuroMarketRate10YearFrance(Record record) {
        return EuroMarketRateItem.builder()
                .value(record.getValue(EURO_MARKET_10_YEAR_FRANCE.VALUE))
                .date(record.getValue(EURO_MARKET_10_YEAR_FRANCE.DATE))
                .build();
    }

    public static EuroMarketRateItem ofEuroMarketRate10YearGb(Record record) {
        return EuroMarketRateItem.builder()
                .value(record.getValue(EURO_MARKET_10_YEAR_GB.VALUE))
                .date(record.getValue(EURO_MARKET_10_YEAR_GB.DATE))
                .build();
    }

    public static EuroMarketRateItem ofEuroMarketRate10YearGermany(Record record) {
        return EuroMarketRateItem.builder()
                .value(record.getValue(EURO_MARKET_10_YEAR_GERMANY.VALUE))
                .date(record.getValue(EURO_MARKET_10_YEAR_GERMANY.DATE))
                .build();
    }

    public static EuroMarketRateItem ofEuroMarketRate10YearJapan(Record record) {
        return EuroMarketRateItem.builder()
                .value(record.getValue(EURO_MARKET_10_YEAR_JAPAN.VALUE))
                .date(record.getValue(EURO_MARKET_10_YEAR_JAPAN.DATE))
                .build();
    }

    public static EuroMarketRateItem ofEuroMarketRate10YearNetherlands(Record record) {
        return EuroMarketRateItem.builder()
                .value(record.getValue(EURO_MARKET_10_YEAR_NETHERLANDS.VALUE))
                .date(record.getValue(EURO_MARKET_10_YEAR_NETHERLANDS.DATE))
                .build();
    }

    public static EuroMarketRateItem ofEuroMarketRate10YearNorway(Record record) {
        return EuroMarketRateItem.builder()
                .value(record.getValue(EURO_MARKET_10_YEAR_NORWAY.VALUE))
                .date(record.getValue(EURO_MARKET_10_YEAR_NORWAY.DATE))
                .build();
    }

    public static EuroMarketRateItem ofEuroMarketRate10YearUsa(Record record) {
        return EuroMarketRateItem.builder()
                .value(record.getValue(EURO_MARKET_10_YEAR_USA.VALUE))
                .date(record.getValue(EURO_MARKET_10_YEAR_USA.DATE))
                .build();
    }

}
