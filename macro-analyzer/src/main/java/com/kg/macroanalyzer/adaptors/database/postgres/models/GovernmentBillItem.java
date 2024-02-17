package com.kg.macroanalyzer.adaptors.database.postgres.models;

import lombok.Builder;
import org.jooq.Record;

import java.time.LocalDate;

import static com.kg.macroanalyzer.jooq.generated.Tables.*;

@Builder
public record GovernmentBillItem(Double value, LocalDate date) {

    public static GovernmentBillItem ofSwedish1Month(Record r) {
        return GovernmentBillItem.builder()
                .value(r.getValue(SWEDISH_GOVERNMENT_BILL_1_MONTH.VALUE))
                .date(r.getValue(SWEDISH_GOVERNMENT_BILL_1_MONTH.DATE))
                .build();
    }

    public static GovernmentBillItem ofSwedish3Month(Record r) {
        return GovernmentBillItem.builder()
                .value(r.getValue(SWEDISH_GOVERNMENT_BILL_3_MONTH.VALUE))
                .date(r.getValue(SWEDISH_GOVERNMENT_BILL_3_MONTH.DATE))
                .build();
    }

    public static GovernmentBillItem ofSwedish6Month(Record r) {
        return GovernmentBillItem.builder()
                .value(r.getValue(SWEDISH_GOVERNMENT_BILL_6_MONTH.VALUE))
                .date(r.getValue(SWEDISH_GOVERNMENT_BILL_6_MONTH.DATE))
                .build();
    }

    public static GovernmentBillItem ofSwedish12Month(Record r) {
        return GovernmentBillItem.builder()
                .value(r.getValue(SWEDISH_GOVERNMENT_BILL_12_MONTH.VALUE))
                .date(r.getValue(SWEDISH_GOVERNMENT_BILL_12_MONTH.DATE))
                .build();
    }

}
