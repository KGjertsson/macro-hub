package com.kg.macroanalyzer.adaptors.webadaptor;

import com.kg.macroanalyzer.adaptors.webadaptor.webadaptorflow.MacroPointAdaptorFlowFactory;
import com.kg.macroanalyzer.application.domain.macroseries.MacroPoint;
import com.kg.macroanalyzer.application.exceptions.ScrapeException;
import com.kg.macroanalyzer.application.ports.driven.WebPort;
import com.kg.macroanalyzer.application.ports.driving.out.seriesconfig.SeriesConfig;
import org.springframework.stereotype.Component;

import java.util.stream.Stream;

@Component
public class MacroPointWebAdaptor implements WebPort<SeriesConfig, Stream<MacroPoint>> {

    private final MacroPointAdaptorFlowFactory webAdaptorFlowFactory;

    public MacroPointWebAdaptor(MacroPointAdaptorFlowFactory webAdaptorFlowFactory) {
        this.webAdaptorFlowFactory = webAdaptorFlowFactory;
    }

    @Override
    public Stream<MacroPoint> fetch(SeriesConfig seriesConfig) throws ScrapeException {
        final var webAdaptorFlow = webAdaptorFlowFactory.build(seriesConfig);

        return webAdaptorFlow.fetch(seriesConfig);
    }

}
