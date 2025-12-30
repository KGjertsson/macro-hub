package com.kg.macroanalyzer.adaptors.database.postgres.repositories;

import com.kg.macroanalyzer.application.domain.macroseries.MacroPoint;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.kg.macroanalyzer.jooq.generated.Tables.FED_SERIES;

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

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public Function<List<MacroPoint>, Integer> fedWriter(String seriesName) {
        return macroPointList -> {
            if (!macroPointList.isEmpty()) {
                final var insertQuery = dslContext.batch(
                        dslContext.insertInto(
                                FED_SERIES,
                                FED_SERIES.GLOBAL_ID,
                                FED_SERIES.SERIES_NAME,
                                FED_SERIES.VALUE,
                                FED_SERIES.DATE
                        ).values(
                                DSL.val((UUID) null),
                                DSL.val(""),
                                DSL.val(0.0),
                                DSL.val(LocalDate.MIN)
                        )
                );
                macroPointList.forEach(macroPoint -> insertQuery.bind(
                        UUID.randomUUID(),
                        seriesName,
                        macroPoint.value(),
                        macroPoint.date()
                ));
                final var result = insertQuery.execute();

                return result.length;
            }

            return 0;
        };
    }

}
