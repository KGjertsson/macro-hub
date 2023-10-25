package com.kg.macroanalyzer.configuration;

import org.jooq.DSLContext;
import org.jooq.meta.CatalogDefinition;
import org.jooq.meta.CatalogVersionProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import static com.kg.macroanalyzer.jooq.generated.Tables.FLYWAY_SCHEMA_HISTORY;

@Configuration
public class FlywayCatalogVersion implements CatalogVersionProvider {

    final DSLContext dslContext;

    @Autowired
    public FlywayCatalogVersion(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    @Override
    public String version(CatalogDefinition catalogDefinition) {
        return dslContext.select(FLYWAY_SCHEMA_HISTORY.VERSION)
                .from(FLYWAY_SCHEMA_HISTORY)
                .orderBy(FLYWAY_SCHEMA_HISTORY.VERSION)
                .fetch()
                .map(r -> r.get(FLYWAY_SCHEMA_HISTORY.VERSION))
                .getFirst();
    }
}
