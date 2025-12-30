package com.kg.macroanalyzer.adaptors.database.postgres.repositories;

import com.kg.macroanalyzer.application.domain.macroseries.MacroPoint;
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
public class GovernmentBillRepository {

    private final DSLContext dslContext;

    @Autowired
    public GovernmentBillRepository(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    public Supplier<List<MacroPoint>> swedishGovBills1MonthReader() {
        return () -> dslContext.select()
                .from(SWEDISH_GOVERNMENT_BILL_1_MONTH)
                .fetch()
                .map(r ->
                        MacroPoint.builder()
                                .value(r.getValue(SWEDISH_GOVERNMENT_BILL_1_MONTH.VALUE))
                                .date(r.getValue(SWEDISH_GOVERNMENT_BILL_1_MONTH.DATE))
                                .build()
                );
    }

    public Supplier<List<MacroPoint>> swedishGovBills3MonthReader() {
        return () -> dslContext.select()
                .from(SWEDISH_GOVERNMENT_BILL_3_MONTH)
                .fetch()
                .map(r ->
                        MacroPoint.builder()
                                .value(r.getValue(SWEDISH_GOVERNMENT_BILL_3_MONTH.VALUE))
                                .date(r.getValue(SWEDISH_GOVERNMENT_BILL_3_MONTH.DATE))
                                .build()
                );
    }

    public Supplier<List<MacroPoint>> swedishGovBills6MonthReader() {
        return () -> dslContext.select()
                .from(SWEDISH_GOVERNMENT_BILL_6_MONTH)
                .fetch()
                .map(r ->
                        MacroPoint.builder()
                                .value(r.getValue(SWEDISH_GOVERNMENT_BILL_6_MONTH.VALUE))
                                .date(r.getValue(SWEDISH_GOVERNMENT_BILL_6_MONTH.DATE))
                                .build()
                );
    }

    public Supplier<List<MacroPoint>> swedishGovBills12MonthReader() {
        return () -> dslContext.select()
                .from(SWEDISH_GOVERNMENT_BILL_12_MONTH)
                .fetch()
                .map(r ->
                        MacroPoint.builder()
                                .value(r.getValue(SWEDISH_GOVERNMENT_BILL_12_MONTH.VALUE))
                                .date(r.getValue(SWEDISH_GOVERNMENT_BILL_12_MONTH.DATE))
                                .build()
                );
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public Function<List<MacroPoint>, Integer> swedishGovBill1MonthWriter() {
        return MacroPointList -> {
            if (!MacroPointList.isEmpty()) {
                final var insertQuery = dslContext.batch(
                        dslContext.insertInto(
                                SWEDISH_GOVERNMENT_BILL_1_MONTH,
                                SWEDISH_GOVERNMENT_BILL_1_MONTH.GLOBAL_ID,
                                SWEDISH_GOVERNMENT_BILL_1_MONTH.VALUE,
                                SWEDISH_GOVERNMENT_BILL_1_MONTH.DATE
                        ).values(DSL.val((UUID) null), DSL.val(0.0), DSL.val(LocalDate.MIN))
                );
                MacroPointList.forEach(MacroPoint -> insertQuery.bind(
                        UUID.randomUUID(),
                        MacroPoint.value(),
                        MacroPoint.date()
                ));
                final var result = insertQuery.execute();

                return result.length;
            }

            return 0;
        };
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public Function<List<MacroPoint>, Integer> swedishGovBill3MonthWriter() {
        return MacroPointList -> {
            if (!MacroPointList.isEmpty()) {
                final var insertQuery = dslContext.batch(
                        dslContext.insertInto(
                                SWEDISH_GOVERNMENT_BILL_3_MONTH,
                                SWEDISH_GOVERNMENT_BILL_3_MONTH.GLOBAL_ID,
                                SWEDISH_GOVERNMENT_BILL_3_MONTH.VALUE,
                                SWEDISH_GOVERNMENT_BILL_3_MONTH.DATE
                        ).values(DSL.val((UUID) null), DSL.val(0.0), DSL.val(LocalDate.MIN))
                );
                MacroPointList.forEach(MacroPoint -> insertQuery.bind(
                        UUID.randomUUID(),
                        MacroPoint.value(),
                        MacroPoint.date()
                ));

                final var result = insertQuery.execute();

                return result.length;
            }

            return 0;
        };
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public Function<List<MacroPoint>, Integer> swedishGovBill6MonthWriter() {
        return MacroPointList -> {
            if (!MacroPointList.isEmpty()) {
                final var insertQuery = dslContext.batch(
                        dslContext.insertInto(
                                SWEDISH_GOVERNMENT_BILL_6_MONTH,
                                SWEDISH_GOVERNMENT_BILL_6_MONTH.GLOBAL_ID,
                                SWEDISH_GOVERNMENT_BILL_6_MONTH.VALUE,
                                SWEDISH_GOVERNMENT_BILL_6_MONTH.DATE
                        ).values(DSL.val((UUID) null), DSL.val(0.0), DSL.val(LocalDate.MIN))
                );
                MacroPointList.forEach(MacroPoint -> insertQuery.bind(
                        UUID.randomUUID(),
                        MacroPoint.value(),
                        MacroPoint.date()
                ));
                final var result = insertQuery.execute();

                return result.length;
            }

            return 0;
        };
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public Function<List<MacroPoint>, Integer> swedishGovBill12MonthWriter() {
        return MacroPointList -> {
            if (!MacroPointList.isEmpty()) {
                final var insertQuery = dslContext.batch(
                        dslContext.insertInto(
                                SWEDISH_GOVERNMENT_BILL_12_MONTH,
                                SWEDISH_GOVERNMENT_BILL_12_MONTH.GLOBAL_ID,
                                SWEDISH_GOVERNMENT_BILL_12_MONTH.VALUE,
                                SWEDISH_GOVERNMENT_BILL_12_MONTH.DATE
                        ).values(DSL.val((UUID) null), DSL.val(0.0), DSL.val(LocalDate.MIN))
                );
                MacroPointList.forEach(MacroPoint -> insertQuery.bind(
                        UUID.randomUUID(),
                        MacroPoint.value(),
                        MacroPoint.date()
                ));

                final var result = insertQuery.execute();

                return result.length;
            }

            return 0;
        };
    }

}
