package com.kg.macroanalyzer.adaptors.webadaptor;

import com.kg.macroanalyzer.adaptors.webadaptor.webadaptorflow.WebAdaptorFlow;
import com.kg.macroanalyzer.adaptors.webadaptor.webadaptorflow.connectionbuilder.RiksdagenConnectionBuilder;
import com.kg.macroanalyzer.adaptors.webadaptor.webadaptorflow.getter.HttpGetter;
import com.kg.macroanalyzer.adaptors.webadaptor.webadaptorflow.responserparser.RiksdagenResponseParser;
import com.kg.macroanalyzer.application.domain.parliament.MemberOfParliament;
import com.kg.macroanalyzer.application.exceptions.ScrapeException;
import com.kg.macroanalyzer.application.ports.driven.WebPort;
import com.kg.macroanalyzer.application.ports.driving.out.seriesconfig.SeriesConfig;

import java.util.stream.Stream;

public class MembersOfParliamentWebAdaptor implements WebPort<SeriesConfig, Stream<MemberOfParliament>> {

    @Override
    public Stream<MemberOfParliament> fetch(SeriesConfig seriesConfig) throws ScrapeException {
        final var connectionBuilder = new RiksdagenConnectionBuilder();
        final var getter = new HttpGetter();
        final var responseParser = new RiksdagenResponseParser();
        final var memberWebAdaptorFlow = new WebAdaptorFlow<>(
                connectionBuilder,
                getter,
                responseParser
        );

        return memberWebAdaptorFlow.fetch(seriesConfig);
    }

}
