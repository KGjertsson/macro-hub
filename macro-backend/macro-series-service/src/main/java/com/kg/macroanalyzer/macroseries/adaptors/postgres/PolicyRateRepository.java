package com.kg.macroanalyzer.macroseries.adaptors.postgres;

import com.kg.macroanalyzer.macroseries.domain.MacroPoint;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.kg.macroanalyzer.macroseries.jooq.generated.Tables.POLICY_RATE_SWEDEN;

@Repository
public class PolicyRateRepository {

    private final DSLContext dslContext;

    @Autowired
    public PolicyRateRepository(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    public Supplier<List<MacroPoint>> policyRateSwedenReader() {
        return () -> dslContext.select()
                .from(POLICY_RATE_SWEDEN)
                .fetch()
                .map(this::toMacroPoint);
    }

    public Function<List<MacroPoint>, Integer> insertPolicyRateItemsSweden() {
        return MacroPointUpsert.writer(
                dslContext,
                POLICY_RATE_SWEDEN,
                POLICY_RATE_SWEDEN.GLOBAL_ID,
                POLICY_RATE_SWEDEN.POLICY_RATE,
                POLICY_RATE_SWEDEN.POLICY_RATE_DATE,
                POLICY_RATE_SWEDEN.UPDATED
        );
    }

    private MacroPoint toMacroPoint(Record r) {
        return MacroPoint.builder()
                .value(r.getValue(POLICY_RATE_SWEDEN.POLICY_RATE))
                .date(r.getValue(POLICY_RATE_SWEDEN.POLICY_RATE_DATE))
                .build();
    }

}
