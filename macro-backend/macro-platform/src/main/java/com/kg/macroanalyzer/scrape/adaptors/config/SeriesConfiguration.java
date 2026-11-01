package com.kg.macroanalyzer.scrape.adaptors.config;

import com.kg.macroanalyzer.scrape.application.port.out.SeriesConfigPort;
import com.kg.macroanalyzer.scrape.domain.SeriesConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SeriesConfiguration {

    @Bean
    public List<SeriesConfig> getSeriesConfigList(SeriesConfigPort seriesConfigPort) {
        return seriesConfigPort.readSeriesConfigList();
    }

}
