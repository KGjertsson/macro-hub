package com.kg.macroanalyzer.models;

import lombok.Builder;
import lombok.Value;
import org.jooq.Record;

import java.time.LocalDateTime;

import static com.kg.macroanalyzer.jooq.generated.Tables.POLICY_RATE_SWEDEN;

@Builder
@Value
public class PolicyRateItem {

    Double policyRate;
    LocalDateTime dateTime;

    public static PolicyRateItem of(Record r) {
        return PolicyRateItem.builder()
                .policyRate(r.getValue(POLICY_RATE_SWEDEN.POLICY_RATE))
                .dateTime(r.getValue(POLICY_RATE_SWEDEN.POLICY_RATE_DATE))
                .build();
    }
}
