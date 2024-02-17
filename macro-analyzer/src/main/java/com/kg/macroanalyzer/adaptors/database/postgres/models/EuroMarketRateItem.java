package com.kg.macroanalyzer.adaptors.database.postgres.models;

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

}
