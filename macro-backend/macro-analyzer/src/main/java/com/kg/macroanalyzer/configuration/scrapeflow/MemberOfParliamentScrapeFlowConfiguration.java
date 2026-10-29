package com.kg.macroanalyzer.configuration.scrapeflow;

import com.kg.macroanalyzer.application.domain.parliament.MemberOfParliament;
import com.kg.macroanalyzer.application.ports.driven.ConfigWithMemberOfParliament;
import com.kg.macroanalyzer.application.ports.driven.DatabasePort;
import com.kg.macroanalyzer.application.services.scrape.scrapeflow.existingfinder.ExistingMemberOfParliamentFinder;
import com.kg.macroanalyzer.application.services.scrape.scrapeflow.persister.MemberOfParliamentPersister;
import com.kg.macroanalyzer.application.services.scrape.scrapeflow.scraper.MemberOfParliamentScraper;
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
    public Persister<ConfigWithMemberOfParliament> persister(DatabasePort databasePort, ScrapeQueuePort scrapeQueuePort) {
        return new MemberOfParliamentPersister(databasePort, scrapeQueuePort);
    }

    @Bean("memberScrapeFlow")
    public KindedScrapeFlow scrapeFlow(
            Preparer<ScrapeQueueItem, SeriesConfig> preparer,
            @Qualifier("existingMemberFinder") ExistingFinder<SeriesConfig, ConfigWithMemberOfParliament> finder,
            @Qualifier("memberScraper") Scraper<ConfigWithMemberOfParliament> scraper,
            @Qualifier("memberPersister") Persister<ConfigWithMemberOfParliament> persister
    ) {
        return KindedScrapeFlow.of(ScrapeQueueKind.MEMBER_OF_PARLIAMENT, new ScrapeFlow<>(
                preparer,
                finder,
                scraper,
                persister
        ));
    }

}
