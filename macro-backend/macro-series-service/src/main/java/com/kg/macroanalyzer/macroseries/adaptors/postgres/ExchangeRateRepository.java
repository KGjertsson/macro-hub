package com.kg.macroanalyzer.macroseries.adaptors.postgres;

import com.kg.macroanalyzer.macroseries.domain.MacroPoint;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.kg.macroanalyzer.macroseries.jooq.generated.tables.ExchangeUsdSek.EXCHANGE_USD_SEK;

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

    public Function<List<MacroPoint>, Integer> insertExchangeRateUsdSek() {
        return MacroPointUpsert.writer(
                dslContext,
                EXCHANGE_USD_SEK,
                EXCHANGE_USD_SEK.GLOBAL_ID,
                EXCHANGE_USD_SEK.USD_SEK,
                EXCHANGE_USD_SEK.USD_SEK_DATE,
                EXCHANGE_USD_SEK.UPDATED
        );
    }

    private MacroPoint toMacroPoint(Record r) {
        return MacroPoint.builder()
                .value(r.getValue(EXCHANGE_USD_SEK.USD_SEK))
                .date(r.getValue(EXCHANGE_USD_SEK.USD_SEK_DATE))
                .build();
    }

}
