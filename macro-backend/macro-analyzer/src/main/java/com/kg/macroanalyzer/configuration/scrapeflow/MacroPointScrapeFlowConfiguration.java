package com.kg.macroanalyzer.configuration.scrapeflow;

import com.kg.macroanalyzer.adaptors.database.postgres.models.ScrapeQueueItem;
import com.kg.macroanalyzer.application.domain.macroseries.MacroPoint;
import com.kg.macroanalyzer.application.ports.driven.ConfigWithMacroPoints;
import com.kg.macroanalyzer.application.ports.driven.DatabasePort;
import com.kg.macroanalyzer.application.ports.driven.WebPort;
import com.kg.macroanalyzer.application.ports.driving.out.seriesconfig.SeriesConfig;
import com.kg.macroanalyzer.application.services.scrape.scrapeflow.ScrapeFlow;
import com.kg.macroanalyzer.application.services.scrape.scrapeflow.existingfinder.ExistingFinder;
import com.kg.macroanalyzer.application.services.scrape.scrapeflow.existingfinder.ExistingMacroPointFinder;
import com.kg.macroanalyzer.application.services.scrape.scrapeflow.persister.MacroPointPersister;
import com.kg.macroanalyzer.application.services.scrape.scrapeflow.persister.Persister;
import com.kg.macroanalyzer.application.services.scrape.scrapeflow.preparer.Preparer;
import com.kg.macroanalyzer.application.services.scrape.scrapeflow.scraper.MacroPointScraper;
import com.kg.macroanalyzer.application.services.scrape.scrapeflow.scraper.Scraper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.stream.Stream;

@Configuration
public class MacroPointScrapeFlowConfiguration {

    @Bean("existingMacroPointFinder")
    public ExistingFinder<SeriesConfig, ConfigWithMacroPoints> finder(DatabasePort databasePort) {
        return new ExistingMacroPointFinder(databasePort);
    }

    @Bean("macroPointScraper")
    public Scraper<ConfigWithMacroPoints> scraper(WebPort<SeriesConfig, Stream<MacroPoint>> webAdaptorFlowPort) {
        return new MacroPointScraper(webAdaptorFlowPort);
    }

    @Bean("macroPointPersister")
    public Persister<ConfigWithMacroPoints> persister(DatabasePort databasePort) {
        return new MacroPointPersister(databasePort);
    }

    @Bean("macroPointScrapeFlow")
    public ScrapeFlow<ScrapeQueueItem, SeriesConfig, ConfigWithMacroPoints> scrapeFlow(
            Preparer<ScrapeQueueItem, SeriesConfig> preparer,
            @Qualifier("existingMacroPointFinder") ExistingFinder<SeriesConfig, ConfigWithMacroPoints> finder,
            @Qualifier("macroPointScraper") Scraper<ConfigWithMacroPoints> scraper,
            @Qualifier("macroPointPersister") Persister<ConfigWithMacroPoints> persister
    ) {
        return new ScrapeFlow<>(
                preparer,
                finder,
                scraper,
                persister
        );
    }

}
