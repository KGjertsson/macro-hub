package com.kg.macroanalyzer.regionalstatistics.adaptors.config;

import com.kg.macroanalyzer.regionalstatistics.application.port.out.ConfigWithScbIndicators;
import com.kg.macroanalyzer.regionalstatistics.application.port.out.RegionalStatisticsRepositoryPort;
import com.kg.macroanalyzer.regionalstatistics.application.port.out.ScbDataRequest;
import com.kg.macroanalyzer.regionalstatistics.application.port.out.ScbMetadataRequest;
import com.kg.macroanalyzer.regionalstatistics.application.service.scrape.ExistingScbTableFinder;
import com.kg.macroanalyzer.regionalstatistics.application.service.scrape.ScbRegionalPersister;
import com.kg.macroanalyzer.regionalstatistics.application.service.scrape.ScbRegionalScraper;
import com.kg.macroanalyzer.regionalstatistics.domain.RegionalStatisticsScrapeFlows;
import com.kg.macroanalyzer.regionalstatistics.domain.ScbTableMetadata;
import com.kg.macroanalyzer.regionalstatistics.domain.ScbTableValues;
import com.kg.macroanalyzer.scrape.application.port.out.ScrapeQueuePort;
import com.kg.macroanalyzer.scrape.application.service.scrapeflow.ExistingFinder;
import com.kg.macroanalyzer.scrape.application.service.scrapeflow.Persister;
import com.kg.macroanalyzer.scrape.application.service.scrapeflow.Preparer;
import com.kg.macroanalyzer.scrape.application.service.scrapeflow.RegisteredScrapeFlow;
import com.kg.macroanalyzer.scrape.application.service.scrapeflow.ScrapeFlow;
import com.kg.macroanalyzer.scrape.application.service.scrapeflow.Scraper;
import com.kg.macroanalyzer.scrape.domain.ScrapeQueueItem;
import com.kg.macroanalyzer.scrape.domain.SeriesConfig;
import com.kg.macroanalyzer.shared.web.WebPort;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ScbRegionalScrapeFlowConfiguration {

    @Bean("existingScbTableFinder")
    public ExistingFinder<SeriesConfig, ConfigWithScbIndicators> finder(RegionalStatisticsRepositoryPort repositoryPort) {
        return new ExistingScbTableFinder(repositoryPort);
    }

    @Bean("scbRegionalScraper")
    public Scraper<ConfigWithScbIndicators> scraper(
            WebPort<ScbMetadataRequest, ScbTableMetadata> scbMetadataWebPort,
            WebPort<ScbDataRequest, ScbTableValues> scbDataWebPort
    ) {
        return new ScbRegionalScraper(scbMetadataWebPort, scbDataWebPort, ScbRegionalScraper.SCB_MAX_CELLS);
    }

    @Bean("scbRegionalPersister")
    public Persister<ConfigWithScbIndicators> persister(RegionalStatisticsRepositoryPort repositoryPort, ScrapeQueuePort scrapeQueuePort) {
        return new ScbRegionalPersister(repositoryPort, scrapeQueuePort);
    }

    @Bean("scbRegionalScrapeFlow")
    public RegisteredScrapeFlow scrapeFlow(
            Preparer<ScrapeQueueItem, SeriesConfig> preparer,
            @Qualifier("existingScbTableFinder") ExistingFinder<SeriesConfig, ConfigWithScbIndicators> finder,
            @Qualifier("scbRegionalScraper") Scraper<ConfigWithScbIndicators> scraper,
            @Qualifier("scbRegionalPersister") Persister<ConfigWithScbIndicators> persister
    ) {
        return RegisteredScrapeFlow.of(RegionalStatisticsScrapeFlows.SCB_REGIONAL, new ScrapeFlow<>(
                preparer,
                finder,
                scraper,
                persister
        ));
    }

}
