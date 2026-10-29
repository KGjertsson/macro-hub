package com.kg.macroanalyzer.election.adaptors.config;

import com.kg.macroanalyzer.election.application.port.out.ConfigWithLocalElections;
import com.kg.macroanalyzer.election.application.port.out.ElectionRepositoryPort;
import com.kg.macroanalyzer.election.application.port.out.LocalElectionFile;
import com.kg.macroanalyzer.election.application.port.out.LocalElectionIndexRequest;
import com.kg.macroanalyzer.election.application.service.scrape.ExistingLocalElectionFinder;
import com.kg.macroanalyzer.election.application.service.scrape.LocalElectionPersister;
import com.kg.macroanalyzer.election.application.service.scrape.LocalElectionScraper;
import com.kg.macroanalyzer.election.domain.LocalElectionResult;
import com.kg.macroanalyzer.scrape.application.port.out.ScrapeQueuePort;
import com.kg.macroanalyzer.scrape.application.service.scrapeflow.ExistingFinder;
import com.kg.macroanalyzer.scrape.application.service.scrapeflow.KindedScrapeFlow;
import com.kg.macroanalyzer.scrape.application.service.scrapeflow.Persister;
import com.kg.macroanalyzer.scrape.application.service.scrapeflow.Preparer;
import com.kg.macroanalyzer.scrape.application.service.scrapeflow.ScrapeFlow;
import com.kg.macroanalyzer.scrape.application.service.scrapeflow.Scraper;
import com.kg.macroanalyzer.scrape.domain.ScrapeQueueItem;
import com.kg.macroanalyzer.scrape.domain.ScrapeQueueKind;
import com.kg.macroanalyzer.scrape.domain.SeriesConfig;
import com.kg.macroanalyzer.shared.web.WebPort;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class LocalElectionScrapeFlowConfiguration {

    @Bean("existingLocalElectionFinder")
    public ExistingFinder<SeriesConfig, ConfigWithLocalElections> finder(ElectionRepositoryPort repositoryPort) {
        return new ExistingLocalElectionFinder(repositoryPort);
    }

    @Bean("localElectionScraper")
    public Scraper<ConfigWithLocalElections> scraper(
            WebPort<LocalElectionIndexRequest, List<LocalElectionFile>> localElectionIndexWebPort,
            WebPort<LocalElectionFile, List<LocalElectionResult>> localElectionWebPort,
            @Value("${valmyndigheten.delay-ms:200}") long delayMs
    ) {
        return new LocalElectionScraper(localElectionIndexWebPort, localElectionWebPort, delayMs);
    }

    @Bean("localElectionPersister")
    public Persister<ConfigWithLocalElections> persister(ElectionRepositoryPort repositoryPort, ScrapeQueuePort scrapeQueuePort) {
        return new LocalElectionPersister(repositoryPort, scrapeQueuePort);
    }

    @Bean("localElectionScrapeFlow")
    public KindedScrapeFlow scrapeFlow(
            Preparer<ScrapeQueueItem, SeriesConfig> preparer,
            @Qualifier("existingLocalElectionFinder") ExistingFinder<SeriesConfig, ConfigWithLocalElections> finder,
            @Qualifier("localElectionScraper") Scraper<ConfigWithLocalElections> scraper,
            @Qualifier("localElectionPersister") Persister<ConfigWithLocalElections> persister
    ) {
        return KindedScrapeFlow.of(ScrapeQueueKind.LOCAL_ELECTION, new ScrapeFlow<>(
                preparer,
                finder,
                scraper,
                persister
        ));
    }

}
