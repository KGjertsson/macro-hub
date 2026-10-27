package com.kg.macroanalyzer.configuration.scrapeflow;

import com.kg.macroanalyzer.adaptors.database.postgres.models.ScrapeQueueItem;
import com.kg.macroanalyzer.application.domain.localelection.LocalElectionResult;
import com.kg.macroanalyzer.application.ports.driven.ConfigWithLocalElections;
import com.kg.macroanalyzer.application.ports.driven.DatabasePort;
import com.kg.macroanalyzer.application.ports.driven.LocalElectionFile;
import com.kg.macroanalyzer.application.ports.driven.LocalElectionIndexRequest;
import com.kg.macroanalyzer.application.ports.driven.WebPort;
import com.kg.macroanalyzer.application.ports.driving.out.seriesconfig.SeriesConfig;
import com.kg.macroanalyzer.application.services.scrape.scrapeflow.ScrapeFlow;
import com.kg.macroanalyzer.application.services.scrape.scrapeflow.existingfinder.ExistingFinder;
import com.kg.macroanalyzer.application.services.scrape.scrapeflow.existingfinder.ExistingLocalElectionFinder;
import com.kg.macroanalyzer.application.services.scrape.scrapeflow.persister.LocalElectionPersister;
import com.kg.macroanalyzer.application.services.scrape.scrapeflow.persister.Persister;
import com.kg.macroanalyzer.application.services.scrape.scrapeflow.preparer.Preparer;
import com.kg.macroanalyzer.application.services.scrape.scrapeflow.scraper.LocalElectionScraper;
import com.kg.macroanalyzer.application.services.scrape.scrapeflow.scraper.Scraper;
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
    public Persister<ConfigWithLocalElections> persister(DatabasePort databasePort) {
        return new LocalElectionPersister(databasePort);
    }

    @Bean("localElectionScrapeFlow")
    public ScrapeFlow<ScrapeQueueItem, SeriesConfig, ConfigWithLocalElections> scrapeFlow(
            Preparer<ScrapeQueueItem, SeriesConfig> preparer,
            @Qualifier("existingLocalElectionFinder") ExistingFinder<SeriesConfig, ConfigWithLocalElections> finder,
            @Qualifier("localElectionScraper") Scraper<ConfigWithLocalElections> scraper,
            @Qualifier("localElectionPersister") Persister<ConfigWithLocalElections> persister
    ) {
        return new ScrapeFlow<>(
                preparer,
                finder,
                scraper,
                persister
        );
    }

}
