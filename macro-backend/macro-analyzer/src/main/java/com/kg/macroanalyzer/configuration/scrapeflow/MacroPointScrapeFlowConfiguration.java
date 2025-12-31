package com.kg.macroanalyzer.configuration.scrapeflow;

import com.kg.macroanalyzer.adaptors.database.postgres.models.ScrapeQueueItem;
import com.kg.macroanalyzer.adaptors.webadaptorflow.WebAdaptorFlowFactory;
import com.kg.macroanalyzer.application.ports.driven.ConfigWithMacroPoints;
import com.kg.macroanalyzer.application.ports.driven.DatabasePort;
import com.kg.macroanalyzer.application.ports.driving.out.seriesconfig.SeriesConfig;
import com.kg.macroanalyzer.application.services.scrape.scrapeflow.ScrapeFlow;
import com.kg.macroanalyzer.application.services.scrape.scrapeflow.existingfinder.ExistingFinder;
import com.kg.macroanalyzer.application.services.scrape.scrapeflow.existingfinder.ExistingMacroPointFinder;
import com.kg.macroanalyzer.application.services.scrape.scrapeflow.persister.MacroPointPersister;
import com.kg.macroanalyzer.application.services.scrape.scrapeflow.persister.Persister;
import com.kg.macroanalyzer.application.services.scrape.scrapeflow.preparer.Preparer;
import com.kg.macroanalyzer.application.services.scrape.scrapeflow.preparer.SeriesConfigPreparer;
import com.kg.macroanalyzer.application.services.scrape.scrapeflow.scraper.MacroPointScraper;
import com.kg.macroanalyzer.application.services.scrape.scrapeflow.scraper.Scraper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class MacroPointScrapeFlowConfiguration {

    @Bean
    public Preparer<ScrapeQueueItem, SeriesConfig> preparer(List<SeriesConfig> seriesConfigList) {
        return new SeriesConfigPreparer(seriesConfigList);
    }

    @Bean
    public ExistingFinder<SeriesConfig, ConfigWithMacroPoints> finder(DatabasePort databasePort) {
        return new ExistingMacroPointFinder(databasePort);
    }

    @Bean
    public Scraper<ConfigWithMacroPoints> scraper(WebAdaptorFlowFactory webAdaptorFlowFactory) {
        return new MacroPointScraper(webAdaptorFlowFactory);
    }

    @Bean
    public Persister<ConfigWithMacroPoints> persister(DatabasePort databasePort) {
        return new MacroPointPersister(databasePort);
    }

    @Bean
    public ScrapeFlow<ScrapeQueueItem, SeriesConfig, ConfigWithMacroPoints> scrapeFlow(
            Preparer<ScrapeQueueItem, SeriesConfig> preparer,
            ExistingFinder<SeriesConfig, ConfigWithMacroPoints> finder,
            Scraper<ConfigWithMacroPoints> scraper,
            Persister<ConfigWithMacroPoints> persister
    ) {
        return new ScrapeFlow<>(
                preparer,
                finder,
                scraper,
                persister
        );
    }

}
