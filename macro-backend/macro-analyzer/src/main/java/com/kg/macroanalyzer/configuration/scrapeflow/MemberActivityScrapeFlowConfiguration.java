package com.kg.macroanalyzer.configuration.scrapeflow;

import com.kg.macroanalyzer.adaptors.database.postgres.models.ScrapeQueueItem;
import com.kg.macroanalyzer.application.domain.parliament.MemberActivity;
import com.kg.macroanalyzer.application.ports.driven.ConfigWithMemberActivity;
import com.kg.macroanalyzer.application.ports.driven.DatabasePort;
import com.kg.macroanalyzer.application.ports.driven.MemberActivityRequest;
import com.kg.macroanalyzer.application.ports.driven.WebPort;
import com.kg.macroanalyzer.application.ports.driving.out.seriesconfig.SeriesConfig;
import com.kg.macroanalyzer.application.services.scrape.scrapeflow.ScrapeFlow;
import com.kg.macroanalyzer.application.services.scrape.scrapeflow.existingfinder.ExistingFinder;
import com.kg.macroanalyzer.application.services.scrape.scrapeflow.existingfinder.ExistingMemberActivityFinder;
import com.kg.macroanalyzer.application.services.scrape.scrapeflow.persister.MemberActivityPersister;
import com.kg.macroanalyzer.application.services.scrape.scrapeflow.persister.Persister;
import com.kg.macroanalyzer.application.services.scrape.scrapeflow.preparer.Preparer;
import com.kg.macroanalyzer.application.services.scrape.scrapeflow.scraper.MemberActivityScraper;
import com.kg.macroanalyzer.application.services.scrape.scrapeflow.scraper.Scraper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MemberActivityScrapeFlowConfiguration {

    @Bean("existingMemberActivityFinder")
    public ExistingFinder<SeriesConfig, ConfigWithMemberActivity> finder(
            DatabasePort databasePort,
            @Value("${riksdagen.activity.term-count:2}") int termCount
    ) {
        return new ExistingMemberActivityFinder(databasePort, termCount);
    }

    @Bean("memberActivityScraper")
    public Scraper<ConfigWithMemberActivity> scraper(
            WebPort<MemberActivityRequest, MemberActivity> activityWebPort,
            @Value("${riksdagen.activity.delay-ms:200}") long delayMs
    ) {
        return new MemberActivityScraper(activityWebPort, delayMs);
    }

    @Bean("memberActivityPersister")
    public Persister<ConfigWithMemberActivity> persister(DatabasePort databasePort) {
        return new MemberActivityPersister(databasePort);
    }

    @Bean("memberActivityScrapeFlow")
    public ScrapeFlow<ScrapeQueueItem, SeriesConfig, ConfigWithMemberActivity> scrapeFlow(
            Preparer<ScrapeQueueItem, SeriesConfig> preparer,
            @Qualifier("existingMemberActivityFinder") ExistingFinder<SeriesConfig, ConfigWithMemberActivity> finder,
            @Qualifier("memberActivityScraper") Scraper<ConfigWithMemberActivity> scraper,
            @Qualifier("memberActivityPersister") Persister<ConfigWithMemberActivity> persister
    ) {
        return new ScrapeFlow<>(
                preparer,
                finder,
                scraper,
                persister
        );
    }

}
