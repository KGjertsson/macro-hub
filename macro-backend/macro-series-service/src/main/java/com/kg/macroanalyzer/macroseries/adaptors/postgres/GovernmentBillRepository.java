package com.kg.macroanalyzer.macroseries.adaptors.postgres;

import com.kg.macroanalyzer.macroseries.domain.MacroPoint;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.kg.macroanalyzer.macroseries.jooq.generated.Tables.*;

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

    public Function<List<MacroPoint>, Integer> swedishGovBill1MonthWriter() {
        return MacroPointUpsert.writer(
                dslContext,
                SWEDISH_GOVERNMENT_BILL_1_MONTH,
                SWEDISH_GOVERNMENT_BILL_1_MONTH.GLOBAL_ID,
                SWEDISH_GOVERNMENT_BILL_1_MONTH.VALUE,
                SWEDISH_GOVERNMENT_BILL_1_MONTH.DATE,
                SWEDISH_GOVERNMENT_BILL_1_MONTH.UPDATED
        );
    }

    public Function<List<MacroPoint>, Integer> swedishGovBill3MonthWriter() {
        return MacroPointUpsert.writer(
                dslContext,
                SWEDISH_GOVERNMENT_BILL_3_MONTH,
                SWEDISH_GOVERNMENT_BILL_3_MONTH.GLOBAL_ID,
                SWEDISH_GOVERNMENT_BILL_3_MONTH.VALUE,
                SWEDISH_GOVERNMENT_BILL_3_MONTH.DATE,
                SWEDISH_GOVERNMENT_BILL_3_MONTH.UPDATED
        );
    }

    public Function<List<MacroPoint>, Integer> swedishGovBill6MonthWriter() {
        return MacroPointUpsert.writer(
                dslContext,
                SWEDISH_GOVERNMENT_BILL_6_MONTH,
                SWEDISH_GOVERNMENT_BILL_6_MONTH.GLOBAL_ID,
                SWEDISH_GOVERNMENT_BILL_6_MONTH.VALUE,
                SWEDISH_GOVERNMENT_BILL_6_MONTH.DATE,
                SWEDISH_GOVERNMENT_BILL_6_MONTH.UPDATED
        );
    }

    public Function<List<MacroPoint>, Integer> swedishGovBill12MonthWriter() {
        return MacroPointUpsert.writer(
                dslContext,
                SWEDISH_GOVERNMENT_BILL_12_MONTH,
                SWEDISH_GOVERNMENT_BILL_12_MONTH.GLOBAL_ID,
                SWEDISH_GOVERNMENT_BILL_12_MONTH.VALUE,
                SWEDISH_GOVERNMENT_BILL_12_MONTH.DATE,
                SWEDISH_GOVERNMENT_BILL_12_MONTH.UPDATED
        );
    }

}
