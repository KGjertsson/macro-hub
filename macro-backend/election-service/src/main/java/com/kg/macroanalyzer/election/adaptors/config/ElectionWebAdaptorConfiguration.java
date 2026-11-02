package com.kg.macroanalyzer.election.adaptors.config;

import com.kg.macroanalyzer.election.adaptors.web.ParliamentPartiesClient;
import com.kg.macroanalyzer.election.adaptors.web.RefreshingPartyColorAdaptor;
import com.kg.macroanalyzer.election.adaptors.web.ValmyndighetenIndexWebAdaptor;
import com.kg.macroanalyzer.election.adaptors.web.ValmyndighetenWebAdaptor;
import com.kg.macroanalyzer.election.application.port.out.LocalElectionFile;
import com.kg.macroanalyzer.election.application.port.out.LocalElectionIndexRequest;
import com.kg.macroanalyzer.election.domain.LocalElectionResult;
import com.kg.macroanalyzer.shared.web.WebPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;
import java.util.List;

@Configuration
public class ElectionWebAdaptorConfiguration {

    @Bean("localElectionIndexWebAdaptor")
    public WebPort<LocalElectionIndexRequest, List<LocalElectionFile>> localElectionIndexWebAdaptor() {
        return new ValmyndighetenIndexWebAdaptor();
    }

    @Bean("localElectionWebAdaptor")
    public WebPort<LocalElectionFile, List<LocalElectionResult>> localElectionWebAdaptor() {
        return new ValmyndighetenWebAdaptor();
    }

    @Bean
    public ParliamentPartiesClient parliamentPartiesClient(
            @Value("${election.parliament.url}") String parliamentUrl,
            @Value("${election.parliament.timeout}") Duration timeout,
            JsonMapper jsonMapper
    ) {
        return new ParliamentPartiesClient(parliamentUrl, timeout, jsonMapper);
    }

    @Bean
    public RefreshingPartyColorAdaptor partyColorAdaptor(
            ParliamentPartiesClient client,
            @Value("${election.parliament.refresh-interval}") Duration refreshInterval,
            @Value("${election.parliament.retry-interval}") Duration retryInterval
    ) {
        return new RefreshingPartyColorAdaptor(client, refreshInterval, retryInterval);
    }

}
