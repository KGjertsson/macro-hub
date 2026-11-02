package com.kg.macroanalyzer.macroseries.adaptors.postgres;

import com.kg.macroanalyzer.macroseries.domain.MacroPoint;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.kg.macroanalyzer.macroseries.jooq.generated.Tables.FED_SERIES;

@Repository
public class FedRepository {

    private final DSLContext dslContext;

    @Autowired
    public FedRepository(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    public Supplier<List<MacroPoint>> fedReader(String seriesName) {
        return () -> dslContext.select()
                .from(FED_SERIES)
                .where(FED_SERIES.SERIES_NAME.eq(seriesName))
                .fetch()
                .map(r ->
                        MacroPoint.builder()
                                .value(r.getValue(FED_SERIES.VALUE))
                                .date(r.getValue(FED_SERIES.DATE))
                                .build()
                );
    }

    public Function<List<MacroPoint>, Integer> fedWriter(String seriesName) {
        return MacroPointUpsert.writer(
                dslContext,
                seriesName,
                FED_SERIES,
                FED_SERIES.SERIES_NAME,
                FED_SERIES.GLOBAL_ID,
                FED_SERIES.VALUE,
                FED_SERIES.DATE,
                FED_SERIES.UPDATED
        );
    }

}
