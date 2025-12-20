package com.kg.macroanalyzer.adaptors.database.postgres.repositories;

import com.kg.macroanalyzer.application.domain.MacroPoint;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.function.Supplier;

import static com.kg.macroanalyzer.jooq.generated.Tables.EUROSTAT_NATIONAL_DEBT_SERIES;

@Repository
public class EurostatRepository {

    private final DSLContext dslContext;

    @Autowired
    public EurostatRepository(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    public Supplier<List<MacroPoint>> eurostatReader(String seriesName) {
        return () -> dslContext.select()
                .from(EUROSTAT_NATIONAL_DEBT_SERIES)
                .where(EUROSTAT_NATIONAL_DEBT_SERIES.SERIES_NAME.eq(seriesName))
                .fetch()
                .map(r ->
                        MacroPoint.builder()
                                .value(r.getValue(EUROSTAT_NATIONAL_DEBT_SERIES.VALUE))
                                .date(r.getValue(EUROSTAT_NATIONAL_DEBT_SERIES.DATE))
                                .build()
                );
    }

}
