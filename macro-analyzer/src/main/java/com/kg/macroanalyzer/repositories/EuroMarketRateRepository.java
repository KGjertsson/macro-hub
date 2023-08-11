package com.kg.macroanalyzer.repositories;

import com.kg.macroanalyzer.models.EuroMarketRateItem;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

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

    public List<EuroMarketRateItem> getEuroMarketRate5YearUsa() {
        return this.dslContext.select()
                .from(EURO_MARKET_5_YEAR_EUR)
                .fetch()
                .map(EuroMarketRateItem::ofEuroMarketRate5YearUsa);
    }

    public List<EuroMarketRateItem> getEuroMarketRate5YearFrance() {
        return this.dslContext.select()
                .from(EURO_MARKET_5_YEAR_FRANCE)
                .fetch()
                .map(EuroMarketRateItem::ofEuroMarketRate5YearFrance);
    }

    public List<EuroMarketRateItem> getEuroMarketRate5YearGermany() {
        return this.dslContext.select()
                .from(EURO_MARKET_5_YEAR_FRANCE)
                .fetch()
                .map(EuroMarketRateItem::ofEuroMarketRate5YearGermany);
    }

    public List<EuroMarketRateItem> getEuroMarketRate5YearHolland() {
        return this.dslContext.select()
                .from(EURO_MARKET_5_YEAR_FRANCE)
                .fetch()
                .map(EuroMarketRateItem::ofEuroMarketRate5YearNetherlands);
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

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void insertEuroMarketRate3MonthDenmark(List<EuroMarketRateItem> scraped) {
        if (!scraped.isEmpty()) {
            final var insertQuery = dslContext.batch(
                    dslContext.insertInto(
                            EURO_MARKET_3_MONTH_DENMARK,
                            EURO_MARKET_3_MONTH_DENMARK.GLOBAL_ID,
                            EURO_MARKET_3_MONTH_DENMARK.VALUE,
                            EURO_MARKET_3_MONTH_DENMARK.DATE
                    ).values(DSL.val((UUID) null), DSL.val(0.0), DSL.val(LocalDate.MIN))
            );
            scraped.forEach(exchangeRateUsdSek -> insertQuery.bind(
                    UUID.randomUUID(),
                    exchangeRateUsdSek.value(),
                    exchangeRateUsdSek.date()
            ));

            insertQuery.execute();
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void insertEuroMarketRate3MonthEur(List<EuroMarketRateItem> scraped) {
        if (!scraped.isEmpty()) {
            final var insertQuery = dslContext.batch(
                    dslContext.insertInto(
                            EURO_MARKET_3_MONTH_EUR,
                            EURO_MARKET_3_MONTH_EUR.GLOBAL_ID,
                            EURO_MARKET_3_MONTH_EUR.VALUE,
                            EURO_MARKET_3_MONTH_EUR.DATE
                    ).values(DSL.val((UUID) null), DSL.val(0.0), DSL.val(LocalDate.MIN))
            );
            scraped.forEach(exchangeRateUsdSek -> insertQuery.bind(
                    UUID.randomUUID(),
                    exchangeRateUsdSek.value(),
                    exchangeRateUsdSek.date()
            ));

            insertQuery.execute();
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void insertEuroMarketRate3MonthGB(List<EuroMarketRateItem> scraped) {
        if (!scraped.isEmpty()) {
            final var insertQuery = dslContext.batch(
                    dslContext.insertInto(
                            EURO_MARKET_3_MONTH_GB,
                            EURO_MARKET_3_MONTH_GB.GLOBAL_ID,
                            EURO_MARKET_3_MONTH_GB.VALUE,
                            EURO_MARKET_3_MONTH_GB.DATE
                    ).values(DSL.val((UUID) null), DSL.val(0.0), DSL.val(LocalDate.MIN))
            );
            scraped.forEach(exchangeRateUsdSek -> insertQuery.bind(
                    UUID.randomUUID(),
                    exchangeRateUsdSek.value(),
                    exchangeRateUsdSek.date()
            ));

            insertQuery.execute();
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void insertEuroMarketRate3MonthJapan(List<EuroMarketRateItem> scraped) {
        if (!scraped.isEmpty()) {
            final var insertQuery = dslContext.batch(
                    dslContext.insertInto(
                            EURO_MARKET_3_MONTH_JAPAN,
                            EURO_MARKET_3_MONTH_JAPAN.GLOBAL_ID,
                            EURO_MARKET_3_MONTH_JAPAN.VALUE,
                            EURO_MARKET_3_MONTH_JAPAN.DATE
                    ).values(DSL.val((UUID) null), DSL.val(0.0), DSL.val(LocalDate.MIN))
            );
            scraped.forEach(exchangeRateUsdSek -> insertQuery.bind(
                    UUID.randomUUID(),
                    exchangeRateUsdSek.value(),
                    exchangeRateUsdSek.date()
            ));

            insertQuery.execute();
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void insertEuroMarketRate3MonthNorway(List<EuroMarketRateItem> scraped) {
        if (!scraped.isEmpty()) {
            final var insertQuery = dslContext.batch(
                    dslContext.insertInto(
                            EURO_MARKET_3_MONTH_NORWAY,
                            EURO_MARKET_3_MONTH_NORWAY.GLOBAL_ID,
                            EURO_MARKET_3_MONTH_NORWAY.VALUE,
                            EURO_MARKET_3_MONTH_NORWAY.DATE
                    ).values(DSL.val((UUID) null), DSL.val(0.0), DSL.val(LocalDate.MIN))
            );
            scraped.forEach(exchangeRateUsdSek -> insertQuery.bind(
                    UUID.randomUUID(),
                    exchangeRateUsdSek.value(),
                    exchangeRateUsdSek.date()
            ));

            insertQuery.execute();
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void insertEuroMarketRate3MonthUsa(List<EuroMarketRateItem> scraped) {
        if (!scraped.isEmpty()) {
            final var insertQuery = dslContext.batch(
                    dslContext.insertInto(
                            EURO_MARKET_3_MONTH_USA,
                            EURO_MARKET_3_MONTH_USA.GLOBAL_ID,
                            EURO_MARKET_3_MONTH_USA.VALUE,
                            EURO_MARKET_3_MONTH_USA.DATE
                    ).values(DSL.val((UUID) null), DSL.val(0.0), DSL.val(LocalDate.MIN))
            );
            scraped.forEach(exchangeRateUsdSek -> insertQuery.bind(
                    UUID.randomUUID(),
                    exchangeRateUsdSek.value(),
                    exchangeRateUsdSek.date()
            ));

            insertQuery.execute();
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void insertEuroMarketRate6MonthDenmark(List<EuroMarketRateItem> scraped) {
        if (!scraped.isEmpty()) {
            final var insertQuery = dslContext.batch(
                    dslContext.insertInto(
                            EURO_MARKET_6_MONTH_DENMARK,
                            EURO_MARKET_6_MONTH_DENMARK.GLOBAL_ID,
                            EURO_MARKET_6_MONTH_DENMARK.VALUE,
                            EURO_MARKET_6_MONTH_DENMARK.DATE
                    ).values(DSL.val((UUID) null), DSL.val(0.0), DSL.val(LocalDate.MIN))
            );
            scraped.forEach(exchangeRateUsdSek -> insertQuery.bind(
                    UUID.randomUUID(),
                    exchangeRateUsdSek.value(),
                    exchangeRateUsdSek.date()
            ));

            insertQuery.execute();
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void insertEuroMarketRate6MonthEur(List<EuroMarketRateItem> scraped) {
        if (!scraped.isEmpty()) {
            final var insertQuery = dslContext.batch(
                    dslContext.insertInto(
                            EURO_MARKET_6_MONTH_EUR,
                            EURO_MARKET_6_MONTH_EUR.GLOBAL_ID,
                            EURO_MARKET_6_MONTH_EUR.VALUE,
                            EURO_MARKET_6_MONTH_EUR.DATE
                    ).values(DSL.val((UUID) null), DSL.val(0.0), DSL.val(LocalDate.MIN))
            );
            scraped.forEach(exchangeRateUsdSek -> insertQuery.bind(
                    UUID.randomUUID(),
                    exchangeRateUsdSek.value(),
                    exchangeRateUsdSek.date()
            ));

            insertQuery.execute();
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void insertEuroMarketRate6MonthGB(List<EuroMarketRateItem> scraped) {
        if (!scraped.isEmpty()) {
            final var insertQuery = dslContext.batch(
                    dslContext.insertInto(
                            EURO_MARKET_6_MONTH_GB,
                            EURO_MARKET_6_MONTH_GB.GLOBAL_ID,
                            EURO_MARKET_6_MONTH_GB.VALUE,
                            EURO_MARKET_6_MONTH_GB.DATE
                    ).values(DSL.val((UUID) null), DSL.val(0.0), DSL.val(LocalDate.MIN))
            );
            scraped.forEach(exchangeRateUsdSek -> insertQuery.bind(
                    UUID.randomUUID(),
                    exchangeRateUsdSek.value(),
                    exchangeRateUsdSek.date()
            ));

            insertQuery.execute();
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void insertEuroMarketRate6MonthJapan(List<EuroMarketRateItem> scraped) {
        if (!scraped.isEmpty()) {
            final var insertQuery = dslContext.batch(
                    dslContext.insertInto(
                            EURO_MARKET_6_MONTH_JAPAN,
                            EURO_MARKET_6_MONTH_JAPAN.GLOBAL_ID,
                            EURO_MARKET_6_MONTH_JAPAN.VALUE,
                            EURO_MARKET_6_MONTH_JAPAN.DATE
                    ).values(DSL.val((UUID) null), DSL.val(0.0), DSL.val(LocalDate.MIN))
            );
            scraped.forEach(exchangeRateUsdSek -> insertQuery.bind(
                    UUID.randomUUID(),
                    exchangeRateUsdSek.value(),
                    exchangeRateUsdSek.date()
            ));

            insertQuery.execute();
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void insertEuroMarketRate6MonthNorway(List<EuroMarketRateItem> scraped) {
        if (!scraped.isEmpty()) {
            final var insertQuery = dslContext.batch(
                    dslContext.insertInto(
                            EURO_MARKET_6_MONTH_NORWAY,
                            EURO_MARKET_6_MONTH_NORWAY.GLOBAL_ID,
                            EURO_MARKET_6_MONTH_NORWAY.VALUE,
                            EURO_MARKET_6_MONTH_NORWAY.DATE
                    ).values(DSL.val((UUID) null), DSL.val(0.0), DSL.val(LocalDate.MIN))
            );
            scraped.forEach(exchangeRateUsdSek -> insertQuery.bind(
                    UUID.randomUUID(),
                    exchangeRateUsdSek.value(),
                    exchangeRateUsdSek.date()
            ));

            insertQuery.execute();
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void insertEuroMarketRate6MonthUsa(List<EuroMarketRateItem> scraped) {
        if (!scraped.isEmpty()) {
            final var insertQuery = dslContext.batch(
                    dslContext.insertInto(
                            EURO_MARKET_6_MONTH_USA,
                            EURO_MARKET_6_MONTH_USA.GLOBAL_ID,
                            EURO_MARKET_6_MONTH_USA.VALUE,
                            EURO_MARKET_6_MONTH_USA.DATE
                    ).values(DSL.val((UUID) null), DSL.val(0.0), DSL.val(LocalDate.MIN))
            );
            scraped.forEach(exchangeRateUsdSek -> insertQuery.bind(
                    UUID.randomUUID(),
                    exchangeRateUsdSek.value(),
                    exchangeRateUsdSek.date()
            ));

            insertQuery.execute();
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void insertEuroMarketRate5YearEur(List<EuroMarketRateItem> scraped) {
        if (!scraped.isEmpty()) {
            final var insertQuery = dslContext.batch(
                    dslContext.insertInto(
                            EURO_MARKET_5_YEAR_EUR,
                            EURO_MARKET_5_YEAR_EUR.GLOBAL_ID,
                            EURO_MARKET_5_YEAR_EUR.VALUE,
                            EURO_MARKET_5_YEAR_EUR.DATE
                    ).values(DSL.val((UUID) null), DSL.val(0.0), DSL.val(LocalDate.MIN))
            );
            scraped.forEach(exchangeRateUsdSek -> insertQuery.bind(
                    UUID.randomUUID(),
                    exchangeRateUsdSek.value(),
                    exchangeRateUsdSek.date()
            ));

            insertQuery.execute();
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void insertEuroMarketRate5YearGB(List<EuroMarketRateItem> scraped) {
        if (!scraped.isEmpty()) {
            final var insertQuery = dslContext.batch(
                    dslContext.insertInto(
                            EURO_MARKET_5_YEAR_GB,
                            EURO_MARKET_5_YEAR_GB.GLOBAL_ID,
                            EURO_MARKET_5_YEAR_GB.VALUE,
                            EURO_MARKET_5_YEAR_GB.DATE
                    ).values(DSL.val((UUID) null), DSL.val(0.0), DSL.val(LocalDate.MIN))
            );
            scraped.forEach(exchangeRateUsdSek -> insertQuery.bind(
                    UUID.randomUUID(),
                    exchangeRateUsdSek.value(),
                    exchangeRateUsdSek.date()
            ));

            insertQuery.execute();
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void insertEuroMarketRate5YearJapan(List<EuroMarketRateItem> scraped) {
        if (!scraped.isEmpty()) {
            final var insertQuery = dslContext.batch(
                    dslContext.insertInto(
                            EURO_MARKET_5_YEAR_JAPAN,
                            EURO_MARKET_5_YEAR_JAPAN.GLOBAL_ID,
                            EURO_MARKET_5_YEAR_JAPAN.VALUE,
                            EURO_MARKET_5_YEAR_JAPAN.DATE
                    ).values(DSL.val((UUID) null), DSL.val(0.0), DSL.val(LocalDate.MIN))
            );
            scraped.forEach(exchangeRateUsdSek -> insertQuery.bind(
                    UUID.randomUUID(),
                    exchangeRateUsdSek.value(),
                    exchangeRateUsdSek.date()
            ));

            insertQuery.execute();
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void insertEuroMarketRate5YearUsa(List<EuroMarketRateItem> scraped) {
        if (!scraped.isEmpty()) {
            final var insertQuery = dslContext.batch(
                    dslContext.insertInto(
                            EURO_MARKET_5_YEAR_USA,
                            EURO_MARKET_5_YEAR_USA.GLOBAL_ID,
                            EURO_MARKET_5_YEAR_USA.VALUE,
                            EURO_MARKET_5_YEAR_USA.DATE
                    ).values(DSL.val((UUID) null), DSL.val(0.0), DSL.val(LocalDate.MIN))
            );
            scraped.forEach(exchangeRateUsdSek -> insertQuery.bind(
                    UUID.randomUUID(),
                    exchangeRateUsdSek.value(),
                    exchangeRateUsdSek.date()
            ));

            insertQuery.execute();
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void insertEuroMarketRate5YearFrance(List<EuroMarketRateItem> scraped) {
        if (!scraped.isEmpty()) {
            final var insertQuery = dslContext.batch(
                    dslContext.insertInto(
                            EURO_MARKET_5_YEAR_FRANCE,
                            EURO_MARKET_5_YEAR_FRANCE.GLOBAL_ID,
                            EURO_MARKET_5_YEAR_FRANCE.VALUE,
                            EURO_MARKET_5_YEAR_FRANCE.DATE
                    ).values(DSL.val((UUID) null), DSL.val(0.0), DSL.val(LocalDate.MIN))
            );
            scraped.forEach(exchangeRateUsdSek -> insertQuery.bind(
                    UUID.randomUUID(),
                    exchangeRateUsdSek.value(),
                    exchangeRateUsdSek.date()
            ));

            insertQuery.execute();
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void insertEuroMarketRate5YearHolland(List<EuroMarketRateItem> scraped) {
        if (!scraped.isEmpty()) {
            final var insertQuery = dslContext.batch(
                    dslContext.insertInto(
                            EURO_MARKET_5_YEAR_NETHERLANDS,
                            EURO_MARKET_5_YEAR_NETHERLANDS.GLOBAL_ID,
                            EURO_MARKET_5_YEAR_NETHERLANDS.VALUE,
                            EURO_MARKET_5_YEAR_NETHERLANDS.DATE
                    ).values(DSL.val((UUID) null), DSL.val(0.0), DSL.val(LocalDate.MIN))
            );
            scraped.forEach(exchangeRateUsdSek -> insertQuery.bind(
                    UUID.randomUUID(),
                    exchangeRateUsdSek.value(),
                    exchangeRateUsdSek.date()
            ));

            insertQuery.execute();
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void insertEuroMarketRate5YearGermany(List<EuroMarketRateItem> scraped) {
        if (!scraped.isEmpty()) {
            final var insertQuery = dslContext.batch(
                    dslContext.insertInto(
                            EURO_MARKET_5_YEAR_GERMANY,
                            EURO_MARKET_5_YEAR_GERMANY.GLOBAL_ID,
                            EURO_MARKET_5_YEAR_GERMANY.VALUE,
                            EURO_MARKET_5_YEAR_GERMANY.DATE
                    ).values(DSL.val((UUID) null), DSL.val(0.0), DSL.val(LocalDate.MIN))
            );
            scraped.forEach(exchangeRateUsdSek -> insertQuery.bind(
                    UUID.randomUUID(),
                    exchangeRateUsdSek.value(),
                    exchangeRateUsdSek.date()
            ));

            insertQuery.execute();
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void insertEuroMarketRate10YearUsa(List<EuroMarketRateItem> scraped) {
        if (!scraped.isEmpty()) {
            final var insertQuery = dslContext.batch(
                    dslContext.insertInto(
                            EURO_MARKET_10_YEAR_USA,
                            EURO_MARKET_10_YEAR_USA.GLOBAL_ID,
                            EURO_MARKET_10_YEAR_USA.VALUE,
                            EURO_MARKET_10_YEAR_USA.DATE
                    ).values(DSL.val((UUID) null), DSL.val(0.0), DSL.val(LocalDate.MIN))
            );
            scraped.forEach(exchangeRateUsdSek -> insertQuery.bind(
                    UUID.randomUUID(),
                    exchangeRateUsdSek.value(),
                    exchangeRateUsdSek.date()
            ));

            insertQuery.execute();
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void insertEuroMarketRate10YearNorway(List<EuroMarketRateItem> scraped) {
        if (!scraped.isEmpty()) {
            final var insertQuery = dslContext.batch(
                    dslContext.insertInto(
                            EURO_MARKET_10_YEAR_NORWAY,
                            EURO_MARKET_10_YEAR_NORWAY.GLOBAL_ID,
                            EURO_MARKET_10_YEAR_NORWAY.VALUE,
                            EURO_MARKET_10_YEAR_NORWAY.DATE
                    ).values(DSL.val((UUID) null), DSL.val(0.0), DSL.val(LocalDate.MIN))
            );
            scraped.forEach(exchangeRateUsdSek -> insertQuery.bind(
                    UUID.randomUUID(),
                    exchangeRateUsdSek.value(),
                    exchangeRateUsdSek.date()
            ));

            insertQuery.execute();
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void insertEuroMarketRate10YearHolland(List<EuroMarketRateItem> scraped) {
        if (!scraped.isEmpty()) {
            final var insertQuery = dslContext.batch(
                    dslContext.insertInto(
                            EURO_MARKET_10_YEAR_NETHERLANDS,
                            EURO_MARKET_10_YEAR_NETHERLANDS.GLOBAL_ID,
                            EURO_MARKET_10_YEAR_NETHERLANDS.VALUE,
                            EURO_MARKET_10_YEAR_NETHERLANDS.DATE
                    ).values(DSL.val((UUID) null), DSL.val(0.0), DSL.val(LocalDate.MIN))
            );
            scraped.forEach(exchangeRateUsdSek -> insertQuery.bind(
                    UUID.randomUUID(),
                    exchangeRateUsdSek.value(),
                    exchangeRateUsdSek.date()
            ));

            insertQuery.execute();
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void insertEuroMarketRate10YearJapan(List<EuroMarketRateItem> scraped) {
        if (!scraped.isEmpty()) {
            final var insertQuery = dslContext.batch(
                    dslContext.insertInto(
                            EURO_MARKET_10_YEAR_JAPAN,
                            EURO_MARKET_10_YEAR_JAPAN.GLOBAL_ID,
                            EURO_MARKET_10_YEAR_JAPAN.VALUE,
                            EURO_MARKET_10_YEAR_JAPAN.DATE
                    ).values(DSL.val((UUID) null), DSL.val(0.0), DSL.val(LocalDate.MIN))
            );
            scraped.forEach(exchangeRateUsdSek -> insertQuery.bind(
                    UUID.randomUUID(),
                    exchangeRateUsdSek.value(),
                    exchangeRateUsdSek.date()
            ));

            insertQuery.execute();
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void insertEuroMarketRate10YearGermany(List<EuroMarketRateItem> scraped) {
        if (!scraped.isEmpty()) {
            final var insertQuery = dslContext.batch(
                    dslContext.insertInto(
                            EURO_MARKET_10_YEAR_GERMANY,
                            EURO_MARKET_10_YEAR_GERMANY.GLOBAL_ID,
                            EURO_MARKET_10_YEAR_GERMANY.VALUE,
                            EURO_MARKET_10_YEAR_GERMANY.DATE
                    ).values(DSL.val((UUID) null), DSL.val(0.0), DSL.val(LocalDate.MIN))
            );
            scraped.forEach(exchangeRateUsdSek -> insertQuery.bind(
                    UUID.randomUUID(),
                    exchangeRateUsdSek.value(),
                    exchangeRateUsdSek.date()
            ));

            insertQuery.execute();
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void insertEuroMarketRate10YearGB(List<EuroMarketRateItem> scraped) {
        if (!scraped.isEmpty()) {
            final var insertQuery = dslContext.batch(
                    dslContext.insertInto(
                            EURO_MARKET_10_YEAR_GB,
                            EURO_MARKET_10_YEAR_GB.GLOBAL_ID,
                            EURO_MARKET_10_YEAR_GB.VALUE,
                            EURO_MARKET_10_YEAR_GB.DATE
                    ).values(DSL.val((UUID) null), DSL.val(0.0), DSL.val(LocalDate.MIN))
            );
            scraped.forEach(exchangeRateUsdSek -> insertQuery.bind(
                    UUID.randomUUID(),
                    exchangeRateUsdSek.value(),
                    exchangeRateUsdSek.date()
            ));

            insertQuery.execute();
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void insertEuroMarketRate10YearFrance(List<EuroMarketRateItem> scraped) {
        if (!scraped.isEmpty()) {
            final var insertQuery = dslContext.batch(
                    dslContext.insertInto(
                            EURO_MARKET_10_YEAR_FRANCE,
                            EURO_MARKET_10_YEAR_FRANCE.GLOBAL_ID,
                            EURO_MARKET_10_YEAR_FRANCE.VALUE,
                            EURO_MARKET_10_YEAR_FRANCE.DATE
                    ).values(DSL.val((UUID) null), DSL.val(0.0), DSL.val(LocalDate.MIN))
            );
            scraped.forEach(exchangeRateUsdSek -> insertQuery.bind(
                    UUID.randomUUID(),
                    exchangeRateUsdSek.value(),
                    exchangeRateUsdSek.date()
            ));

            insertQuery.execute();
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void insertEuroMarketRate10YearFinland(List<EuroMarketRateItem> scraped) {
        if (!scraped.isEmpty()) {
            final var insertQuery = dslContext.batch(
                    dslContext.insertInto(
                            EURO_MARKET_10_YEAR_FINLAND,
                            EURO_MARKET_10_YEAR_FINLAND.GLOBAL_ID,
                            EURO_MARKET_10_YEAR_FINLAND.VALUE,
                            EURO_MARKET_10_YEAR_FINLAND.DATE
                    ).values(DSL.val((UUID) null), DSL.val(0.0), DSL.val(LocalDate.MIN))
            );
            scraped.forEach(exchangeRateUsdSek -> insertQuery.bind(
                    UUID.randomUUID(),
                    exchangeRateUsdSek.value(),
                    exchangeRateUsdSek.date()
            ));

            insertQuery.execute();
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void insertEuroMarketRate10YearEur(List<EuroMarketRateItem> scraped) {
        if (!scraped.isEmpty()) {
            final var insertQuery = dslContext.batch(
                    dslContext.insertInto(
                            EURO_MARKET_10_YEAR_EUR,
                            EURO_MARKET_10_YEAR_EUR.GLOBAL_ID,
                            EURO_MARKET_10_YEAR_EUR.VALUE,
                            EURO_MARKET_10_YEAR_EUR.DATE
                    ).values(DSL.val((UUID) null), DSL.val(0.0), DSL.val(LocalDate.MIN))
            );
            scraped.forEach(exchangeRateUsdSek -> insertQuery.bind(
                    UUID.randomUUID(),
                    exchangeRateUsdSek.value(),
                    exchangeRateUsdSek.date()
            ));

            insertQuery.execute();
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void insertEuroMarketRate10YearDenmark(List<EuroMarketRateItem> scraped) {
        if (!scraped.isEmpty()) {
            final var insertQuery = dslContext.batch(
                    dslContext.insertInto(
                            EURO_MARKET_10_YEAR_DENMARK,
                            EURO_MARKET_10_YEAR_DENMARK.GLOBAL_ID,
                            EURO_MARKET_10_YEAR_DENMARK.VALUE,
                            EURO_MARKET_10_YEAR_DENMARK.DATE
                    ).values(DSL.val((UUID) null), DSL.val(0.0), DSL.val(LocalDate.MIN))
            );
            scraped.forEach(exchangeRateUsdSek -> insertQuery.bind(
                    UUID.randomUUID(),
                    exchangeRateUsdSek.value(),
                    exchangeRateUsdSek.date()
            ));

            insertQuery.execute();
        }
    }

}
