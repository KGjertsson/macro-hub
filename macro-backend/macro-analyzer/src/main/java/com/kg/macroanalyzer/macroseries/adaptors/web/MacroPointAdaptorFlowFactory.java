package com.kg.macroanalyzer.macroseries.adaptors.web;

import com.kg.macroanalyzer.macroseries.domain.MacroPoint;
import com.kg.macroanalyzer.scrape.domain.SeriesConfig;
import com.kg.macroanalyzer.shared.web.HttpGetter;
import com.kg.macroanalyzer.shared.web.WebAdaptorFlow;

import java.util.stream.Stream;

public record MacroPointAdaptorFlowFactory(
        String fedSubscriptionKey,
        String riksbankenSubscriptionKey
) {

    public WebAdaptorFlow<SeriesConfig, Stream<MacroPoint>> build(SeriesConfig seriesConfig) {
        switch (seriesConfig.name()) {
            case String name when name.startsWith("FED") -> {
                final var connectionBuilder = new FedConnectionBuilder(fedSubscriptionKey);
                final var getter = new HttpGetter();
                final var parser = new FedResponseParser();

                return new WebAdaptorFlow<>(connectionBuilder, getter, parser);
            }
            case String name when name.startsWith("Debt") -> {
                final var connectionBuilder = new EuroStatConnectionBuilder();
                final var getter = new HttpGetter();
                final var parser = new EuroStatResponseParser();

                return new WebAdaptorFlow<>(connectionBuilder, getter, parser);
            }
            case String name when isRiksbankenConfig(name) -> {
                final var connectionBuilder = new RiksbankenConnectionBuilder(riksbankenSubscriptionKey);
                final var getter = new HttpGetter();
                final var parser = new RiksbankenResponseParser();

                return new WebAdaptorFlow<>(connectionBuilder, getter, parser);
            }
            default -> throw new IllegalStateException("Unexpected value: " + seriesConfig.name());
        }
    }

    private boolean isRiksbankenConfig(String name) {
        return name.startsWith("Int")
                || name.startsWith("Euro")
                || name.startsWith("GovernmentBondSweden")
                || name.startsWith("GovernmentBillSweden")
                || name.equals("PolicyRateSweden")
                || name.equals("UsdSekExchangeRate");
    }

}
