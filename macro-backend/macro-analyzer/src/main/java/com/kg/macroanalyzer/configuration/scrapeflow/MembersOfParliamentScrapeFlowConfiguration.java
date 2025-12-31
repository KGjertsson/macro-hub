package com.kg.macroanalyzer.configuration.scrapeflow;

import com.kg.macroanalyzer.adaptors.database.postgres.models.ScrapeQueueItem;
import com.kg.macroanalyzer.application.ports.driven.ConfigWithMacroPoints;
import com.kg.macroanalyzer.application.ports.driving.out.seriesconfig.SeriesConfig;
import com.kg.macroanalyzer.application.services.scrape.scrapeflow.ScrapeFlow;
import com.kg.macroanalyzer.application.services.scrape.scrapeflow.existingfinder.ExistingFinder;
import com.kg.macroanalyzer.application.services.scrape.scrapeflow.persister.Persister;
import com.kg.macroanalyzer.application.services.scrape.scrapeflow.preparer.Preparer;
import com.kg.macroanalyzer.application.services.scrape.scrapeflow.scraper.Scraper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MembersOfParliamentScrapeFlowConfiguration {

//    @Bean
//    public ScrapeFlow<ScrapeQueueItem, SeriesConfig, ConfigWithMacroPoints> scrapeFlow(
//            Preparer<ScrapeQueueItem, SeriesConfig> preparer,
//            ExistingFinder<SeriesConfig, ConfigWithMacroPoints> finder,
//            Scraper<ConfigWithMacroPoints> scraper,
//            Persister<ConfigWithMacroPoints> persister
//    ) {
//        return new ScrapeFlow<>(
//                preparer,
//                finder,
//                scraper,
//                persister
//        );
//    }

}
