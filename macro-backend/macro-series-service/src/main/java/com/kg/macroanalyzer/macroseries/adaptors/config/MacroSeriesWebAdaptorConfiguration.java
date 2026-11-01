package com.kg.macroanalyzer.macroseries.adaptors.config;

import com.kg.macroanalyzer.macroseries.adaptors.web.MacroPointAdaptorFlowFactory;
import com.kg.macroanalyzer.macroseries.adaptors.web.MacroPointWebAdaptor;
import com.kg.macroanalyzer.macroseries.domain.MacroPoint;
import com.kg.macroanalyzer.scrape.domain.SeriesConfig;
import com.kg.macroanalyzer.shared.web.WebPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.stream.Stream;

@Configuration
public class MacroSeriesWebAdaptorConfiguration {

    @Value("${fed.prod.subscription.key}")
    String fedSubscriptionKey;

    @Value("${riksbanken.prod.subscription.key}")
    String riksbankenSubscriptionKey;

    @Bean("macroPointWebAdaptor")
    public WebPort<SeriesConfig, Stream<MacroPoint>> macroPointWebAdaptor() {
        final var macroPointAdaptorFlowFactory = new MacroPointAdaptorFlowFactory(
                fedSubscriptionKey,
                riksbankenSubscriptionKey
        );

        return new MacroPointWebAdaptor(macroPointAdaptorFlowFactory);
    }

}
