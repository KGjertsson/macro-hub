package com.kg.macroanalyzer.repositories;

import com.kg.macroanalyzer.models.GovernmentBillItem;
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

    public Supplier<List<GovernmentBillItem>> swedishGovBills1MonthReader() {
        return () -> dslContext.select()
                .from(SWEDISH_GOVERNMENT_BILL_1_MONTH)
                .fetch()
                .map(GovernmentBillItem::ofSwedish1Month);
    }

    public Supplier<List<GovernmentBillItem>> swedishGovBills3MonthReader() {
        return () -> dslContext.select()
                .from(SWEDISH_GOVERNMENT_BILL_3_MONTH)
                .fetch()
                .map(GovernmentBillItem::ofSwedish3Month);
    }

    public Supplier<List<GovernmentBillItem>> swedishGovBills6MonthReader() {
        return () -> dslContext.select()
                .from(SWEDISH_GOVERNMENT_BILL_6_MONTH)
                .fetch()
                .map(GovernmentBillItem::ofSwedish6Month);
    }

    public Supplier<List<GovernmentBillItem>> swedishGovBills12MonthReader() {
        return () -> dslContext.select()
                .from(SWEDISH_GOVERNMENT_BILL_12_MONTH)
                .fetch()
                .map(GovernmentBillItem::ofSwedish12Month);
    }

    public Function<List<GovernmentBillItem>, Integer> swedishGovBill1MonthWriter() {
        return governmentBillItemList -> {
            if (!governmentBillItemList.isEmpty()) {
                final var insertQuery = dslContext.batch(
                        dslContext.insertInto(
                                SWEDISH_GOVERNMENT_BILL_1_MONTH,
                                SWEDISH_GOVERNMENT_BILL_1_MONTH.GLOBAL_ID,
                                SWEDISH_GOVERNMENT_BILL_1_MONTH.VALUE,
                                SWEDISH_GOVERNMENT_BILL_1_MONTH.DATE
                        ).values(DSL.val((UUID) null), DSL.val(0.0), DSL.val(LocalDate.MIN))
                );
                governmentBillItemList.forEach(governmentBillItem -> insertQuery.bind(
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

    public Function<List<GovernmentBillItem>, Integer> swedishGovBill3MonthWriter() {
        return governmentBillItemList -> {
            if (!governmentBillItemList.isEmpty()) {
                final var insertQuery = dslContext.batch(
                        dslContext.insertInto(
                                SWEDISH_GOVERNMENT_BILL_3_MONTH,
                                SWEDISH_GOVERNMENT_BILL_3_MONTH.GLOBAL_ID,
                                SWEDISH_GOVERNMENT_BILL_3_MONTH.VALUE,
                                SWEDISH_GOVERNMENT_BILL_3_MONTH.DATE
                        ).values(DSL.val((UUID) null), DSL.val(0.0), DSL.val(LocalDate.MIN))
                );
                governmentBillItemList.forEach(governmentBillItem -> insertQuery.bind(
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

    public Function<List<GovernmentBillItem>, Integer> swedishGovBill6MonthWriter() {
        return governmentBillItemList -> {
            if (!governmentBillItemList.isEmpty()) {
                final var insertQuery = dslContext.batch(
                        dslContext.insertInto(
                                SWEDISH_GOVERNMENT_BILL_6_MONTH,
                                SWEDISH_GOVERNMENT_BILL_6_MONTH.GLOBAL_ID,
                                SWEDISH_GOVERNMENT_BILL_6_MONTH.VALUE,
                                SWEDISH_GOVERNMENT_BILL_6_MONTH.DATE
                        ).values(DSL.val((UUID) null), DSL.val(0.0), DSL.val(LocalDate.MIN))
                );
                governmentBillItemList.forEach(governmentBillItem -> insertQuery.bind(
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

    public Function<List<GovernmentBillItem>, Integer> swedishGovBill12MonthWriter() {
        return governmentBillItemList -> {
            if (!governmentBillItemList.isEmpty()) {
                final var insertQuery = dslContext.batch(
                        dslContext.insertInto(
                                SWEDISH_GOVERNMENT_BILL_12_MONTH,
                                SWEDISH_GOVERNMENT_BILL_12_MONTH.GLOBAL_ID,
                                SWEDISH_GOVERNMENT_BILL_12_MONTH.VALUE,
                                SWEDISH_GOVERNMENT_BILL_12_MONTH.DATE
                        ).values(DSL.val((UUID) null), DSL.val(0.0), DSL.val(LocalDate.MIN))
                );
                governmentBillItemList.forEach(governmentBillItem -> insertQuery.bind(
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

}
