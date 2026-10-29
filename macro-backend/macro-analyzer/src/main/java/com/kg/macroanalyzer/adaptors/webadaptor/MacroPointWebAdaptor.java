package com.kg.macroanalyzer.adaptors.webadaptor;

import com.kg.macroanalyzer.adaptors.webadaptor.webadaptorflow.MacroPointAdaptorFlowFactory;
import com.kg.macroanalyzer.application.domain.macroseries.MacroPoint;
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
