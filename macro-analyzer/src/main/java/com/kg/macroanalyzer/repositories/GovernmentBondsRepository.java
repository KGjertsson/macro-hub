package com.kg.macroanalyzer.repositories;

import com.kg.macroanalyzer.models.GovernmentBondItem;
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
                .from(SWEDISH_GOVERNMENT_BONDS_2_YEAR)
                .fetch()
                .map(GovernmentBondItem::ofSwedish2Month);
    }

    public List<GovernmentBondItem> getSwedishGovernmentBonds5Month() {
        return dslContext.select()
                .from(SWEDISH_GOVERNMENT_BONDS_5_YEAR)
                .fetch()
                .map(GovernmentBondItem::ofSwedish5Month);
    }

    public List<GovernmentBondItem> getSwedishGovernmentBonds7Month() {
        return dslContext.select()
                .from(SWEDISH_GOVERNMENT_BONDS_7_YEAR)
                .fetch()
                .map(GovernmentBondItem::ofSwedish7Month);
    }

    public List<GovernmentBondItem> getSwedishGovernmentBonds10Month() {
        return dslContext.select()
                .from(SWEDISH_GOVERNMENT_BONDS_10_YEAR)
                .fetch()
                .map(GovernmentBondItem::ofSwedish10Month);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void insertSwedishGovernmentBonds2Month(List<GovernmentBondItem> governmentBondItemList) {
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

            insertQuery.execute();
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void insertSwedishGovernmentBonds5Month(List<GovernmentBondItem> governmentBondItemList) {
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

            insertQuery.execute();
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void insertSwedishGovernmentBonds7Month(List<GovernmentBondItem> governmentBondItemList) {
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

            insertQuery.execute();
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void insertSwedishGovernmentBonds10Month(List<GovernmentBondItem> governmentBondItemList) {
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

            insertQuery.execute();
        }
    }

    public List<GovernmentBondItem> getIntGovBond5YearEur() {
        return this.dslContext.select()
                .from(INT_GOV_BOND_5_YEAR_EUR)
                .fetch()
                .map(GovernmentBondItem::ofIntGovBond5YearEur);
    }

    public List<GovernmentBondItem> getIntGovBond5YearGB() {
        return this.dslContext.select()
                .from(INT_GOV_BOND_5_YEAR_GB)
                .fetch()
                .map(GovernmentBondItem::ofIntGovBond5YearGb);
    }

    public List<GovernmentBondItem> getIntGovBond5YearJapan() {
        return this.dslContext.select()
                .from(INT_GOV_BOND_5_YEAR_JAPAN)
                .fetch()
                .map(GovernmentBondItem::ofIntGovBond5YearJapan);
    }

    public List<GovernmentBondItem> getIntGovBond5YearUsa() {
        return this.dslContext.select()
                .from(INT_GOV_BOND_5_YEAR_EUR)
                .fetch()
                .map(GovernmentBondItem::ofIntGovBond5YearUsa);
    }

    public List<GovernmentBondItem> getIntGovBond5YearFrance() {
        return this.dslContext.select()
                .from(INT_GOV_BOND_5_YEAR_FRANCE)
                .fetch()
                .map(GovernmentBondItem::ofIntGovBond5YearFrance);
    }

    public List<GovernmentBondItem> getIntGovBond5YearGermany() {
        return this.dslContext.select()
                .from(INT_GOV_BOND_5_YEAR_GERMANY)
                .fetch()
                .map(GovernmentBondItem::ofIntGovBond5YearGermany);
    }

    public List<GovernmentBondItem> getIntGovBond5YearHolland() {
        return this.dslContext.select()
                .from(INT_GOV_BOND_5_YEAR_FRANCE)
                .fetch()
                .map(GovernmentBondItem::ofIntGovBond5YearNetherlands);
    }

    public List<GovernmentBondItem> getIntGovBond10YearDenmark() {
        return this.dslContext.select()
                .from(INT_GOV_BOND_10_YEAR_DENMARK)
                .fetch()
                .map(GovernmentBondItem::ofIntGovBond10YearDenmark);
    }

    public List<GovernmentBondItem> getIntGovBond10YearEur() {
        return this.dslContext.select()
                .from(INT_GOV_BOND_10_YEAR_EUR)
                .fetch()
                .map(GovernmentBondItem::ofIntGovBond10YearEur);
    }

    public List<GovernmentBondItem> getIntGovBond10YearFinland() {
        return this.dslContext.select()
                .from(INT_GOV_BOND_10_YEAR_FINLAND)
                .fetch()
                .map(GovernmentBondItem::ofIntGovBond10YearFinland);
    }

    public List<GovernmentBondItem> getIntGovBond10YearFrance() {
        return this.dslContext.select()
                .from(INT_GOV_BOND_10_YEAR_FRANCE)
                .fetch()
                .map(GovernmentBondItem::ofIntGovBond10YearFrance);
    }

    public List<GovernmentBondItem> getIntGovBond10YearGB() {
        return this.dslContext.select()
                .from(INT_GOV_BOND_10_YEAR_GB)
                .fetch()
                .map(GovernmentBondItem::ofIntGovBond10YearGb);
    }

    public List<GovernmentBondItem> getIntGovBond10YearGermany() {
        return this.dslContext.select()
                .from(INT_GOV_BOND_10_YEAR_GERMANY)
                .fetch()
                .map(GovernmentBondItem::ofIntGovBond10YearGermany);
    }

    public List<GovernmentBondItem> getIntGovBond10YearJapan() {
        return this.dslContext.select()
                .from(INT_GOV_BOND_10_YEAR_JAPAN)
                .fetch()
                .map(GovernmentBondItem::ofIntGovBond10YearJapan);
    }

    public List<GovernmentBondItem> getIntGovBond10YearHolland() {
        return this.dslContext.select()
                .from(INT_GOV_BOND_10_YEAR_NETHERLANDS)
                .fetch()
                .map(GovernmentBondItem::ofIntGovBond10YearNetherlands);
    }

    public List<GovernmentBondItem> getIntGovBond10YearNorway() {
        return this.dslContext.select()
                .from(INT_GOV_BOND_10_YEAR_NORWAY)
                .fetch()
                .map(GovernmentBondItem::ofIntGovBond10YearNorway);
    }

    public List<GovernmentBondItem> getIntGovBond10YearUsa() {
        return this.dslContext.select()
                .from(INT_GOV_BOND_10_YEAR_USA)
                .fetch()
                .map(GovernmentBondItem::ofIntGovBond10YearUsa);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void insertIntGovBond5YearEur(List<GovernmentBondItem> scraped) {
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

            insertQuery.execute();
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void insertIntGovBond5YearGB(List<GovernmentBondItem> scraped) {
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

            insertQuery.execute();
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void insertIntGovBond5YearJapan(List<GovernmentBondItem> scraped) {
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

            insertQuery.execute();
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void insertIntGovBond5YearUsa(List<GovernmentBondItem> scraped) {
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

            insertQuery.execute();
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void insertIntGovBond5YearFrance(List<GovernmentBondItem> scraped) {
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

            insertQuery.execute();
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void insertIntGovBond5YearHolland(List<GovernmentBondItem> scraped) {
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

            insertQuery.execute();
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void insertIntGovBond5YearGermany(List<GovernmentBondItem> scraped) {
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

            insertQuery.execute();
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void insertIntGovBond10YearUsa(List<GovernmentBondItem> scraped) {
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

            insertQuery.execute();
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void insertIntGovBond10YearNorway(List<GovernmentBondItem> scraped) {
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

            insertQuery.execute();
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void insertIntGovBond10YearHolland(List<GovernmentBondItem> scraped) {
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

            insertQuery.execute();
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void insertIntGovBond10YearJapan(List<GovernmentBondItem> scraped) {
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

            insertQuery.execute();
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void insertIntGovBond10YearGermany(List<GovernmentBondItem> scraped) {
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

            insertQuery.execute();
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void insertIntGovBond10YearGB(List<GovernmentBondItem> scraped) {
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

            insertQuery.execute();
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void insertIntGovBond10YearFrance(List<GovernmentBondItem> scraped) {
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

            insertQuery.execute();
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void insertIntGovBond10YearFinland(List<GovernmentBondItem> scraped) {
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

            insertQuery.execute();
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void insertIntGovBond10YearEur(List<GovernmentBondItem> scraped) {
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

            insertQuery.execute();
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void insertIntGovBond10YearDenmark(List<GovernmentBondItem> scraped) {
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

            insertQuery.execute();
        }
    }

}
