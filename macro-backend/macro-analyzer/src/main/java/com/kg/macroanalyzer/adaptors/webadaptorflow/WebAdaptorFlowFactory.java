package com.kg.macroanalyzer.adaptors.webadaptorflow;

import com.kg.macroanalyzer.adaptors.webadaptorflow.connectionbuilder.EuroStatConnectionBuilder;
import com.kg.macroanalyzer.adaptors.webadaptorflow.connectionbuilder.FedConnectionBuilder;
import com.kg.macroanalyzer.adaptors.webadaptorflow.connectionbuilder.RiksbankenConnectionBuilder;
import com.kg.macroanalyzer.adaptors.webadaptorflow.getter.HttpGetter;
import com.kg.macroanalyzer.adaptors.webadaptorflow.responserparser.EuroStatResponseParser;
import com.kg.macroanalyzer.adaptors.webadaptorflow.responserparser.FedResponseParser;
import com.kg.macroanalyzer.adaptors.webadaptorflow.responserparser.RiksbankenResponseParser;
import com.kg.macroanalyzer.application.domain.macroseries.MacroPoint;
import com.kg.macroanalyzer.application.ports.driving.out.seriesconfig.SeriesConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.stream.Stream;

@Component
public record WebAdaptorFlowFactory(
        @Value("${fed.prod.subscription.key}") String fedSubscriptionKey,
        @Value("${riksbanken.prod.subscription.key}") String riksbankenSubscriptionKey
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
        return name.startsWith("Int") || name.startsWith("Euro") || name.equals("PolicyRateSweden");
    }

}
