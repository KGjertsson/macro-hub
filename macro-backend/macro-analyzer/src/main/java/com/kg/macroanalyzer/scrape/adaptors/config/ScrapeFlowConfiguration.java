package com.kg.macroanalyzer.scrape.adaptors.config;

import com.kg.macroanalyzer.scrape.application.service.scrapeflow.Preparer;
import com.kg.macroanalyzer.scrape.application.service.scrapeflow.SeriesConfigPreparer;
import com.kg.macroanalyzer.scrape.domain.ScrapeQueueItem;
import com.kg.macroanalyzer.scrape.domain.SeriesConfig;
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
