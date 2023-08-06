package com.kg.macroanalyzer.repositories;

import com.kg.macroanalyzer.models.policyrate.PolicyRateItem;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static com.kg.macroanalyzer.jooq.generated.Tables.POLICY_RATE_SWEDEN;

@Repository
public class PolicyRateRepository {

    private final DSLContext dslContext;

    @Autowired
    public PolicyRateRepository(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    public List<PolicyRateItem> getPolicyRateSweden() {
        return dslContext.select()
                .from(POLICY_RATE_SWEDEN)
                .fetch()
                .map(PolicyRateItem::ofSweden);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void insertPolicyRateItemsSweden(List<PolicyRateItem> policyRateItemList) {
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

            insertQuery.execute();
        }
    }

}
