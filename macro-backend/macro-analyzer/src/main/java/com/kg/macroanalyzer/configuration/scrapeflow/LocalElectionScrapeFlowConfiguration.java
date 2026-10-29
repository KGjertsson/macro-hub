package com.kg.macroanalyzer.configuration.scrapeflow;

import com.kg.macroanalyzer.application.domain.localelection.LocalElectionResult;
import com.kg.macroanalyzer.application.ports.driven.ConfigWithLocalElections;
import com.kg.macroanalyzer.application.ports.driven.DatabasePort;
import com.kg.macroanalyzer.application.ports.driven.LocalElectionFile;
import com.kg.macroanalyzer.application.ports.driven.LocalElectionIndexRequest;
import com.kg.macroanalyzer.application.services.scrape.scrapeflow.existingfinder.ExistingLocalElectionFinder;
import com.kg.macroanalyzer.application.services.scrape.scrapeflow.persister.LocalElectionPersister;
import com.kg.macroanalyzer.application.services.scrape.scrapeflow.scraper.LocalElectionScraper;
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
    public ExistingFinder<SeriesConfig, ConfigWithLocalElections> finder(DatabasePort databasePort) {
        return new ExistingLocalElectionFinder(databasePort);
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
    public Persister<ConfigWithLocalElections> persister(DatabasePort databasePort, ScrapeQueuePort scrapeQueuePort) {
        return new LocalElectionPersister(databasePort, scrapeQueuePort);
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
