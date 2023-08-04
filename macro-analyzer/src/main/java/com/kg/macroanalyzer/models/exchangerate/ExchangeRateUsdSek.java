package com.kg.macroanalyzer.models.exchangerate;

import lombok.Builder;
import org.jooq.Record;

import java.time.LocalDate;

import static com.kg.macroanalyzer.jooq.generated.tables.ExchangeUsdSek.EXCHANGE_USD_SEK;

@Builder
public record ExchangeRateUsdSek(Double value, LocalDate date) {

    public static ExchangeRateUsdSek of(Record r) {
        return ExchangeRateUsdSek.builder()
                .value(r.getValue(EXCHANGE_USD_SEK.USD_SEK))
                .date(r.getValue(EXCHANGE_USD_SEK.USD_SEK_DATE))
                .build();
    }

}
