package com.kg.macroanalyzer.models;

import lombok.Builder;
import org.jooq.Record;

import java.time.LocalDate;

import static com.kg.macroanalyzer.jooq.generated.Tables.POLICY_RATE_SWEDEN;

@Builder
public record PolicyRateItem(Double value, LocalDate date) {

    public static PolicyRateItem ofSweden(Record r) {
        return PolicyRateItem.builder()
                .value(r.getValue(POLICY_RATE_SWEDEN.POLICY_RATE))
                .date(r.getValue(POLICY_RATE_SWEDEN.POLICY_RATE_DATE))
                .build();
    }

}
