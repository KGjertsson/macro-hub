package com.kg.macroanalyzer.repositories;

import com.kg.macroanalyzer.models.EuroMarketRateItem;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.kg.macroanalyzer.jooq.generated.Tables.*;


@Repository
public class EuroMarketRateRepository {

    private final DSLContext dslContext;

    @Autowired
    public EuroMarketRateRepository(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    public Supplier<List<EuroMarketRateItem>> getEuroMarketRate3MonthDenmark() {
        return () -> this.dslContext.select()
                .from(EURO_MARKET_3_MONTH_DENMARK)
                .fetch()
                .map(EuroMarketRateItem::ofEuroMarketRate3MonthDenmark);
    }

    public Supplier<List<EuroMarketRateItem>> getEuroMarketRate3MonthEur() {
        return () -> this.dslContext.select()
                .from(EURO_MARKET_3_MONTH_EUR)
                .fetch()
                .map(EuroMarketRateItem::ofEuroMarketRate3MonthEur);
    }

    public Supplier<List<EuroMarketRateItem>> getEuroMarketRate3MonthGB() {
        return () -> this.dslContext.select()
                .from(EURO_MARKET_3_MONTH_GB)
                .fetch()
                .map(EuroMarketRateItem::ofEuroMarketRate3MonthGb);
    }

    public Supplier<List<EuroMarketRateItem>> getEuroMarketRate3MonthJapan() {
        return () -> this.dslContext.select()
                .from(EURO_MARKET_3_MONTH_JAPAN)
                .fetch()
                .map(EuroMarketRateItem::ofEuroMarketRate3MonthJapan);
    }

    public Supplier<List<EuroMarketRateItem>> getEuroMarketRate3MonthNorway() {
        return () -> this.dslContext.select()
                .from(EURO_MARKET_3_MONTH_NORWAY)
                .fetch()
                .map(EuroMarketRateItem::ofEuroMarketRate3MonthNorway);
    }

    public Supplier<List<EuroMarketRateItem>> getEuroMarketRate3MonthUsa() {
        return () -> this.dslContext.select()
                .from(EURO_MARKET_3_MONTH_EUR)
                .fetch()
                .map(EuroMarketRateItem::ofEuroMarketRate3MonthUsa);
    }

    public Supplier<List<EuroMarketRateItem>> getEuroMarketRate6MonthDenmark() {
        return () -> this.dslContext.select()
                .from(EURO_MARKET_6_MONTH_DENMARK)
                .fetch()
                .map(EuroMarketRateItem::ofEuroMarketRate6MonthDenmark);
    }

    public Supplier<List<EuroMarketRateItem>> getEuroMarketRate6MonthEur() {
        return () -> this.dslContext.select()
                .from(EURO_MARKET_6_MONTH_EUR)
                .fetch()
                .map(EuroMarketRateItem::ofEuroMarketRate6MonthEur);
    }

    public Supplier<List<EuroMarketRateItem>> getEuroMarketRate6MonthGB() {
        return () -> this.dslContext.select()
                .from(EURO_MARKET_6_MONTH_GB)
                .fetch()
                .map(EuroMarketRateItem::ofEuroMarketRate6MonthGb);
    }

    public Supplier<List<EuroMarketRateItem>> getEuroMarketRate6MonthJapan() {
        return () -> this.dslContext.select()
                .from(EURO_MARKET_6_MONTH_JAPAN)
                .fetch()
                .map(EuroMarketRateItem::ofEuroMarketRate6MonthJapan);
    }

    public Supplier<List<EuroMarketRateItem>> getEuroMarketRate6MonthNorway() {
        return () -> this.dslContext.select()
                .from(EURO_MARKET_6_MONTH_NORWAY)
                .fetch()
                .map(EuroMarketRateItem::ofEuroMarketRate6MonthNorway);
    }

    public Supplier<List<EuroMarketRateItem>> getEuroMarketRate6MonthUsa() {
        return () -> this.dslContext.select()
                .from(EURO_MARKET_6_MONTH_EUR)
                .fetch()
                .map(EuroMarketRateItem::ofEuroMarketRate6MonthUsa);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public Function<List<EuroMarketRateItem>, Integer> insertEuroMarketRate3MonthDenmark() {
        return scraped -> {
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

                final var response = insertQuery.execute();

                return response.length;
            }

            return 0;
        };
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public Function<List<EuroMarketRateItem>, Integer> insertEuroMarketRate3MonthEur() {
        return scraped -> {
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
                final var response = insertQuery.execute();

                return response.length;
            }

            return 0;
        };
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public Function<List<EuroMarketRateItem>, Integer> insertEuroMarketRate3MonthGB() {
        return scraped -> {
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
                final var response = insertQuery.execute();

                return response.length;
            }

            return 0;
        };
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public Function<List<EuroMarketRateItem>, Integer> insertEuroMarketRate3MonthJapan() {
        return scraped -> {
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
                final var response = insertQuery.execute();

                return response.length;
            }

            return 0;
        };
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public Function<List<EuroMarketRateItem>, Integer> insertEuroMarketRate3MonthNorway() {
        return scraped -> {
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
                final var response = insertQuery.execute();

                return response.length;
            }

            return 0;
        };
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public Function<List<EuroMarketRateItem>, Integer> insertEuroMarketRate3MonthUsa() {
        return scraped -> {
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
                final var response = insertQuery.execute();

                return response.length;
            }

            return 0;
        };
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public Function<List<EuroMarketRateItem>, Integer> insertEuroMarketRate6MonthDenmark() {
        return scraped -> {
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
                final var response = insertQuery.execute();

                return response.length;
            }

            return 0;
        };
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public Function<List<EuroMarketRateItem>, Integer> insertEuroMarketRate6MonthEur() {
        return scraped -> {
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
                final var response = insertQuery.execute();

                return response.length;
            }

            return 0;
        };
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public Function<List<EuroMarketRateItem>, Integer> insertEuroMarketRate6MonthGB() {
        return scraped -> {
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
                final var response = insertQuery.execute();

                return response.length;
            }

            return 0;
        };
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public Function<List<EuroMarketRateItem>, Integer> insertEuroMarketRate6MonthJapan() {
        return scraped -> {
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
                final var response = insertQuery.execute();

                return response.length;
            }

            return 0;
        };
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public Function<List<EuroMarketRateItem>, Integer> insertEuroMarketRate6MonthNorway() {
        return scraped -> {
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
                final var response = insertQuery.execute();

                return response.length;
            }

            return 0;
        };
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public Function<List<EuroMarketRateItem>, Integer> insertEuroMarketRate6MonthUsa() {
        return scraped -> {
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
                final var response = insertQuery.execute();

                return response.length;
            }

            return 0;
        };
    }

}
