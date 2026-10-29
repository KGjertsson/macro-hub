package com.kg.macroanalyzer.configuration.scrapeflow;

import com.kg.macroanalyzer.application.domain.macroseries.MacroPoint;
import com.kg.macroanalyzer.application.ports.driven.ConfigWithMacroPoints;
import com.kg.macroanalyzer.application.ports.driven.DatabasePort;
import com.kg.macroanalyzer.application.services.scrape.scrapeflow.existingfinder.ExistingMacroPointFinder;
import com.kg.macroanalyzer.application.services.scrape.scrapeflow.persister.MacroPointPersister;
import com.kg.macroanalyzer.application.services.scrape.scrapeflow.scraper.MacroPointScraper;
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
    public Persister<ConfigWithMacroPoints> persister(DatabasePort databasePort, ScrapeQueuePort scrapeQueuePort) {
        return new MacroPointPersister(databasePort, scrapeQueuePort);
    }

    @Bean("macroPointScrapeFlow")
    public KindedScrapeFlow scrapeFlow(
            Preparer<ScrapeQueueItem, SeriesConfig> preparer,
            @Qualifier("existingMacroPointFinder") ExistingFinder<SeriesConfig, ConfigWithMacroPoints> finder,
            @Qualifier("macroPointScraper") Scraper<ConfigWithMacroPoints> scraper,
            @Qualifier("macroPointPersister") Persister<ConfigWithMacroPoints> persister
    ) {
        return KindedScrapeFlow.of(ScrapeQueueKind.MACRO_POINT, new ScrapeFlow<>(
                preparer,
                finder,
                scraper,
                persister
        ));
    }

}
