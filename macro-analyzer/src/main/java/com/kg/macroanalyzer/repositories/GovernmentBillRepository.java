package com.kg.macroanalyzer.repositories;

import com.kg.macroanalyzer.models.governmentbills.GovernmentBillItem;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static com.kg.macroanalyzer.jooq.generated.Tables.*;

@Repository
public class GovernmentBillRepository {

    private final DSLContext dslContext;

    @Autowired
    public GovernmentBillRepository(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    public List<GovernmentBillItem> getSwedishGovernmentBills1Month() {
        return dslContext.select()
                .from(SWEDISH_GOVERNMENT_BILL_1_MONTH)
                .fetch()
                .map(GovernmentBillItem::ofSwedish1Month);
    }

    public List<GovernmentBillItem> getSwedishGovernmentBills3Month() {
        return dslContext.select()
                .from(SWEDISH_GOVERNMENT_BILL_3_MONTH)
                .fetch()
                .map(GovernmentBillItem::ofSwedish3Month);
    }

    public List<GovernmentBillItem> getSwedishGovernmentBills6Month() {
        return dslContext.select()
                .from(SWEDISH_GOVERNMENT_BILL_6_MONTH)
                .fetch()
                .map(GovernmentBillItem::ofSwedish6Month);
    }

    public List<GovernmentBillItem> getSwedishGovernmentBills12Month() {
        return dslContext.select()
                .from(SWEDISH_GOVERNMENT_BILL_12_MONTH)
                .fetch()
                .map(GovernmentBillItem::ofSwedish12Month);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void insertSwedishGovernmentBills1Month(List<GovernmentBillItem> governmentBillItemList) {
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

            insertQuery.execute();
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void insertSwedishGovernmentBills3Month(List<GovernmentBillItem> governmentBillItemList) {
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

            insertQuery.execute();
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void insertSwedishGovernmentBills6Month(List<GovernmentBillItem> governmentBillItemList) {
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

            insertQuery.execute();
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void insertSwedishGovernmentBills12Month(List<GovernmentBillItem> governmentBillItemList) {
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

            insertQuery.execute();
        }
    }

}
