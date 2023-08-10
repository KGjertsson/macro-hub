package com.kg.macroanalyzer.repositories;

import com.kg.macroanalyzer.models.ExchangeRateUsdSek;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static com.kg.macroanalyzer.jooq.generated.tables.ExchangeUsdSek.EXCHANGE_USD_SEK;

@Repository
public class ExchangeRateRepository {

    private final DSLContext dslContext;

    @Autowired
    public ExchangeRateRepository(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    public List<ExchangeRateUsdSek> getExchangeRateUsdSek() {
        return dslContext.select()
                .from(EXCHANGE_USD_SEK)
                .fetch()
                .map(ExchangeRateUsdSek::of);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void insertExchangeRateUsdSek(List<ExchangeRateUsdSek> exchangeRateUsdSekList) {
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

            insertQuery.execute();
        }
    }

}
