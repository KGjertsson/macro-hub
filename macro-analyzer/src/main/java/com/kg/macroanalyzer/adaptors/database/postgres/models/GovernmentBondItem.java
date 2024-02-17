package com.kg.macroanalyzer.adaptors.database.postgres.models;

import lombok.Builder;
import org.jooq.Record;

import java.time.LocalDate;

import static com.kg.macroanalyzer.jooq.generated.Tables.*;

@Builder
public record GovernmentBondItem(Double value, LocalDate date) {

    public static GovernmentBondItem ofSwedish2Month(Record r) {
        return GovernmentBondItem.builder()
                .value(r.getValue(SWEDISH_GOVERNMENT_BONDS_2_YEAR.VALUE))
                .date(r.getValue(SWEDISH_GOVERNMENT_BONDS_2_YEAR.DATE))
                .build();
    }

    public static GovernmentBondItem ofSwedish5Month(Record r) {
        return GovernmentBondItem.builder()
                .value(r.getValue(SWEDISH_GOVERNMENT_BONDS_5_YEAR.VALUE))
                .date(r.getValue(SWEDISH_GOVERNMENT_BONDS_5_YEAR.DATE))
                .build();
    }

    public static GovernmentBondItem ofSwedish7Month(Record r) {
        return GovernmentBondItem.builder()
                .value(r.getValue(SWEDISH_GOVERNMENT_BONDS_7_YEAR.VALUE))
                .date(r.getValue(SWEDISH_GOVERNMENT_BONDS_7_YEAR.DATE))
                .build();
    }

    public static GovernmentBondItem ofSwedish10Month(Record r) {
        return GovernmentBondItem.builder()
                .value(r.getValue(SWEDISH_GOVERNMENT_BONDS_10_YEAR.VALUE))
                .date(r.getValue(SWEDISH_GOVERNMENT_BONDS_10_YEAR.DATE))
                .build();
    }

    public static GovernmentBondItem ofIntGovBond5YearEur(Record record) {
        return GovernmentBondItem.builder()
                .value(record.getValue(INT_GOV_BOND_5_YEAR_EUR.VALUE))
                .date(record.getValue(INT_GOV_BOND_5_YEAR_EUR.DATE))
                .build();
    }

    public static GovernmentBondItem ofIntGovBond5YearGb(Record record) {
        return GovernmentBondItem.builder()
                .value(record.getValue(INT_GOV_BOND_5_YEAR_GB.VALUE))
                .date(record.getValue(INT_GOV_BOND_5_YEAR_GB.DATE))
                .build();
    }

    public static GovernmentBondItem ofIntGovBond5YearJapan(Record record) {
        return GovernmentBondItem.builder()
                .value(record.getValue(INT_GOV_BOND_5_YEAR_JAPAN.VALUE))
                .date(record.getValue(INT_GOV_BOND_5_YEAR_JAPAN.DATE))
                .build();
    }

    public static GovernmentBondItem ofIntGovBond5YearUsa(Record record) {
        return GovernmentBondItem.builder()
                .value(record.getValue(INT_GOV_BOND_5_YEAR_USA.VALUE))
                .date(record.getValue(INT_GOV_BOND_5_YEAR_USA.DATE))
                .build();
    }

    public static GovernmentBondItem ofIntGovBond5YearFrance(Record record) {
        return GovernmentBondItem.builder()
                .value(record.getValue(INT_GOV_BOND_5_YEAR_FRANCE.VALUE))
                .date(record.getValue(INT_GOV_BOND_5_YEAR_FRANCE.DATE))
                .build();
    }

    public static GovernmentBondItem ofIntGovBond5YearGermany(Record record) {
        return GovernmentBondItem.builder()
                .value(record.getValue(INT_GOV_BOND_5_YEAR_GERMANY.VALUE))
                .date(record.getValue(INT_GOV_BOND_5_YEAR_GERMANY.DATE))
                .build();
    }

    public static GovernmentBondItem ofIntGovBond5YearNetherlands(Record record) {
        return GovernmentBondItem.builder()
                .value(record.getValue(INT_GOV_BOND_5_YEAR_NETHERLANDS.VALUE))
                .date(record.getValue(INT_GOV_BOND_5_YEAR_NETHERLANDS.DATE))
                .build();
    }

    public static GovernmentBondItem ofIntGovBond10YearDenmark(Record record) {
        return GovernmentBondItem.builder()
                .value(record.getValue(INT_GOV_BOND_10_YEAR_DENMARK.VALUE))
                .date(record.getValue(INT_GOV_BOND_10_YEAR_DENMARK.DATE))
                .build();
    }

    public static GovernmentBondItem ofIntGovBond10YearEur(Record record) {
        return GovernmentBondItem.builder()
                .value(record.getValue(INT_GOV_BOND_10_YEAR_EUR.VALUE))
                .date(record.getValue(INT_GOV_BOND_10_YEAR_EUR.DATE))
                .build();
    }

    public static GovernmentBondItem ofIntGovBond10YearFinland(Record record) {
        return GovernmentBondItem.builder()
                .value(record.getValue(INT_GOV_BOND_10_YEAR_FINLAND.VALUE))
                .date(record.getValue(INT_GOV_BOND_10_YEAR_FINLAND.DATE))
                .build();
    }

    public static GovernmentBondItem ofIntGovBond10YearFrance(Record record) {
        return GovernmentBondItem.builder()
                .value(record.getValue(INT_GOV_BOND_10_YEAR_FRANCE.VALUE))
                .date(record.getValue(INT_GOV_BOND_10_YEAR_FRANCE.DATE))
                .build();
    }

    public static GovernmentBondItem ofIntGovBond10YearGb(Record record) {
        return GovernmentBondItem.builder()
                .value(record.getValue(INT_GOV_BOND_10_YEAR_GB.VALUE))
                .date(record.getValue(INT_GOV_BOND_10_YEAR_GB.DATE))
                .build();
    }

    public static GovernmentBondItem ofIntGovBond10YearGermany(Record record) {
        return GovernmentBondItem.builder()
                .value(record.getValue(INT_GOV_BOND_10_YEAR_GERMANY.VALUE))
                .date(record.getValue(INT_GOV_BOND_10_YEAR_GERMANY.DATE))
                .build();
    }

    public static GovernmentBondItem ofIntGovBond10YearJapan(Record record) {
        return GovernmentBondItem.builder()
                .value(record.getValue(INT_GOV_BOND_10_YEAR_JAPAN.VALUE))
                .date(record.getValue(INT_GOV_BOND_10_YEAR_JAPAN.DATE))
                .build();
    }

    public static GovernmentBondItem ofIntGovBond10YearNetherlands(Record record) {
        return GovernmentBondItem.builder()
                .value(record.getValue(INT_GOV_BOND_10_YEAR_NETHERLANDS.VALUE))
                .date(record.getValue(INT_GOV_BOND_10_YEAR_NETHERLANDS.DATE))
                .build();
    }

    public static GovernmentBondItem ofIntGovBond10YearNorway(Record record) {
        return GovernmentBondItem.builder()
                .value(record.getValue(INT_GOV_BOND_10_YEAR_NORWAY.VALUE))
                .date(record.getValue(INT_GOV_BOND_10_YEAR_NORWAY.DATE))
                .build();
    }

    public static GovernmentBondItem ofIntGovBond10YearUsa(Record record) {
        return GovernmentBondItem.builder()
                .value(record.getValue(INT_GOV_BOND_10_YEAR_USA.VALUE))
                .date(record.getValue(INT_GOV_BOND_10_YEAR_USA.DATE))
                .build();
    }

}
