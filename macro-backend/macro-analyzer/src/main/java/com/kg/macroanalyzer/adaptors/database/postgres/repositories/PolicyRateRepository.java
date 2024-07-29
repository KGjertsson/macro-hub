package com.kg.macroanalyzer.adaptors.database.postgres.repositories;

import com.kg.macroanalyzer.application.domain.MacroPoint;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.kg.macroanalyzer.jooq.generated.Tables.POLICY_RATE_SWEDEN;

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

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public Function<List<MacroPoint>, Integer> insertPolicyRateItemsSweden() {
        return policyRateItemList -> {
            if (!policyRateItemList.isEmpty()) {
                final var insertQuery = dslContext.batch(
                        dslContext.insertInto(
                                POLICY_RATE_SWEDEN,
                                POLICY_RATE_SWEDEN.GLOBAL_ID,
                                POLICY_RATE_SWEDEN.POLICY_RATE,
                                POLICY_RATE_SWEDEN.POLICY_RATE_DATE
                        ).values(DSL.val((UUID) null), DSL.val(0.0), DSL.val(LocalDate.MIN))
                );
                policyRateItemList.forEach(policyRateItemSweden -> insertQuery.bind(
                        UUID.randomUUID(),
                        policyRateItemSweden.value(),
                        policyRateItemSweden.date()
                ));

                final var response = insertQuery.execute();

                return response.length;
            }

            return 0;
        };
    }

    private MacroPoint toMacroPoint(Record r) {
        return MacroPoint.builder()
                .value(r.getValue(POLICY_RATE_SWEDEN.POLICY_RATE))
                .date(r.getValue(POLICY_RATE_SWEDEN.POLICY_RATE_DATE))
                .build();
    }

}
