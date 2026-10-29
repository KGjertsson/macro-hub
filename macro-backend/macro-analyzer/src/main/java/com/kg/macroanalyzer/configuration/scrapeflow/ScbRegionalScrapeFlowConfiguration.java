package com.kg.macroanalyzer.configuration.scrapeflow;

import com.kg.macroanalyzer.adaptors.database.postgres.models.ScrapeQueueItem;
import com.kg.macroanalyzer.application.domain.scb.ScbTableMetadata;
import com.kg.macroanalyzer.application.domain.scb.ScbTableValues;
import com.kg.macroanalyzer.application.ports.driven.ConfigWithScbIndicators;
import com.kg.macroanalyzer.application.ports.driven.DatabasePort;
import com.kg.macroanalyzer.application.ports.driven.ScbDataRequest;
import com.kg.macroanalyzer.application.ports.driven.ScbMetadataRequest;
import com.kg.macroanalyzer.application.ports.driven.WebPort;
import com.kg.macroanalyzer.application.ports.driving.out.seriesconfig.SeriesConfig;
import com.kg.macroanalyzer.application.services.scrape.scrapeflow.ScrapeFlow;
import com.kg.macroanalyzer.application.services.scrape.scrapeflow.existingfinder.ExistingFinder;
import com.kg.macroanalyzer.application.services.scrape.scrapeflow.existingfinder.ExistingScbTableFinder;
import com.kg.macroanalyzer.application.services.scrape.scrapeflow.persister.Persister;
import com.kg.macroanalyzer.application.services.scrape.scrapeflow.persister.ScbRegionalPersister;
import com.kg.macroanalyzer.application.services.scrape.scrapeflow.preparer.Preparer;
import com.kg.macroanalyzer.application.services.scrape.scrapeflow.scraper.ScbRegionalScraper;
import com.kg.macroanalyzer.application.services.scrape.scrapeflow.scraper.Scraper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ScbRegionalScrapeFlowConfiguration {

    @Bean("existingScbTableFinder")
    public ExistingFinder<SeriesConfig, ConfigWithScbIndicators> finder(DatabasePort databasePort) {
        return new ExistingScbTableFinder(databasePort);
    }

    @Bean("scbRegionalScraper")
    public Scraper<ConfigWithScbIndicators> scraper(
            WebPort<ScbMetadataRequest, ScbTableMetadata> scbMetadataWebPort,
            WebPort<ScbDataRequest, ScbTableValues> scbDataWebPort
    ) {
        return new ScbRegionalScraper(scbMetadataWebPort, scbDataWebPort, ScbRegionalScraper.SCB_MAX_CELLS);
    }

    @Bean("scbRegionalPersister")
    public Persister<ConfigWithScbIndicators> persister(DatabasePort databasePort) {
        return new ScbRegionalPersister(databasePort);
    }

    @Bean("scbRegionalScrapeFlow")
    public ScrapeFlow<ScrapeQueueItem, SeriesConfig, ConfigWithScbIndicators> scrapeFlow(
            Preparer<ScrapeQueueItem, SeriesConfig> preparer,
            @Qualifier("existingScbTableFinder") ExistingFinder<SeriesConfig, ConfigWithScbIndicators> finder,
            @Qualifier("scbRegionalScraper") Scraper<ConfigWithScbIndicators> scraper,
            @Qualifier("scbRegionalPersister") Persister<ConfigWithScbIndicators> persister
    ) {
        return new ScrapeFlow<>(
                preparer,
                finder,
                scraper,
                persister
        );
    }

}
