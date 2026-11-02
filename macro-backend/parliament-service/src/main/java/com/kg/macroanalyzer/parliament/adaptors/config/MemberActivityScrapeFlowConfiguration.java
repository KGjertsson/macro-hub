package com.kg.macroanalyzer.parliament.adaptors.config;

import com.kg.macroanalyzer.parliament.application.port.out.ConfigWithMemberActivity;
import com.kg.macroanalyzer.parliament.application.port.out.MemberActivityRequest;
import com.kg.macroanalyzer.parliament.application.port.out.ParliamentRepositoryPort;
import com.kg.macroanalyzer.parliament.application.service.scrape.ExistingMemberActivityFinder;
import com.kg.macroanalyzer.parliament.application.service.scrape.MemberActivityPersister;
import com.kg.macroanalyzer.parliament.application.service.scrape.MemberActivityScraper;
import com.kg.macroanalyzer.parliament.domain.MemberActivity;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MemberActivityScrapeFlowConfiguration {

    @Bean("existingMemberActivityFinder")
    public ExistingFinder<SeriesConfig, ConfigWithMemberActivity> finder(
            ParliamentRepositoryPort repositoryPort,
            @Value("${riksdagen.activity.term-count:2}") int termCount
    ) {
        return new ExistingMemberActivityFinder(repositoryPort, termCount);
    }

    @Bean("memberActivityScraper")
    public Scraper<ConfigWithMemberActivity> scraper(
            WebPort<MemberActivityRequest, MemberActivity> activityWebPort,
            @Value("${riksdagen.activity.delay-ms:200}") long delayMs
    ) {
        return new MemberActivityScraper(activityWebPort, delayMs);
    }

    @Bean("memberActivityPersister")
    public Persister<ConfigWithMemberActivity> persister(ParliamentRepositoryPort repositoryPort, ScrapeQueuePort scrapeQueuePort) {
        return new MemberActivityPersister(repositoryPort, scrapeQueuePort);
    }

    @Bean("memberActivityScrapeFlow")
    public RegisteredScrapeFlow scrapeFlow(
            Preparer<ScrapeQueueItem, SeriesConfig> preparer,
            @Qualifier("existingMemberActivityFinder") ExistingFinder<SeriesConfig, ConfigWithMemberActivity> finder,
            @Qualifier("memberActivityScraper") Scraper<ConfigWithMemberActivity> scraper,
            @Qualifier("memberActivityPersister") Persister<ConfigWithMemberActivity> persister
    ) {
        return RegisteredScrapeFlow.of(ParliamentScrapeFlows.MEMBER_ACTIVITY, new ScrapeFlow<>(
                preparer,
                finder,
                scraper,
                persister
        ));
    }

}
