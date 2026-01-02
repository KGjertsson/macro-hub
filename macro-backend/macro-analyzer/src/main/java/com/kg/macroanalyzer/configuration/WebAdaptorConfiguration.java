package com.kg.macroanalyzer.configuration;

import com.kg.macroanalyzer.adaptors.webadaptor.MacroPointWebAdaptor;
import com.kg.macroanalyzer.adaptors.webadaptor.MembersOfParliamentWebAdaptor;
import com.kg.macroanalyzer.adaptors.webadaptor.webadaptorflow.MacroPointAdaptorFlowFactory;
import com.kg.macroanalyzer.application.domain.macroseries.MacroPoint;
import com.kg.macroanalyzer.application.domain.parliament.MemberOfParliament;
import com.kg.macroanalyzer.application.ports.driven.WebPort;
import com.kg.macroanalyzer.application.ports.driving.out.seriesconfig.SeriesConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.stream.Stream;

@Configuration
public class WebAdaptorConfiguration {

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

    @Bean("memberWebAdaptor")
    public WebPort<SeriesConfig, Stream<MemberOfParliament>> memberWebAdaptor() {
        return new MembersOfParliamentWebAdaptor();
    }

}
