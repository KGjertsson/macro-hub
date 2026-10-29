package com.kg.macroanalyzer.macroseries.adaptors.web;

import com.kg.macroanalyzer.macroseries.domain.MacroPoint;
import com.kg.macroanalyzer.scrape.domain.SeriesConfig;
import com.kg.macroanalyzer.shared.web.ScrapeException;
import com.kg.macroanalyzer.shared.web.WebPort;

import java.util.stream.Stream;

public record MacroPointWebAdaptor(
        MacroPointAdaptorFlowFactory webAdaptorFlowFactory
) implements WebPort<SeriesConfig, Stream<MacroPoint>> {

    @Override
    public Stream<MacroPoint> fetch(SeriesConfig seriesConfig) throws ScrapeException {
        final var webAdaptorFlow = webAdaptorFlowFactory.build(seriesConfig);

        return webAdaptorFlow.fetch(seriesConfig);
    }

}
