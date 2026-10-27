package com.kg.macroanalyzer.configuration;

import com.kg.macroanalyzer.adaptors.webadaptor.MacroPointWebAdaptor;
import com.kg.macroanalyzer.adaptors.webadaptor.MembersOfParliamentWebAdaptor;
import com.kg.macroanalyzer.adaptors.webadaptor.RiksdagenActivityWebAdaptor;
import com.kg.macroanalyzer.adaptors.webadaptor.ValmyndighetenIndexWebAdaptor;
import com.kg.macroanalyzer.adaptors.webadaptor.ValmyndighetenWebAdaptor;
import com.kg.macroanalyzer.adaptors.webadaptor.webadaptorflow.MacroPointAdaptorFlowFactory;
import com.kg.macroanalyzer.application.domain.localelection.LocalElectionResult;
import com.kg.macroanalyzer.application.domain.macroseries.MacroPoint;
import com.kg.macroanalyzer.application.domain.parliament.MemberActivity;
import com.kg.macroanalyzer.application.domain.parliament.MemberOfParliament;
import com.kg.macroanalyzer.application.ports.driven.LocalElectionFile;
import com.kg.macroanalyzer.application.ports.driven.LocalElectionIndexRequest;
import com.kg.macroanalyzer.application.ports.driven.MemberActivityRequest;
import com.kg.macroanalyzer.application.ports.driven.WebPort;
import com.kg.macroanalyzer.application.ports.driving.out.seriesconfig.SeriesConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
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

    @Bean("memberActivityWebAdaptor")
    public WebPort<MemberActivityRequest, MemberActivity> memberActivityWebAdaptor() {
        return new RiksdagenActivityWebAdaptor();
    }

    @Bean("localElectionIndexWebAdaptor")
    public WebPort<LocalElectionIndexRequest, List<LocalElectionFile>> localElectionIndexWebAdaptor() {
        return new ValmyndighetenIndexWebAdaptor();
    }

    @Bean("localElectionWebAdaptor")
    public WebPort<LocalElectionFile, List<LocalElectionResult>> localElectionWebAdaptor() {
        return new ValmyndighetenWebAdaptor();
    }

}
