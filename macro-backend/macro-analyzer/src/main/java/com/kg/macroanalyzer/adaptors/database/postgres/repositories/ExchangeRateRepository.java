package com.kg.macroanalyzer.adaptors.database.postgres.repositories;

import com.kg.macroanalyzer.application.domain.macroseries.MacroPoint;
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

import static com.kg.macroanalyzer.jooq.generated.tables.ExchangeUsdSek.EXCHANGE_USD_SEK;

@Repository
public class ExchangeRateRepository {

    private final DSLContext dslContext;

    @Autowired
    public ExchangeRateRepository(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    public Supplier<List<MacroPoint>> getExchangeRateUsdSek() {
        return () -> dslContext.select()
                .from(EXCHANGE_USD_SEK)
                .fetch()
                .map(this::toMacroPoint);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public Function<List<MacroPoint>, Integer> insertExchangeRateUsdSek() {
        return exchangeRateUsdSekList -> {
            if (!exchangeRateUsdSekList.isEmpty()) {
                final var insertQuery = dslContext.batch(
                        dslContext.insertInto(
                                EXCHANGE_USD_SEK,
                                EXCHANGE_USD_SEK.GLOBAL_ID,
                                EXCHANGE_USD_SEK.USD_SEK,
                                EXCHANGE_USD_SEK.USD_SEK_DATE
                        ).values(DSL.val((UUID) null), DSL.val(0.0), DSL.val(LocalDate.MIN))
                );
                exchangeRateUsdSekList.forEach(exchangeRateUsdSek -> insertQuery.bind(
                        UUID.randomUUID(),
                        exchangeRateUsdSek.value(),
                        exchangeRateUsdSek.date()
                ));

                final var response = insertQuery.execute();

                return response.length;
            }

            return 0;
        };
    }

    private MacroPoint toMacroPoint(Record r) {
        return MacroPoint.builder()
                .value(r.getValue(EXCHANGE_USD_SEK.USD_SEK))
                .date(r.getValue(EXCHANGE_USD_SEK.USD_SEK_DATE))
                .build();
    }

}
