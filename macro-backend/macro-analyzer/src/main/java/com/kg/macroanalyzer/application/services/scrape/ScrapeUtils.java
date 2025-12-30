package com.kg.macroanalyzer.application.services.scrape;

import com.kg.macroanalyzer.application.domain.macroseries.MacroPoint;
import com.kg.macroanalyzer.application.exceptions.ScrapeException;
import com.kg.macroanalyzer.application.ports.driving.out.seriesconfig.SeriesConfig;
import com.kg.macroanalyzer.adaptors.web.macroseries.WebAdaptorFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

import static java.util.Objects.isNull;

@Slf4j
@Component
public class ScrapeUtils {

    private final WebAdaptorFactory webAdaptorFactory;

    @Autowired
    public ScrapeUtils(WebAdaptorFactory webAdaptorFactory) {
        this.webAdaptorFactory = webAdaptorFactory;
    }

    public List<MacroPoint> scrapeNovelItems(
            SeriesConfig seriesConfig,
            List<MacroPoint> persistedItems
    ) throws ScrapeException {
        if (isNull(seriesConfig)) {
            throw new ScrapeException("Found null seriesConfig when scraping novel items");
        }
        final var webAdaptor = webAdaptorFactory.build(seriesConfig);

        return webAdaptor.fetchSeriesData(seriesConfig)
                .filter(i -> !persistedItems.contains(i))
                .toList();
    }

}
