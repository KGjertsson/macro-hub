package com.kg.macroanalyzer.scrape.adaptors.postgres;

import com.kg.macroanalyzer.scrape.application.port.out.SeriesConfigPort;
import com.kg.macroanalyzer.scrape.domain.SeriesConfig;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SeriesConfigPostgresAdaptor implements SeriesConfigPort {

    private final SeriesConfigRepository seriesConfigRepository;

    public SeriesConfigPostgresAdaptor(SeriesConfigRepository seriesConfigRepository) {
        this.seriesConfigRepository = seriesConfigRepository;
    }

    @Override
    public List<SeriesConfig> readSeriesConfigList() {
        return seriesConfigRepository.readSeriesConfigList();
    }

}
