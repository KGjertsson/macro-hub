package com.kg.macroanalyzer.macroseries.adaptors.config;

import com.kg.macroanalyzer.macroseries.application.port.out.ConfigWithMacroPoints;
import com.kg.macroanalyzer.macroseries.application.port.out.MacroSeriesRepositoryPort;
import com.kg.macroanalyzer.macroseries.application.service.scrape.ExistingMacroPointFinder;
import com.kg.macroanalyzer.macroseries.application.service.scrape.MacroPointPersister;
import com.kg.macroanalyzer.macroseries.application.service.scrape.MacroPointScraper;
import com.kg.macroanalyzer.macroseries.domain.MacroPoint;
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
    public ExistingFinder<SeriesConfig, ConfigWithMacroPoints> finder(MacroSeriesRepositoryPort repositoryPort) {
        return new ExistingMacroPointFinder(repositoryPort);
    }

    @Bean("macroPointScraper")
    public Scraper<ConfigWithMacroPoints> scraper(WebPort<SeriesConfig, Stream<MacroPoint>> webAdaptorFlowPort) {
        return new MacroPointScraper(webAdaptorFlowPort);
    }

    @Bean("macroPointPersister")
    public Persister<ConfigWithMacroPoints> persister(MacroSeriesRepositoryPort repositoryPort, ScrapeQueuePort scrapeQueuePort) {
        return new MacroPointPersister(repositoryPort, scrapeQueuePort);
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
