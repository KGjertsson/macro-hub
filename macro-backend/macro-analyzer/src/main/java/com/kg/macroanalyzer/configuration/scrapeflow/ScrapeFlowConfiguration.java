package com.kg.macroanalyzer.configuration.scrapeflow;

import com.kg.macroanalyzer.adaptors.database.postgres.models.ScrapeQueueItem;
import com.kg.macroanalyzer.application.ports.driving.out.seriesconfig.SeriesConfig;
import com.kg.macroanalyzer.application.services.scrape.scrapeflow.preparer.Preparer;
import com.kg.macroanalyzer.application.services.scrape.scrapeflow.preparer.SeriesConfigPreparer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class ScrapeFlowConfiguration {

    @Bean
    public Preparer<ScrapeQueueItem, SeriesConfig> preparer(List<SeriesConfig> seriesConfigList) {
        return new SeriesConfigPreparer(seriesConfigList);
    }

}
