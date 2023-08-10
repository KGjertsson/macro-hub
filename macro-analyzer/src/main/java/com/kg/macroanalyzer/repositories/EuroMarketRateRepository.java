package com.kg.macroanalyzer.repositories;

import com.kg.macroanalyzer.models.EuroMarketRateItem;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.kg.macroanalyzer.jooq.generated.Tables.*;


@Repository
public class EuroMarketRateRepository {

    private final DSLContext dslContext;

    @Autowired
    public EuroMarketRateRepository(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    public List<EuroMarketRateItem> getEuroMarketRate3MonthDenmark() {
        return this.dslContext.select()
                .from(EURO_MARKET_3_MONTH_DENMARK)
                .fetch()
                .map(EuroMarketRateItem::ofEuroMarketRate3MonthDenmark);
    }

    public List<EuroMarketRateItem> getEuroMarketRate3MonthEur() {
        return this.dslContext.select()
                .from(EURO_MARKET_3_MONTH_EUR)
                .fetch()
                .map(EuroMarketRateItem::ofEuroMarketRate3MonthEur);
    }

    public List<EuroMarketRateItem> getEuroMarketRate3MonthGB() {
        return this.dslContext.select()
                .from(EURO_MARKET_3_MONTH_GB)
                .fetch()
                .map(EuroMarketRateItem::ofEuroMarketRate3MonthGb);
    }

    public List<EuroMarketRateItem> getEuroMarketRate3MonthJapan() {
        return this.dslContext.select()
                .from(EURO_MARKET_3_MONTH_JAPAN)
                .fetch()
                .map(EuroMarketRateItem::ofEuroMarketRate3MonthJapan);
    }

    public List<EuroMarketRateItem> getEuroMarketRate3MonthNorway() {
        return this.dslContext.select()
                .from(EURO_MARKET_3_MONTH_NORWAY)
                .fetch()
                .map(EuroMarketRateItem::ofEuroMarketRate3MonthNorway);
    }

    public List<EuroMarketRateItem> getEuroMarketRate3MonthUsa() {
        return this.dslContext.select()
                .from(EURO_MARKET_3_MONTH_EUR)
                .fetch()
                .map(EuroMarketRateItem::ofEuroMarketRate3MonthUsa);
    }

    public List<EuroMarketRateItem> getEuroMarketRate6MonthDenmark() {
        return this.dslContext.select()
                .from(EURO_MARKET_6_MONTH_DENMARK)
                .fetch()
                .map(EuroMarketRateItem::ofEuroMarketRate6MonthDenmark);
    }

    public List<EuroMarketRateItem> getEuroMarketRate6MonthEur() {
        return this.dslContext.select()
                .from(EURO_MARKET_6_MONTH_EUR)
                .fetch()
                .map(EuroMarketRateItem::ofEuroMarketRate6MonthEur);
    }

    public List<EuroMarketRateItem> getEuroMarketRate6MonthGB() {
        return this.dslContext.select()
                .from(EURO_MARKET_6_MONTH_GB)
                .fetch()
                .map(EuroMarketRateItem::ofEuroMarketRate6MonthGb);
    }

    public List<EuroMarketRateItem> getEuroMarketRate6MonthJapan() {
        return this.dslContext.select()
                .from(EURO_MARKET_6_MONTH_JAPAN)
                .fetch()
                .map(EuroMarketRateItem::ofEuroMarketRate6MonthJapan);
    }

    public List<EuroMarketRateItem> getEuroMarketRate6MonthNorway() {
        return this.dslContext.select()
                .from(EURO_MARKET_6_MONTH_NORWAY)
                .fetch()
                .map(EuroMarketRateItem::ofEuroMarketRate6MonthNorway);
    }

    public List<EuroMarketRateItem> getEuroMarketRate6MonthUsa() {
        return this.dslContext.select()
                .from(EURO_MARKET_6_MONTH_EUR)
                .fetch()
                .map(EuroMarketRateItem::ofEuroMarketRate6MonthUsa);
    }

    public List<EuroMarketRateItem> getEuroMarketRate5YearDenmark() {
        return this.dslContext.select()
                .from(EURO_MARKET_5_YEAR_DENMARK)
                .fetch()
                .map(EuroMarketRateItem::ofEuroMarketRate5YearDenmark);
    }

    public List<EuroMarketRateItem> getEuroMarketRate5YearEur() {
        return this.dslContext.select()
                .from(EURO_MARKET_5_YEAR_EUR)
                .fetch()
                .map(EuroMarketRateItem::ofEuroMarketRate5YearEur);
    }

    public List<EuroMarketRateItem> getEuroMarketRate5YearGB() {
        return this.dslContext.select()
                .from(EURO_MARKET_5_YEAR_GB)
                .fetch()
                .map(EuroMarketRateItem::ofEuroMarketRate5YearGb);
    }

    public List<EuroMarketRateItem> getEuroMarketRate5YearJapan() {
        return this.dslContext.select()
                .from(EURO_MARKET_5_YEAR_JAPAN)
                .fetch()
                .map(EuroMarketRateItem::ofEuroMarketRate5YearJapan);
    }

    public List<EuroMarketRateItem> getEuroMarketRate5YearNorway() {
        return this.dslContext.select()
                .from(EURO_MARKET_5_YEAR_NORWAY)
                .fetch()
                .map(EuroMarketRateItem::ofEuroMarketRate5YearNorway);
    }

    public List<EuroMarketRateItem> getEuroMarketRate5YearUsa() {
        return this.dslContext.select()
                .from(EURO_MARKET_5_YEAR_EUR)
                .fetch()
                .map(EuroMarketRateItem::ofEuroMarketRate5YearUsa);
    }

    public List<EuroMarketRateItem> getEuroMarketRate10YearDenmark() {
        return this.dslContext.select()
                .from(EURO_MARKET_10_YEAR_DENMARK)
                .fetch()
                .map(EuroMarketRateItem::ofEuroMarketRate10YearDenmark);
    }

    public List<EuroMarketRateItem> getEuroMarketRate10YearEur() {
        return this.dslContext.select()
                .from(EURO_MARKET_10_YEAR_EUR)
                .fetch()
                .map(EuroMarketRateItem::ofEuroMarketRate10YearEur);
    }

    public List<EuroMarketRateItem> getEuroMarketRate10YearFinland() {
        return this.dslContext.select()
                .from(EURO_MARKET_10_YEAR_FINLAND)
                .fetch()
                .map(EuroMarketRateItem::ofEuroMarketRate10YearFinland);
    }

    public List<EuroMarketRateItem> getEuroMarketRate10YearFrance() {
        return this.dslContext.select()
                .from(EURO_MARKET_10_YEAR_FRANCE)
                .fetch()
                .map(EuroMarketRateItem::ofEuroMarketRate10YearFrance);
    }

    public List<EuroMarketRateItem> getEuroMarketRate10YearGB() {
        return this.dslContext.select()
                .from(EURO_MARKET_10_YEAR_GB)
                .fetch()
                .map(EuroMarketRateItem::ofEuroMarketRate10YearGb);
    }

    public List<EuroMarketRateItem> getEuroMarketRate10YearGermany() {
        return this.dslContext.select()
                .from(EURO_MARKET_10_YEAR_GERMANY)
                .fetch()
                .map(EuroMarketRateItem::ofEuroMarketRate10YearGermany);
    }

    public List<EuroMarketRateItem> getEuroMarketRate10YearJapan() {
        return this.dslContext.select()
                .from(EURO_MARKET_10_YEAR_JAPAN)
                .fetch()
                .map(EuroMarketRateItem::ofEuroMarketRate10YearJapan);
    }

    public List<EuroMarketRateItem> getEuroMarketRate10YearHolland() {
        return this.dslContext.select()
                .from(EURO_MARKET_10_YEAR_NETHERLANDS)
                .fetch()
                .map(EuroMarketRateItem::ofEuroMarketRate10YearNetherlands);
    }

    public List<EuroMarketRateItem> getEuroMarketRate10YearNorway() {
        return this.dslContext.select()
                .from(EURO_MARKET_10_YEAR_NORWAY)
                .fetch()
                .map(EuroMarketRateItem::ofEuroMarketRate10YearNorway);
    }

    public List<EuroMarketRateItem> getEuroMarketRate10YearUsa() {
        return this.dslContext.select()
                .from(EURO_MARKET_10_YEAR_USA)
                .fetch()
                .map(EuroMarketRateItem::ofEuroMarketRate10YearUsa);
    }

}
