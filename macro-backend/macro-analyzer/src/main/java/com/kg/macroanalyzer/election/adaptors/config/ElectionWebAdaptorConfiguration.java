package com.kg.macroanalyzer.election.adaptors.config;

import com.kg.macroanalyzer.election.adaptors.web.ValmyndighetenIndexWebAdaptor;
import com.kg.macroanalyzer.election.adaptors.web.ValmyndighetenWebAdaptor;
import com.kg.macroanalyzer.election.application.port.out.LocalElectionFile;
import com.kg.macroanalyzer.election.application.port.out.LocalElectionIndexRequest;
import com.kg.macroanalyzer.election.domain.LocalElectionResult;
import com.kg.macroanalyzer.shared.web.WebPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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

}
