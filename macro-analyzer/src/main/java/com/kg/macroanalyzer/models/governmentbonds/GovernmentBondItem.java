package com.kg.macroanalyzer.models.governmentbonds;

import lombok.Builder;
import org.jooq.Record;

import java.time.LocalDate;

import static com.kg.macroanalyzer.jooq.generated.Tables.*;

@Builder
public record GovernmentBondItem(Double value, LocalDate date) {

    public static GovernmentBondItem ofSwedish2Month(Record r) {
        return GovernmentBondItem.builder()
                .value(r.getValue(SWEDISH_GOVERNMENT_BONDS_2_MONTH.VALUE))
                .date(r.getValue(SWEDISH_GOVERNMENT_BONDS_2_MONTH.DATE))
                .build();
    }

    public static GovernmentBondItem ofSwedish5Month(Record r) {
        return GovernmentBondItem.builder()
                .value(r.getValue(SWEDISH_GOVERNMENT_BONDS_5_MONTH.VALUE))
                .date(r.getValue(SWEDISH_GOVERNMENT_BONDS_5_MONTH.DATE))
                .build();
    }

    public static GovernmentBondItem ofSwedish7Month(Record r) {
        return GovernmentBondItem.builder()
                .value(r.getValue(SWEDISH_GOVERNMENT_BONDS_7_MONTH.VALUE))
                .date(r.getValue(SWEDISH_GOVERNMENT_BONDS_7_MONTH.DATE))
                .build();
    }

    public static GovernmentBondItem ofSwedish10Month(Record r) {
        return GovernmentBondItem.builder()
                .value(r.getValue(SWEDISH_GOVERNMENT_BONDS_10_MONTH.VALUE))
                .date(r.getValue(SWEDISH_GOVERNMENT_BONDS_10_MONTH.DATE))
                .build();
    }

}
