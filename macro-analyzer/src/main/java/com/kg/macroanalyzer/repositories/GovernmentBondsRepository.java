package com.kg.macroanalyzer.repositories;

import com.kg.macroanalyzer.models.governmentbonds.GovernmentBondItem;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static com.kg.macroanalyzer.jooq.generated.Tables.*;

@Slf4j
@Repository
public class GovernmentBondsRepository {

    private final DSLContext dslContext;

    public GovernmentBondsRepository(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    public List<GovernmentBondItem> getSwedishGovernmentBonds2Month() {
        return dslContext.select()
                .from(SWEDISH_GOVERNMENT_BONDS_2_MONTH)
                .fetch()
                .map(GovernmentBondItem::ofSwedish2Month);
    }

    public List<GovernmentBondItem> getSwedishGovernmentBonds5Month() {
        return dslContext.select()
                .from(SWEDISH_GOVERNMENT_BONDS_5_MONTH)
                .fetch()
                .map(GovernmentBondItem::ofSwedish5Month);
    }

    public List<GovernmentBondItem> getSwedishGovernmentBonds7Month() {
        return dslContext.select()
                .from(SWEDISH_GOVERNMENT_BONDS_7_MONTH)
                .fetch()
                .map(GovernmentBondItem::ofSwedish7Month);
    }

    public List<GovernmentBondItem> getSwedishGovernmentBonds10Month() {
        return dslContext.select()
                .from(SWEDISH_GOVERNMENT_BONDS_10_MONTH)
                .fetch()
                .map(GovernmentBondItem::ofSwedish10Month);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void insertSwedishGovernmentBonds2Month(List<GovernmentBondItem> governmentBondItemList) {
        if (!governmentBondItemList.isEmpty()) {
            final var insertQuery = dslContext.batch(
                    dslContext.insertInto(
                            SWEDISH_GOVERNMENT_BONDS_2_MONTH,
                            SWEDISH_GOVERNMENT_BONDS_2_MONTH.GLOBAL_ID,
                            SWEDISH_GOVERNMENT_BONDS_2_MONTH.VALUE,
                            SWEDISH_GOVERNMENT_BONDS_2_MONTH.DATE
                    ).values(DSL.val((UUID) null), DSL.val(0.0), DSL.val(LocalDate.MIN))
            );
            governmentBondItemList.forEach(governmentBillItem -> insertQuery.bind(
                    UUID.randomUUID(),
                    governmentBillItem.value(),
                    governmentBillItem.date()
            ));

            insertQuery.execute();
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void insertSwedishGovernmentBonds5Month(List<GovernmentBondItem> governmentBondItemList) {
        if (!governmentBondItemList.isEmpty()) {
            final var insertQuery = dslContext.batch(
                    dslContext.insertInto(
                            SWEDISH_GOVERNMENT_BONDS_5_MONTH,
                            SWEDISH_GOVERNMENT_BONDS_5_MONTH.GLOBAL_ID,
                            SWEDISH_GOVERNMENT_BONDS_5_MONTH.VALUE,
                            SWEDISH_GOVERNMENT_BONDS_5_MONTH.DATE
                    ).values(DSL.val((UUID) null), DSL.val(0.0), DSL.val(LocalDate.MIN))
            );
            governmentBondItemList.forEach(governmentBillItem -> insertQuery.bind(
                    UUID.randomUUID(),
                    governmentBillItem.value(),
                    governmentBillItem.date()
            ));

            insertQuery.execute();
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void insertSwedishGovernmentBonds7Month(List<GovernmentBondItem> governmentBondItemList) {
        if (!governmentBondItemList.isEmpty()) {
            final var insertQuery = dslContext.batch(
                    dslContext.insertInto(
                            SWEDISH_GOVERNMENT_BONDS_7_MONTH,
                            SWEDISH_GOVERNMENT_BONDS_7_MONTH.GLOBAL_ID,
                            SWEDISH_GOVERNMENT_BONDS_7_MONTH.VALUE,
                            SWEDISH_GOVERNMENT_BONDS_7_MONTH.DATE
                    ).values(DSL.val((UUID) null), DSL.val(0.0), DSL.val(LocalDate.MIN))
            );
            governmentBondItemList.forEach(governmentBillItem -> insertQuery.bind(
                    UUID.randomUUID(),
                    governmentBillItem.value(),
                    governmentBillItem.date()
            ));

            insertQuery.execute();
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void insertSwedishGovernmentBonds10Month(List<GovernmentBondItem> governmentBondItemList) {
        if (!governmentBondItemList.isEmpty()) {
            final var insertQuery = dslContext.batch(
                    dslContext.insertInto(
                            SWEDISH_GOVERNMENT_BONDS_10_MONTH,
                            SWEDISH_GOVERNMENT_BONDS_10_MONTH.GLOBAL_ID,
                            SWEDISH_GOVERNMENT_BONDS_10_MONTH.VALUE,
                            SWEDISH_GOVERNMENT_BONDS_10_MONTH.DATE
                    ).values(DSL.val((UUID) null), DSL.val(0.0), DSL.val(LocalDate.MIN))
            );
            governmentBondItemList.forEach(governmentBillItem -> insertQuery.bind(
                    UUID.randomUUID(),
                    governmentBillItem.value(),
                    governmentBillItem.date()
            ));

            insertQuery.execute();
        }
    }

}
