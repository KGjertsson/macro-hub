package com.kg.macroanalyzer.parliament.adaptors.config;

import com.kg.macroanalyzer.parliament.application.port.out.ConfigWithMemberOfParliament;
import com.kg.macroanalyzer.parliament.application.port.out.ParliamentRepositoryPort;
import com.kg.macroanalyzer.parliament.application.service.scrape.ExistingMemberOfParliamentFinder;
import com.kg.macroanalyzer.parliament.application.service.scrape.MemberOfParliamentPersister;
import com.kg.macroanalyzer.parliament.application.service.scrape.MemberOfParliamentScraper;
import com.kg.macroanalyzer.parliament.domain.MemberOfParliament;
import com.kg.macroanalyzer.parliament.domain.ParliamentScrapeFlows;
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
    public Persister<ConfigWithMemberOfParliament> persister(ParliamentRepositoryPort repositoryPort, ScrapeQueuePort scrapeQueuePort) {
        return new MemberOfParliamentPersister(repositoryPort, scrapeQueuePort);
    }

    @Bean("memberScrapeFlow")
    public RegisteredScrapeFlow scrapeFlow(
            Preparer<ScrapeQueueItem, SeriesConfig> preparer,
            @Qualifier("existingMemberFinder") ExistingFinder<SeriesConfig, ConfigWithMemberOfParliament> finder,
            @Qualifier("memberScraper") Scraper<ConfigWithMemberOfParliament> scraper,
            @Qualifier("memberPersister") Persister<ConfigWithMemberOfParliament> persister
    ) {
        return RegisteredScrapeFlow.of(ParliamentScrapeFlows.MEMBER_OF_PARLIAMENT, new ScrapeFlow<>(
                preparer,
                finder,
                scraper,
                persister
        ));
    }

}
