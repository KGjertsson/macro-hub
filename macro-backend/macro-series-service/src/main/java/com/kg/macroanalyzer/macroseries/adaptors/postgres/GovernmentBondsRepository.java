package com.kg.macroanalyzer.macroseries.adaptors.postgres;

import com.kg.macroanalyzer.macroseries.domain.MacroPoint;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.kg.macroanalyzer.macroseries.jooq.generated.Tables.*;

@Slf4j
@Repository
public class GovernmentBondsRepository {

    private final DSLContext dslContext;

    public GovernmentBondsRepository(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    public Supplier<List<MacroPoint>> swedishGovBond2YearReader() {
        return () -> dslContext.select()
                .from(SWEDISH_GOVERNMENT_BONDS_2_YEAR)
                .fetch()
                .map(r ->
                        MacroPoint.builder()
                                .value(r.getValue(SWEDISH_GOVERNMENT_BONDS_2_YEAR.VALUE))
                                .date(r.getValue(SWEDISH_GOVERNMENT_BONDS_2_YEAR.DATE))
                                .build()
                );
    }

    public Supplier<List<MacroPoint>> swedishGovBond5YearReader() {
        return () -> dslContext.select()
                .from(SWEDISH_GOVERNMENT_BONDS_5_YEAR)
                .fetch()
                .map(r ->
                        MacroPoint.builder()
                                .value(r.getValue(SWEDISH_GOVERNMENT_BONDS_5_YEAR.VALUE))
                                .date(r.getValue(SWEDISH_GOVERNMENT_BONDS_5_YEAR.DATE))
                                .build()
                );
    }

    public Supplier<List<MacroPoint>> swedishGovBond7YearReader() {
        return () -> dslContext.select()
                .from(SWEDISH_GOVERNMENT_BONDS_7_YEAR)
                .fetch()
                .map(r ->
                        MacroPoint.builder()
                                .value(r.getValue(SWEDISH_GOVERNMENT_BONDS_7_YEAR.VALUE))
                                .date(r.getValue(SWEDISH_GOVERNMENT_BONDS_7_YEAR.DATE))
                                .build()
                );
    }

    public Supplier<List<MacroPoint>> swedishGovBond10YearReader() {
        return () -> dslContext.select()
                .from(SWEDISH_GOVERNMENT_BONDS_10_YEAR)
                .fetch()
                .map(r ->
                        MacroPoint.builder()
                                .value(r.getValue(SWEDISH_GOVERNMENT_BONDS_10_YEAR.VALUE))
                                .date(r.getValue(SWEDISH_GOVERNMENT_BONDS_10_YEAR.DATE))
                                .build()
                );
    }

    public Function<List<MacroPoint>, Integer> swedishGovBond2YearWriter() {
        return MacroPointUpsert.writer(
                dslContext,
                SWEDISH_GOVERNMENT_BONDS_2_YEAR,
                SWEDISH_GOVERNMENT_BONDS_2_YEAR.GLOBAL_ID,
                SWEDISH_GOVERNMENT_BONDS_2_YEAR.VALUE,
                SWEDISH_GOVERNMENT_BONDS_2_YEAR.DATE,
                SWEDISH_GOVERNMENT_BONDS_2_YEAR.UPDATED
        );
    }

    public Function<List<MacroPoint>, Integer> swedishGovBond5YearWriter() {
        return MacroPointUpsert.writer(
                dslContext,
                SWEDISH_GOVERNMENT_BONDS_5_YEAR,
                SWEDISH_GOVERNMENT_BONDS_5_YEAR.GLOBAL_ID,
                SWEDISH_GOVERNMENT_BONDS_5_YEAR.VALUE,
                SWEDISH_GOVERNMENT_BONDS_5_YEAR.DATE,
                SWEDISH_GOVERNMENT_BONDS_5_YEAR.UPDATED
        );
    }

    public Function<List<MacroPoint>, Integer> swedishGovBond7YearWriter() {
        return MacroPointUpsert.writer(
                dslContext,
                SWEDISH_GOVERNMENT_BONDS_7_YEAR,
                SWEDISH_GOVERNMENT_BONDS_7_YEAR.GLOBAL_ID,
                SWEDISH_GOVERNMENT_BONDS_7_YEAR.VALUE,
                SWEDISH_GOVERNMENT_BONDS_7_YEAR.DATE,
                SWEDISH_GOVERNMENT_BONDS_7_YEAR.UPDATED
        );
    }

    public Function<List<MacroPoint>, Integer> swedishGovBond10YearWriter() {
        return MacroPointUpsert.writer(
                dslContext,
                SWEDISH_GOVERNMENT_BONDS_10_YEAR,
                SWEDISH_GOVERNMENT_BONDS_10_YEAR.GLOBAL_ID,
                SWEDISH_GOVERNMENT_BONDS_10_YEAR.VALUE,
                SWEDISH_GOVERNMENT_BONDS_10_YEAR.DATE,
                SWEDISH_GOVERNMENT_BONDS_10_YEAR.UPDATED
        );
    }

    public Supplier<List<MacroPoint>> intGovBond5YearReaderEur() {
        return () -> this.dslContext.select()
                .from(INT_GOV_BOND_5_YEAR_EUR)
                .fetch()
                .map(r ->
                        MacroPoint.builder()
                                .value(r.getValue(INT_GOV_BOND_5_YEAR_EUR.VALUE))
                                .date(r.getValue(INT_GOV_BOND_5_YEAR_EUR.DATE))
                                .build()
                );
    }

    public Supplier<List<MacroPoint>> intGovBond5YearReaderGB() {
        return () -> this.dslContext.select()
                .from(INT_GOV_BOND_5_YEAR_GB)
                .fetch()
                .map(r ->
                        MacroPoint.builder()
                                .value(r.getValue(INT_GOV_BOND_5_YEAR_GB.VALUE))
                                .date(r.getValue(INT_GOV_BOND_5_YEAR_GB.DATE))
                                .build()
                );
    }

    public Supplier<List<MacroPoint>> intGovBond5YearReaderJapan() {
        return () -> this.dslContext.select()
                .from(INT_GOV_BOND_5_YEAR_JAPAN)
                .fetch()
                .map(r ->
                        MacroPoint.builder()
                                .value(r.getValue(INT_GOV_BOND_5_YEAR_JAPAN.VALUE))
                                .date(r.getValue(INT_GOV_BOND_5_YEAR_JAPAN.DATE))
                                .build()
                );
    }

    public Supplier<List<MacroPoint>> intGovBond5YearReaderUSA() {
        return () -> this.dslContext.select()
                .from(INT_GOV_BOND_5_YEAR_USA)
                .fetch()
                .map(r ->
                        MacroPoint.builder()
                                .value(r.getValue(INT_GOV_BOND_5_YEAR_USA.VALUE))
                                .date(r.getValue(INT_GOV_BOND_5_YEAR_USA.DATE))
                                .build()
                );
    }

    public Supplier<List<MacroPoint>> intGovBond5YearReaderFrance() {
        return () -> this.dslContext.select()
                .from(INT_GOV_BOND_5_YEAR_FRANCE)
                .fetch()
                .map(r ->
                        MacroPoint.builder()
                                .value(r.getValue(INT_GOV_BOND_5_YEAR_FRANCE.VALUE))
                                .date(r.getValue(INT_GOV_BOND_5_YEAR_FRANCE.DATE))
                                .build()
                );
    }

    public Supplier<List<MacroPoint>> intGovBond5YearReaderGermany() {
        return () -> this.dslContext.select()
                .from(INT_GOV_BOND_5_YEAR_GERMANY)
                .fetch()
                .map(r ->
                        MacroPoint.builder()
                                .value(r.getValue(INT_GOV_BOND_5_YEAR_GERMANY.VALUE))
                                .date(r.getValue(INT_GOV_BOND_5_YEAR_GERMANY.DATE))
                                .build()
                );
    }

    public Supplier<List<MacroPoint>> intGovBond5YearReaderHolland() {
        return () -> this.dslContext.select()
                .from(INT_GOV_BOND_5_YEAR_NETHERLANDS)
                .fetch()
                .map(r ->
                        MacroPoint.builder()
                                .value(r.getValue(INT_GOV_BOND_5_YEAR_NETHERLANDS.VALUE))
                                .date(r.getValue(INT_GOV_BOND_5_YEAR_NETHERLANDS.DATE))
                                .build()
                );
    }

    public Supplier<List<MacroPoint>> intGovBond10YearReaderDenmark() {
        return () -> this.dslContext.select()
                .from(INT_GOV_BOND_10_YEAR_DENMARK)
                .fetch()
                .map(r ->
                        MacroPoint.builder()
                                .value(r.getValue(INT_GOV_BOND_10_YEAR_DENMARK.VALUE))
                                .date(r.getValue(INT_GOV_BOND_10_YEAR_DENMARK.DATE))
                                .build()
                );
    }

    public Supplier<List<MacroPoint>> intGovBond10YearReaderEur() {
        return () -> this.dslContext.select()
                .from(INT_GOV_BOND_10_YEAR_EUR)
                .fetch()
                .map(r ->
                        MacroPoint.builder()
                                .value(r.getValue(INT_GOV_BOND_10_YEAR_EUR.VALUE))
                                .date(r.getValue(INT_GOV_BOND_10_YEAR_EUR.DATE))
                                .build()
                );
    }

    public Supplier<List<MacroPoint>> intGovBond10YearReaderFinland() {
        return () -> this.dslContext.select()
                .from(INT_GOV_BOND_10_YEAR_FINLAND)
                .fetch()
                .map(r ->
                        MacroPoint.builder()
                                .value(r.getValue(INT_GOV_BOND_10_YEAR_FINLAND.VALUE))
                                .date(r.getValue(INT_GOV_BOND_10_YEAR_FINLAND.DATE))
                                .build()
                );
    }

    public Supplier<List<MacroPoint>> intGovBond10YearReaderFrance() {
        return () -> this.dslContext.select()
                .from(INT_GOV_BOND_10_YEAR_FRANCE)
                .fetch()
                .map(r ->
                        MacroPoint.builder()
                                .value(r.getValue(INT_GOV_BOND_10_YEAR_FRANCE.VALUE))
                                .date(r.getValue(INT_GOV_BOND_10_YEAR_FRANCE.DATE))
                                .build()
                );
    }

    public Supplier<List<MacroPoint>> intGovBond10YearReaderGB() {
        return () -> this.dslContext.select()
                .from(INT_GOV_BOND_10_YEAR_GB)
                .fetch()
                .map(r ->
                        MacroPoint.builder()
                                .value(r.getValue(INT_GOV_BOND_10_YEAR_GB.VALUE))
                                .date(r.getValue(INT_GOV_BOND_10_YEAR_GB.DATE))
                                .build()
                );
    }

    public Supplier<List<MacroPoint>> intGovBond10YearReaderGermany() {
        return () -> this.dslContext.select()
                .from(INT_GOV_BOND_10_YEAR_GERMANY)
                .fetch()
                .map(r ->
                        MacroPoint.builder()
                                .value(r.getValue(INT_GOV_BOND_10_YEAR_GERMANY.VALUE))
                                .date(r.getValue(INT_GOV_BOND_10_YEAR_GERMANY.DATE))
                                .build()
                );
    }

    public Supplier<List<MacroPoint>> intGovBond10YearReaderJapan() {
        return () -> this.dslContext.select()
                .from(INT_GOV_BOND_10_YEAR_JAPAN)
                .fetch()
                .map(r ->
                        MacroPoint.builder()
                                .value(r.getValue(INT_GOV_BOND_10_YEAR_JAPAN.VALUE))
                                .date(r.getValue(INT_GOV_BOND_10_YEAR_JAPAN.DATE))
                                .build()
                );
    }

    public Supplier<List<MacroPoint>> intGovBond10YearReaderHolland() {
        return () -> this.dslContext.select()
                .from(INT_GOV_BOND_10_YEAR_NETHERLANDS)
                .fetch()
                .map(r ->
                        MacroPoint.builder()
                                .value(r.getValue(INT_GOV_BOND_10_YEAR_NETHERLANDS.VALUE))
                                .date(r.getValue(INT_GOV_BOND_10_YEAR_NETHERLANDS.DATE))
                                .build()
                );
    }

    public Supplier<List<MacroPoint>> intGovBond10YearReaderNorway() {
        return () -> this.dslContext.select()
                .from(INT_GOV_BOND_10_YEAR_NORWAY)
                .fetch()
                .map(r ->
                        MacroPoint.builder()
                                .value(r.getValue(INT_GOV_BOND_10_YEAR_NORWAY.VALUE))
                                .date(r.getValue(INT_GOV_BOND_10_YEAR_NORWAY.DATE))
                                .build()
                );
    }

    public Supplier<List<MacroPoint>> intGovBond10YearReaderUSA() {
        return () -> this.dslContext.select()
                .from(INT_GOV_BOND_10_YEAR_USA)
                .fetch()
                .map(r ->
                        MacroPoint.builder()
                                .value(r.getValue(INT_GOV_BOND_10_YEAR_USA.VALUE))
                                .date(r.getValue(INT_GOV_BOND_10_YEAR_USA.DATE))
                                .build()
                );
    }

    public Function<List<MacroPoint>, Integer> intGovBond5YearWriterEur() {
        return MacroPointUpsert.writer(
                dslContext,
                INT_GOV_BOND_5_YEAR_EUR,
                INT_GOV_BOND_5_YEAR_EUR.GLOBAL_ID,
                INT_GOV_BOND_5_YEAR_EUR.VALUE,
                INT_GOV_BOND_5_YEAR_EUR.DATE,
                INT_GOV_BOND_5_YEAR_EUR.UPDATED
        );
    }

    public Function<List<MacroPoint>, Integer> intGovBond5YearWriterGB() {
        return MacroPointUpsert.writer(
                dslContext,
                INT_GOV_BOND_5_YEAR_GB,
                INT_GOV_BOND_5_YEAR_GB.GLOBAL_ID,
                INT_GOV_BOND_5_YEAR_GB.VALUE,
                INT_GOV_BOND_5_YEAR_GB.DATE,
                INT_GOV_BOND_5_YEAR_GB.UPDATED
        );
    }

    public Function<List<MacroPoint>, Integer> intGovBond5YearWriterJapan() {
        return MacroPointUpsert.writer(
                dslContext,
                INT_GOV_BOND_5_YEAR_JAPAN,
                INT_GOV_BOND_5_YEAR_JAPAN.GLOBAL_ID,
                INT_GOV_BOND_5_YEAR_JAPAN.VALUE,
                INT_GOV_BOND_5_YEAR_JAPAN.DATE,
                INT_GOV_BOND_5_YEAR_JAPAN.UPDATED
        );
    }

    public Function<List<MacroPoint>, Integer> intGovBond5YearWriterUSA() {
        return MacroPointUpsert.writer(
                dslContext,
                INT_GOV_BOND_5_YEAR_USA,
                INT_GOV_BOND_5_YEAR_USA.GLOBAL_ID,
                INT_GOV_BOND_5_YEAR_USA.VALUE,
                INT_GOV_BOND_5_YEAR_USA.DATE,
                INT_GOV_BOND_5_YEAR_USA.UPDATED
        );
    }

    public Function<List<MacroPoint>, Integer> intGovBond5YearWriterFrance() {
        return MacroPointUpsert.writer(
                dslContext,
                INT_GOV_BOND_5_YEAR_FRANCE,
                INT_GOV_BOND_5_YEAR_FRANCE.GLOBAL_ID,
                INT_GOV_BOND_5_YEAR_FRANCE.VALUE,
                INT_GOV_BOND_5_YEAR_FRANCE.DATE,
                INT_GOV_BOND_5_YEAR_FRANCE.UPDATED
        );
    }

    public Function<List<MacroPoint>, Integer> intGovBond5YearWriterHolland() {
        return MacroPointUpsert.writer(
                dslContext,
                INT_GOV_BOND_5_YEAR_NETHERLANDS,
                INT_GOV_BOND_5_YEAR_NETHERLANDS.GLOBAL_ID,
                INT_GOV_BOND_5_YEAR_NETHERLANDS.VALUE,
                INT_GOV_BOND_5_YEAR_NETHERLANDS.DATE,
                INT_GOV_BOND_5_YEAR_NETHERLANDS.UPDATED
        );
    }

    public Function<List<MacroPoint>, Integer> intGovBond5YearWriterGermany() {
        return MacroPointUpsert.writer(
                dslContext,
                INT_GOV_BOND_5_YEAR_GERMANY,
                INT_GOV_BOND_5_YEAR_GERMANY.GLOBAL_ID,
                INT_GOV_BOND_5_YEAR_GERMANY.VALUE,
                INT_GOV_BOND_5_YEAR_GERMANY.DATE,
                INT_GOV_BOND_5_YEAR_GERMANY.UPDATED
        );
    }

    public Function<List<MacroPoint>, Integer> intGovBond10YearWriterUSA() {
        return MacroPointUpsert.writer(
                dslContext,
                INT_GOV_BOND_10_YEAR_USA,
                INT_GOV_BOND_10_YEAR_USA.GLOBAL_ID,
                INT_GOV_BOND_10_YEAR_USA.VALUE,
                INT_GOV_BOND_10_YEAR_USA.DATE,
                INT_GOV_BOND_10_YEAR_USA.UPDATED
        );
    }

    public Function<List<MacroPoint>, Integer> intGovBond10YearWriterNorway() {
        return MacroPointUpsert.writer(
                dslContext,
                INT_GOV_BOND_10_YEAR_NORWAY,
                INT_GOV_BOND_10_YEAR_NORWAY.GLOBAL_ID,
                INT_GOV_BOND_10_YEAR_NORWAY.VALUE,
                INT_GOV_BOND_10_YEAR_NORWAY.DATE,
                INT_GOV_BOND_10_YEAR_NORWAY.UPDATED
        );
    }

    public Function<List<MacroPoint>, Integer> intGovBond10YearWriterHolland() {
        return MacroPointUpsert.writer(
                dslContext,
                INT_GOV_BOND_10_YEAR_NETHERLANDS,
                INT_GOV_BOND_10_YEAR_NETHERLANDS.GLOBAL_ID,
                INT_GOV_BOND_10_YEAR_NETHERLANDS.VALUE,
                INT_GOV_BOND_10_YEAR_NETHERLANDS.DATE,
                INT_GOV_BOND_10_YEAR_NETHERLANDS.UPDATED
        );
    }

    public Function<List<MacroPoint>, Integer> intGovBond10YearWriterJapan() {
        return MacroPointUpsert.writer(
                dslContext,
                INT_GOV_BOND_10_YEAR_JAPAN,
                INT_GOV_BOND_10_YEAR_JAPAN.GLOBAL_ID,
                INT_GOV_BOND_10_YEAR_JAPAN.VALUE,
                INT_GOV_BOND_10_YEAR_JAPAN.DATE,
                INT_GOV_BOND_10_YEAR_JAPAN.UPDATED
        );
    }

    public Function<List<MacroPoint>, Integer> intGovBond10YearWriterGermany() {
        return MacroPointUpsert.writer(
                dslContext,
                INT_GOV_BOND_10_YEAR_GERMANY,
                INT_GOV_BOND_10_YEAR_GERMANY.GLOBAL_ID,
                INT_GOV_BOND_10_YEAR_GERMANY.VALUE,
                INT_GOV_BOND_10_YEAR_GERMANY.DATE,
                INT_GOV_BOND_10_YEAR_GERMANY.UPDATED
        );
    }

    public Function<List<MacroPoint>, Integer> intGovBond10YearWriterGB() {
        return MacroPointUpsert.writer(
                dslContext,
                INT_GOV_BOND_10_YEAR_GB,
                INT_GOV_BOND_10_YEAR_GB.GLOBAL_ID,
                INT_GOV_BOND_10_YEAR_GB.VALUE,
                INT_GOV_BOND_10_YEAR_GB.DATE,
                INT_GOV_BOND_10_YEAR_GB.UPDATED
        );
    }

    public Function<List<MacroPoint>, Integer> intGovBond10YearWriterFrance() {
        return MacroPointUpsert.writer(
                dslContext,
                INT_GOV_BOND_10_YEAR_FRANCE,
                INT_GOV_BOND_10_YEAR_FRANCE.GLOBAL_ID,
                INT_GOV_BOND_10_YEAR_FRANCE.VALUE,
                INT_GOV_BOND_10_YEAR_FRANCE.DATE,
                INT_GOV_BOND_10_YEAR_FRANCE.UPDATED
        );
    }

    public Function<List<MacroPoint>, Integer> intGovBond10YearWriterFinland() {
        return MacroPointUpsert.writer(
                dslContext,
                INT_GOV_BOND_10_YEAR_FINLAND,
                INT_GOV_BOND_10_YEAR_FINLAND.GLOBAL_ID,
                INT_GOV_BOND_10_YEAR_FINLAND.VALUE,
                INT_GOV_BOND_10_YEAR_FINLAND.DATE,
                INT_GOV_BOND_10_YEAR_FINLAND.UPDATED
        );
    }

    public Function<List<MacroPoint>, Integer> intGovBond10YearWriterEur() {
        return MacroPointUpsert.writer(
                dslContext,
                INT_GOV_BOND_10_YEAR_EUR,
                INT_GOV_BOND_10_YEAR_EUR.GLOBAL_ID,
                INT_GOV_BOND_10_YEAR_EUR.VALUE,
                INT_GOV_BOND_10_YEAR_EUR.DATE,
                INT_GOV_BOND_10_YEAR_EUR.UPDATED
        );
    }

    public Function<List<MacroPoint>, Integer> intGovBond10YearWriterDenmark() {
        return MacroPointUpsert.writer(
                dslContext,
                INT_GOV_BOND_10_YEAR_DENMARK,
                INT_GOV_BOND_10_YEAR_DENMARK.GLOBAL_ID,
                INT_GOV_BOND_10_YEAR_DENMARK.VALUE,
                INT_GOV_BOND_10_YEAR_DENMARK.DATE,
                INT_GOV_BOND_10_YEAR_DENMARK.UPDATED
        );
    }

}
