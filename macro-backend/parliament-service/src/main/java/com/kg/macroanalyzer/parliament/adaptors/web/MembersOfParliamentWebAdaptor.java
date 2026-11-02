package com.kg.macroanalyzer.parliament.adaptors.web;

import com.kg.macroanalyzer.parliament.domain.MemberOfParliament;
import com.kg.macroanalyzer.scrape.domain.SeriesConfig;
import com.kg.macroanalyzer.shared.web.HttpGetter;
import com.kg.macroanalyzer.shared.web.ScrapeException;
import com.kg.macroanalyzer.shared.web.WebAdaptorFlow;
import com.kg.macroanalyzer.shared.web.WebPort;

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
