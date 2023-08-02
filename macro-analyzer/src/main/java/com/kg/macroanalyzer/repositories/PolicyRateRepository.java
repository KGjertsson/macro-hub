package com.kg.macroanalyzer.repositories;

import com.kg.macroanalyzer.models.policyrate.PolicyRateItem;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

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
                .map(PolicyRateItem::of);
    }

}
