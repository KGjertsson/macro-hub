package com.kg.macroanalyzer.models.policyrate;

import lombok.Builder;
import org.jooq.Record;

import java.time.LocalDate;

import static com.kg.macroanalyzer.jooq.generated.Tables.POLICY_RATE_SWEDEN;

@Builder
public record PolicyRateItem(Double value, LocalDate date) {

    public static PolicyRateItem of(Record r) {
        return PolicyRateItem.builder()
                .value(r.getValue(POLICY_RATE_SWEDEN.POLICY_RATE))
                .date(r.getValue(POLICY_RATE_SWEDEN.POLICY_RATE_DATE))
                .build();
    }

}
