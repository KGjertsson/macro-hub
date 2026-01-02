package com.kg.macroanalyzer.configuration.scrapeflow;

import com.kg.macroanalyzer.adaptors.database.postgres.models.ScrapeQueueItem;
import com.kg.macroanalyzer.application.domain.parliament.MemberOfParliament;
import com.kg.macroanalyzer.application.ports.driven.ConfigWithMemberOfParliament;
import com.kg.macroanalyzer.application.ports.driven.WebPort;
import com.kg.macroanalyzer.application.ports.driving.out.seriesconfig.SeriesConfig;
import com.kg.macroanalyzer.application.services.scrape.scrapeflow.ScrapeFlow;
import com.kg.macroanalyzer.application.services.scrape.scrapeflow.existingfinder.ExistingFinder;
import com.kg.macroanalyzer.application.services.scrape.scrapeflow.existingfinder.ExistingMemberOfParliamentFinder;
import com.kg.macroanalyzer.application.services.scrape.scrapeflow.persister.MemberOfParliamentPersister;
import com.kg.macroanalyzer.application.services.scrape.scrapeflow.persister.Persister;
import com.kg.macroanalyzer.application.services.scrape.scrapeflow.preparer.Preparer;
import com.kg.macroanalyzer.application.services.scrape.scrapeflow.scraper.MemberOfParliamentScraper;
import com.kg.macroanalyzer.application.services.scrape.scrapeflow.scraper.Scraper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.stream.Stream;

@Configuration
public class MemberOfParliamentScrapeFlowConfiguration {

    @Bean("existingMemberFinder")
    public ExistingFinder<SeriesConfig, ConfigWithMemberOfParliament> finder() {
        return new ExistingMemberOfParliamentFinder();
    }

    @Bean("memberScraper")
    public Scraper<ConfigWithMemberOfParliament> scraper(
            WebPort<SeriesConfig, Stream<MemberOfParliament>> webAdaptorFlowPort
    ) {
        return new MemberOfParliamentScraper(webAdaptorFlowPort);
    }

    @Bean("memberPersister")
    public Persister<ConfigWithMemberOfParliament> persister() {
        return new MemberOfParliamentPersister();
    }

    @Bean("memberScrapeFlow")
    public ScrapeFlow<ScrapeQueueItem, SeriesConfig, ConfigWithMemberOfParliament> scrapeFlow(
            Preparer<ScrapeQueueItem, SeriesConfig> preparer,
            @Qualifier("existingMemberFinder") ExistingFinder<SeriesConfig, ConfigWithMemberOfParliament> finder,
            @Qualifier("memberScraper") Scraper<ConfigWithMemberOfParliament> scraper,
            @Qualifier("memberPersister") Persister<ConfigWithMemberOfParliament> persister
    ) {
        return new ScrapeFlow<>(
                preparer,
                finder,
                scraper,
                persister
        );
    }

}
