package com.kg.macroanalyzer.adaptors.database.postgres.repositories;

import com.kg.macroanalyzer.application.domain.MacroPoint;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
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

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public Function<List<MacroPoint>, Integer> eurostatWriter(String seriesName) {
        return macroPointList -> {
            if (!macroPointList.isEmpty()) {
                final var insertQuery = dslContext.batch(
                        dslContext.insertInto(
                                EUROSTAT_NATIONAL_DEBT_SERIES,
                                EUROSTAT_NATIONAL_DEBT_SERIES.GLOBAL_ID,
                                EUROSTAT_NATIONAL_DEBT_SERIES.SERIES_NAME,
                                EUROSTAT_NATIONAL_DEBT_SERIES.VALUE,
                                EUROSTAT_NATIONAL_DEBT_SERIES.DATE
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
