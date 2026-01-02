package com.kg.macroanalyzer.adaptors.webadaptor;

import com.kg.macroanalyzer.adaptors.webadaptor.webadaptorflow.MacroPointAdaptorFlowFactory;
import com.kg.macroanalyzer.application.domain.macroseries.MacroPoint;
import com.kg.macroanalyzer.application.exceptions.ScrapeException;
import com.kg.macroanalyzer.application.ports.driven.WebPort;
import com.kg.macroanalyzer.application.ports.driving.out.seriesconfig.SeriesConfig;

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
