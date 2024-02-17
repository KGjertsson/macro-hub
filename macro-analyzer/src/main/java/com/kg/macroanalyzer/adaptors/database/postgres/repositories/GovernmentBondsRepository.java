package com.kg.macroanalyzer.adaptors.database.postgres.repositories;

import com.kg.macroanalyzer.adaptors.database.postgres.models.GovernmentBondItem;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.kg.macroanalyzer.jooq.generated.Tables.*;

@Slf4j
@Repository
public class GovernmentBondsRepository {

    private final DSLContext dslContext;

    public GovernmentBondsRepository(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    public Supplier<List<GovernmentBondItem>> swedishGovBond2YearReader() {
        return () -> dslContext.select()
                .from(SWEDISH_GOVERNMENT_BONDS_2_YEAR)
                .fetch()
                .map(GovernmentBondItem::ofSwedish2Month);
    }

    public Supplier<List<GovernmentBondItem>> swedishGovBond5YearReader() {
        return () -> dslContext.select()
                .from(SWEDISH_GOVERNMENT_BONDS_5_YEAR)
                .fetch()
                .map(GovernmentBondItem::ofSwedish5Month);
    }

    public Supplier<List<GovernmentBondItem>> swedishGovBond7YearReader() {
        return () -> dslContext.select()
                .from(SWEDISH_GOVERNMENT_BONDS_7_YEAR)
                .fetch()
                .map(GovernmentBondItem::ofSwedish7Month);
    }

    public Supplier<List<GovernmentBondItem>> swedishGovBond10YearReader() {
        return () -> dslContext.select()
                .from(SWEDISH_GOVERNMENT_BONDS_10_YEAR)
                .fetch()
                .map(GovernmentBondItem::ofSwedish10Month);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public Function<List<GovernmentBondItem>, Integer> swedishGovBond2YearWriter() {
        return governmentBondItemList -> {
            if (!governmentBondItemList.isEmpty()) {
                final var insertQuery = dslContext.batch(
                        dslContext.insertInto(
                                SWEDISH_GOVERNMENT_BONDS_2_YEAR,
                                SWEDISH_GOVERNMENT_BONDS_2_YEAR.GLOBAL_ID,
                                SWEDISH_GOVERNMENT_BONDS_2_YEAR.VALUE,
                                SWEDISH_GOVERNMENT_BONDS_2_YEAR.DATE
                        ).values(DSL.val((UUID) null), DSL.val(0.0), DSL.val(LocalDate.MIN))
                );
                governmentBondItemList.forEach(governmentBillItem -> insertQuery.bind(
                        UUID.randomUUID(),
                        governmentBillItem.value(),
                        governmentBillItem.date()
                ));
                final var result = insertQuery.execute();

                return result.length;
            }

            return 0;
        };
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public Function<List<GovernmentBondItem>, Integer> swedishGovBond5YearWriter() {
        return governmentBondItemList -> {
            if (!governmentBondItemList.isEmpty()) {
                final var insertQuery = dslContext.batch(
                        dslContext.insertInto(
                                SWEDISH_GOVERNMENT_BONDS_5_YEAR,
                                SWEDISH_GOVERNMENT_BONDS_5_YEAR.GLOBAL_ID,
                                SWEDISH_GOVERNMENT_BONDS_5_YEAR.VALUE,
                                SWEDISH_GOVERNMENT_BONDS_5_YEAR.DATE
                        ).values(DSL.val((UUID) null), DSL.val(0.0), DSL.val(LocalDate.MIN))
                );
                governmentBondItemList.forEach(governmentBillItem -> insertQuery.bind(
                        UUID.randomUUID(),
                        governmentBillItem.value(),
                        governmentBillItem.date()
                ));
                final var result = insertQuery.execute();

                return result.length;
            }

            return 0;
        };
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public Function<List<GovernmentBondItem>, Integer> swedishGovBond7YearWriter() {
        return governmentBondItemList -> {
            if (!governmentBondItemList.isEmpty()) {
                final var insertQuery = dslContext.batch(
                        dslContext.insertInto(
                                SWEDISH_GOVERNMENT_BONDS_7_YEAR,
                                SWEDISH_GOVERNMENT_BONDS_7_YEAR.GLOBAL_ID,
                                SWEDISH_GOVERNMENT_BONDS_7_YEAR.VALUE,
                                SWEDISH_GOVERNMENT_BONDS_7_YEAR.DATE
                        ).values(DSL.val((UUID) null), DSL.val(0.0), DSL.val(LocalDate.MIN))
                );
                governmentBondItemList.forEach(governmentBillItem -> insertQuery.bind(
                        UUID.randomUUID(),
                        governmentBillItem.value(),
                        governmentBillItem.date()
                ));
                final var result = insertQuery.execute();

                return result.length;
            }

            return 0;
        };
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public Function<List<GovernmentBondItem>, Integer> swedishGovBond10YearWriter() {
        return governmentBondItemList -> {
            if (!governmentBondItemList.isEmpty()) {
                final var insertQuery = dslContext.batch(
                        dslContext.insertInto(
                                SWEDISH_GOVERNMENT_BONDS_10_YEAR,
                                SWEDISH_GOVERNMENT_BONDS_10_YEAR.GLOBAL_ID,
                                SWEDISH_GOVERNMENT_BONDS_10_YEAR.VALUE,
                                SWEDISH_GOVERNMENT_BONDS_10_YEAR.DATE
                        ).values(DSL.val((UUID) null), DSL.val(0.0), DSL.val(LocalDate.MIN))
                );
                governmentBondItemList.forEach(governmentBillItem -> insertQuery.bind(
                        UUID.randomUUID(),
                        governmentBillItem.value(),
                        governmentBillItem.date()
                ));
                final var result = insertQuery.execute();

                return result.length;
            }

            return 0;
        };
    }

    public Supplier<List<GovernmentBondItem>> intGovBond5YearReaderEur() {
        return () -> this.dslContext.select()
                .from(INT_GOV_BOND_5_YEAR_EUR)
                .fetch()
                .map(GovernmentBondItem::ofIntGovBond5YearEur);
    }

    public Supplier<List<GovernmentBondItem>> intGovBond5YearReaderGB() {
        return () -> this.dslContext.select()
                .from(INT_GOV_BOND_5_YEAR_GB)
                .fetch()
                .map(GovernmentBondItem::ofIntGovBond5YearGb);
    }

    public Supplier<List<GovernmentBondItem>> intGovBond5YearReaderJapan() {
        return () -> this.dslContext.select()
                .from(INT_GOV_BOND_5_YEAR_JAPAN)
                .fetch()
                .map(GovernmentBondItem::ofIntGovBond5YearJapan);
    }

    public Supplier<List<GovernmentBondItem>> intGovBond5YearReaderUSA() {
        return () -> this.dslContext.select()
                .from(INT_GOV_BOND_5_YEAR_USA)
                .fetch()
                .map(GovernmentBondItem::ofIntGovBond5YearUsa);
    }

    public Supplier<List<GovernmentBondItem>> intGovBond5YearReaderFrance() {
        return () -> this.dslContext.select()
                .from(INT_GOV_BOND_5_YEAR_FRANCE)
                .fetch()
                .map(GovernmentBondItem::ofIntGovBond5YearFrance);
    }

    public Supplier<List<GovernmentBondItem>> intGovBond5YearReaderGermany() {
        return () -> this.dslContext.select()
                .from(INT_GOV_BOND_5_YEAR_GERMANY)
                .fetch()
                .map(GovernmentBondItem::ofIntGovBond5YearGermany);
    }

    public Supplier<List<GovernmentBondItem>> intGovBond5YearReaderHolland() {
        return () -> this.dslContext.select()
                .from(INT_GOV_BOND_5_YEAR_NETHERLANDS)
                .fetch()
                .map(GovernmentBondItem::ofIntGovBond5YearNetherlands);
    }

    public Supplier<List<GovernmentBondItem>> intGovBond10YearReaderDenmark() {
        return () -> this.dslContext.select()
                .from(INT_GOV_BOND_10_YEAR_DENMARK)
                .fetch()
                .map(GovernmentBondItem::ofIntGovBond10YearDenmark);
    }

    public Supplier<List<GovernmentBondItem>> intGovBond10YearReaderEur() {
        return () -> this.dslContext.select()
                .from(INT_GOV_BOND_10_YEAR_EUR)
                .fetch()
                .map(GovernmentBondItem::ofIntGovBond10YearEur);
    }

    public Supplier<List<GovernmentBondItem>> intGovBond10YearReaderFinland() {
        return () -> this.dslContext.select()
                .from(INT_GOV_BOND_10_YEAR_FINLAND)
                .fetch()
                .map(GovernmentBondItem::ofIntGovBond10YearFinland);
    }

    public Supplier<List<GovernmentBondItem>> intGovBond10YearReaderFrance() {
        return () -> this.dslContext.select()
                .from(INT_GOV_BOND_10_YEAR_FRANCE)
                .fetch()
                .map(GovernmentBondItem::ofIntGovBond10YearFrance);
    }

    public Supplier<List<GovernmentBondItem>> intGovBond10YearReaderGB() {
        return () -> this.dslContext.select()
                .from(INT_GOV_BOND_10_YEAR_GB)
                .fetch()
                .map(GovernmentBondItem::ofIntGovBond10YearGb);
    }

    public Supplier<List<GovernmentBondItem>> intGovBond10YearReaderGermany() {
        return () -> this.dslContext.select()
                .from(INT_GOV_BOND_10_YEAR_GERMANY)
                .fetch()
                .map(GovernmentBondItem::ofIntGovBond10YearGermany);
    }

    public Supplier<List<GovernmentBondItem>> intGovBond10YearReaderJapan() {
        return () -> this.dslContext.select()
                .from(INT_GOV_BOND_10_YEAR_JAPAN)
                .fetch()
                .map(GovernmentBondItem::ofIntGovBond10YearJapan);
    }

    public Supplier<List<GovernmentBondItem>> intGovBond10YearReaderHolland() {
        return () -> this.dslContext.select()
                .from(INT_GOV_BOND_10_YEAR_NETHERLANDS)
                .fetch()
                .map(GovernmentBondItem::ofIntGovBond10YearNetherlands);
    }

    public Supplier<List<GovernmentBondItem>> intGovBond10YearReaderNorway() {
        return () -> this.dslContext.select()
                .from(INT_GOV_BOND_10_YEAR_NORWAY)
                .fetch()
                .map(GovernmentBondItem::ofIntGovBond10YearNorway);
    }

    public Supplier<List<GovernmentBondItem>> intGovBond10YearReaderUSA() {
        return () -> this.dslContext.select()
                .from(INT_GOV_BOND_10_YEAR_USA)
                .fetch()
                .map(GovernmentBondItem::ofIntGovBond10YearUsa);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public Function<List<GovernmentBondItem>, Integer> intGovBond5YearWriterEur() {
        return scraped -> {
            if (!scraped.isEmpty()) {
                final var insertQuery = dslContext.batch(
                        dslContext.insertInto(
                                INT_GOV_BOND_5_YEAR_EUR,
                                INT_GOV_BOND_5_YEAR_EUR.GLOBAL_ID,
                                INT_GOV_BOND_5_YEAR_EUR.VALUE,
                                INT_GOV_BOND_5_YEAR_EUR.DATE
                        ).values(DSL.val((UUID) null), DSL.val(0.0), DSL.val(LocalDate.MIN))
                );
                scraped.forEach(exchangeRateUsdSek -> insertQuery.bind(
                        UUID.randomUUID(),
                        exchangeRateUsdSek.value(),
                        exchangeRateUsdSek.date()
                ));
                final var result = insertQuery.execute();

                return result.length;
            }

            return 0;
        };
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public Function<List<GovernmentBondItem>, Integer> intGovBond5YearWriterGB() {
        return scraped -> {
            if (!scraped.isEmpty()) {
                final var insertQuery = dslContext.batch(
                        dslContext.insertInto(
                                INT_GOV_BOND_5_YEAR_GB,
                                INT_GOV_BOND_5_YEAR_GB.GLOBAL_ID,
                                INT_GOV_BOND_5_YEAR_GB.VALUE,
                                INT_GOV_BOND_5_YEAR_GB.DATE
                        ).values(DSL.val((UUID) null), DSL.val(0.0), DSL.val(LocalDate.MIN))
                );
                scraped.forEach(exchangeRateUsdSek -> insertQuery.bind(
                        UUID.randomUUID(),
                        exchangeRateUsdSek.value(),
                        exchangeRateUsdSek.date()
                ));
                final var result = insertQuery.execute();

                return result.length;
            }

            return 0;
        };
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public Function<List<GovernmentBondItem>, Integer> intGovBond5YearWriterJapan() {
        return scraped -> {
            if (!scraped.isEmpty()) {
                final var insertQuery = dslContext.batch(
                        dslContext.insertInto(
                                INT_GOV_BOND_5_YEAR_JAPAN,
                                INT_GOV_BOND_5_YEAR_JAPAN.GLOBAL_ID,
                                INT_GOV_BOND_5_YEAR_JAPAN.VALUE,
                                INT_GOV_BOND_5_YEAR_JAPAN.DATE
                        ).values(DSL.val((UUID) null), DSL.val(0.0), DSL.val(LocalDate.MIN))
                );
                scraped.forEach(exchangeRateUsdSek -> insertQuery.bind(
                        UUID.randomUUID(),
                        exchangeRateUsdSek.value(),
                        exchangeRateUsdSek.date()
                ));
                final var result = insertQuery.execute();

                return result.length;
            }

            return 0;
        };
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public Function<List<GovernmentBondItem>, Integer> intGovBond5YearWriterUSA() {
        return scraped -> {
            if (!scraped.isEmpty()) {
                final var insertQuery = dslContext.batch(
                        dslContext.insertInto(
                                INT_GOV_BOND_5_YEAR_USA,
                                INT_GOV_BOND_5_YEAR_USA.GLOBAL_ID,
                                INT_GOV_BOND_5_YEAR_USA.VALUE,
                                INT_GOV_BOND_5_YEAR_USA.DATE
                        ).values(DSL.val((UUID) null), DSL.val(0.0), DSL.val(LocalDate.MIN))
                );
                scraped.forEach(exchangeRateUsdSek -> insertQuery.bind(
                        UUID.randomUUID(),
                        exchangeRateUsdSek.value(),
                        exchangeRateUsdSek.date()
                ));
                final var result = insertQuery.execute();

                return result.length;
            }

            return 0;
        };
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public Function<List<GovernmentBondItem>, Integer> intGovBond5YearWriterFrance() {
        return scraped -> {
            if (!scraped.isEmpty()) {
                final var insertQuery = dslContext.batch(
                        dslContext.insertInto(
                                INT_GOV_BOND_5_YEAR_FRANCE,
                                INT_GOV_BOND_5_YEAR_FRANCE.GLOBAL_ID,
                                INT_GOV_BOND_5_YEAR_FRANCE.VALUE,
                                INT_GOV_BOND_5_YEAR_FRANCE.DATE
                        ).values(DSL.val((UUID) null), DSL.val(0.0), DSL.val(LocalDate.MIN))
                );
                scraped.forEach(exchangeRateUsdSek -> insertQuery.bind(
                        UUID.randomUUID(),
                        exchangeRateUsdSek.value(),
                        exchangeRateUsdSek.date()
                ));
                final var result = insertQuery.execute();

                return result.length;
            }

            return 0;
        };
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public Function<List<GovernmentBondItem>, Integer> intGovBond5YearWriterHolland() {
        return scraped -> {
            if (!scraped.isEmpty()) {
                final var insertQuery = dslContext.batch(
                        dslContext.insertInto(
                                INT_GOV_BOND_5_YEAR_NETHERLANDS,
                                INT_GOV_BOND_5_YEAR_NETHERLANDS.GLOBAL_ID,
                                INT_GOV_BOND_5_YEAR_NETHERLANDS.VALUE,
                                INT_GOV_BOND_5_YEAR_NETHERLANDS.DATE
                        ).values(DSL.val((UUID) null), DSL.val(0.0), DSL.val(LocalDate.MIN))
                );
                scraped.forEach(exchangeRateUsdSek -> insertQuery.bind(
                        UUID.randomUUID(),
                        exchangeRateUsdSek.value(),
                        exchangeRateUsdSek.date()
                ));
                final var result = insertQuery.execute();

                return result.length;
            }

            return 0;
        };
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public Function<List<GovernmentBondItem>, Integer> intGovBond5YearWriterGermany() {
        return scraped -> {
            if (!scraped.isEmpty()) {
                final var insertQuery = dslContext.batch(
                        dslContext.insertInto(
                                INT_GOV_BOND_5_YEAR_GERMANY,
                                INT_GOV_BOND_5_YEAR_GERMANY.GLOBAL_ID,
                                INT_GOV_BOND_5_YEAR_GERMANY.VALUE,
                                INT_GOV_BOND_5_YEAR_GERMANY.DATE
                        ).values(DSL.val((UUID) null), DSL.val(0.0), DSL.val(LocalDate.MIN))
                );
                scraped.forEach(exchangeRateUsdSek -> insertQuery.bind(
                        UUID.randomUUID(),
                        exchangeRateUsdSek.value(),
                        exchangeRateUsdSek.date()
                ));
                final var result = insertQuery.execute();

                return result.length;
            }

            return 0;
        };
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public Function<List<GovernmentBondItem>, Integer> intGovBond10YearWriterUSA() {
        return scraped -> {
            if (!scraped.isEmpty()) {
                final var insertQuery = dslContext.batch(
                        dslContext.insertInto(
                                INT_GOV_BOND_10_YEAR_USA,
                                INT_GOV_BOND_10_YEAR_USA.GLOBAL_ID,
                                INT_GOV_BOND_10_YEAR_USA.VALUE,
                                INT_GOV_BOND_10_YEAR_USA.DATE
                        ).values(DSL.val((UUID) null), DSL.val(0.0), DSL.val(LocalDate.MIN))
                );
                scraped.forEach(exchangeRateUsdSek -> insertQuery.bind(
                        UUID.randomUUID(),
                        exchangeRateUsdSek.value(),
                        exchangeRateUsdSek.date()
                ));
                final var result = insertQuery.execute();

                return result.length;
            }

            return 0;
        };
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public Function<List<GovernmentBondItem>, Integer> intGovBond10YearWriterNorway() {
        return scraped -> {
            if (!scraped.isEmpty()) {
                final var insertQuery = dslContext.batch(
                        dslContext.insertInto(
                                INT_GOV_BOND_10_YEAR_NORWAY,
                                INT_GOV_BOND_10_YEAR_NORWAY.GLOBAL_ID,
                                INT_GOV_BOND_10_YEAR_NORWAY.VALUE,
                                INT_GOV_BOND_10_YEAR_NORWAY.DATE
                        ).values(DSL.val((UUID) null), DSL.val(0.0), DSL.val(LocalDate.MIN))
                );
                scraped.forEach(exchangeRateUsdSek -> insertQuery.bind(
                        UUID.randomUUID(),
                        exchangeRateUsdSek.value(),
                        exchangeRateUsdSek.date()
                ));
                final var result = insertQuery.execute();

                return result.length;
            }

            return 0;
        };
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public Function<List<GovernmentBondItem>, Integer> intGovBond10YearWriterHolland() {
        return scraped -> {
            if (!scraped.isEmpty()) {
                final var insertQuery = dslContext.batch(
                        dslContext.insertInto(
                                INT_GOV_BOND_10_YEAR_NETHERLANDS,
                                INT_GOV_BOND_10_YEAR_NETHERLANDS.GLOBAL_ID,
                                INT_GOV_BOND_10_YEAR_NETHERLANDS.VALUE,
                                INT_GOV_BOND_10_YEAR_NETHERLANDS.DATE
                        ).values(DSL.val((UUID) null), DSL.val(0.0), DSL.val(LocalDate.MIN))
                );
                scraped.forEach(exchangeRateUsdSek -> insertQuery.bind(
                        UUID.randomUUID(),
                        exchangeRateUsdSek.value(),
                        exchangeRateUsdSek.date()
                ));
                final var result = insertQuery.execute();

                return result.length;
            }

            return 0;
        };
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public Function<List<GovernmentBondItem>, Integer> intGovBond10YearWriterJapan() {
        return scraped -> {
            if (!scraped.isEmpty()) {
                final var insertQuery = dslContext.batch(
                        dslContext.insertInto(
                                INT_GOV_BOND_10_YEAR_JAPAN,
                                INT_GOV_BOND_10_YEAR_JAPAN.GLOBAL_ID,
                                INT_GOV_BOND_10_YEAR_JAPAN.VALUE,
                                INT_GOV_BOND_10_YEAR_JAPAN.DATE
                        ).values(DSL.val((UUID) null), DSL.val(0.0), DSL.val(LocalDate.MIN))
                );
                scraped.forEach(exchangeRateUsdSek -> insertQuery.bind(
                        UUID.randomUUID(),
                        exchangeRateUsdSek.value(),
                        exchangeRateUsdSek.date()
                ));
                final var result = insertQuery.execute();

                return result.length;
            }

            return 0;
        };
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public Function<List<GovernmentBondItem>, Integer> intGovBond10YearWriterGermany() {
        return scraped -> {
            if (!scraped.isEmpty()) {
                final var insertQuery = dslContext.batch(
                        dslContext.insertInto(
                                INT_GOV_BOND_10_YEAR_GERMANY,
                                INT_GOV_BOND_10_YEAR_GERMANY.GLOBAL_ID,
                                INT_GOV_BOND_10_YEAR_GERMANY.VALUE,
                                INT_GOV_BOND_10_YEAR_GERMANY.DATE
                        ).values(DSL.val((UUID) null), DSL.val(0.0), DSL.val(LocalDate.MIN))
                );
                scraped.forEach(exchangeRateUsdSek -> insertQuery.bind(
                        UUID.randomUUID(),
                        exchangeRateUsdSek.value(),
                        exchangeRateUsdSek.date()
                ));
                final var result = insertQuery.execute();

                return result.length;
            }

            return 0;
        };
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public Function<List<GovernmentBondItem>, Integer> intGovBond10YearWriterGB() {
        return scraped -> {
            if (!scraped.isEmpty()) {
                final var insertQuery = dslContext.batch(
                        dslContext.insertInto(
                                INT_GOV_BOND_10_YEAR_GB,
                                INT_GOV_BOND_10_YEAR_GB.GLOBAL_ID,
                                INT_GOV_BOND_10_YEAR_GB.VALUE,
                                INT_GOV_BOND_10_YEAR_GB.DATE
                        ).values(DSL.val((UUID) null), DSL.val(0.0), DSL.val(LocalDate.MIN))
                );
                scraped.forEach(exchangeRateUsdSek -> insertQuery.bind(
                        UUID.randomUUID(),
                        exchangeRateUsdSek.value(),
                        exchangeRateUsdSek.date()
                ));
                final var result = insertQuery.execute();

                return result.length;
            }

            return 0;
        };
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public Function<List<GovernmentBondItem>, Integer> intGovBond10YearWriterFrance() {
        return scraped -> {
            if (!scraped.isEmpty()) {
                final var insertQuery = dslContext.batch(
                        dslContext.insertInto(
                                INT_GOV_BOND_10_YEAR_FRANCE,
                                INT_GOV_BOND_10_YEAR_FRANCE.GLOBAL_ID,
                                INT_GOV_BOND_10_YEAR_FRANCE.VALUE,
                                INT_GOV_BOND_10_YEAR_FRANCE.DATE
                        ).values(DSL.val((UUID) null), DSL.val(0.0), DSL.val(LocalDate.MIN))
                );
                scraped.forEach(exchangeRateUsdSek -> insertQuery.bind(
                        UUID.randomUUID(),
                        exchangeRateUsdSek.value(),
                        exchangeRateUsdSek.date()
                ));
                final var result = insertQuery.execute();

                return result.length;
            }

            return 0;
        };
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public Function<List<GovernmentBondItem>, Integer> intGovBond10YearWriterFinland() {
        return scraped -> {
            if (!scraped.isEmpty()) {
                final var insertQuery = dslContext.batch(
                        dslContext.insertInto(
                                INT_GOV_BOND_10_YEAR_FINLAND,
                                INT_GOV_BOND_10_YEAR_FINLAND.GLOBAL_ID,
                                INT_GOV_BOND_10_YEAR_FINLAND.VALUE,
                                INT_GOV_BOND_10_YEAR_FINLAND.DATE
                        ).values(DSL.val((UUID) null), DSL.val(0.0), DSL.val(LocalDate.MIN))
                );
                scraped.forEach(exchangeRateUsdSek -> insertQuery.bind(
                        UUID.randomUUID(),
                        exchangeRateUsdSek.value(),
                        exchangeRateUsdSek.date()
                ));
                final var result = insertQuery.execute();

                return result.length;
            }

            return 0;
        };
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public Function<List<GovernmentBondItem>, Integer> intGovBond10YearWriterEur() {
        return scraped -> {
            if (!scraped.isEmpty()) {
                final var insertQuery = dslContext.batch(
                        dslContext.insertInto(
                                INT_GOV_BOND_10_YEAR_EUR,
                                INT_GOV_BOND_10_YEAR_EUR.GLOBAL_ID,
                                INT_GOV_BOND_10_YEAR_EUR.VALUE,
                                INT_GOV_BOND_10_YEAR_EUR.DATE
                        ).values(DSL.val((UUID) null), DSL.val(0.0), DSL.val(LocalDate.MIN))
                );
                scraped.forEach(exchangeRateUsdSek -> insertQuery.bind(
                        UUID.randomUUID(),
                        exchangeRateUsdSek.value(),
                        exchangeRateUsdSek.date()
                ));
                final var result = insertQuery.execute();

                return result.length;
            }

            return 0;
        };
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public Function<List<GovernmentBondItem>, Integer> intGovBond10YearWriterDenmark() {
        return scraped -> {
            if (!scraped.isEmpty()) {
                final var insertQuery = dslContext.batch(
                        dslContext.insertInto(
                                INT_GOV_BOND_10_YEAR_DENMARK,
                                INT_GOV_BOND_10_YEAR_DENMARK.GLOBAL_ID,
                                INT_GOV_BOND_10_YEAR_DENMARK.VALUE,
                                INT_GOV_BOND_10_YEAR_DENMARK.DATE
                        ).values(DSL.val((UUID) null), DSL.val(0.0), DSL.val(LocalDate.MIN))
                );
                scraped.forEach(exchangeRateUsdSek -> insertQuery.bind(
                        UUID.randomUUID(),
                        exchangeRateUsdSek.value(),
                        exchangeRateUsdSek.date()
                ));
                final var result = insertQuery.execute();

                return result.length;
            }

            return 0;
        };
    }

}
