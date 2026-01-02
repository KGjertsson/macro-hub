package com.kg.macroanalyzer.adaptors.webadaptor;

import com.kg.macroanalyzer.application.domain.parliament.MemberOfParliament;
import com.kg.macroanalyzer.application.exceptions.ScrapeException;
import com.kg.macroanalyzer.application.ports.driven.WebPort;
import com.kg.macroanalyzer.application.ports.driving.out.seriesconfig.SeriesConfig;
import org.springframework.stereotype.Component;

import java.util.stream.Stream;

@Component
public class MembersOfParliamentWebAdaptor implements WebPort<SeriesConfig, Stream<MemberOfParliament>> {

    @Override
    public Stream<MemberOfParliament> fetch(SeriesConfig seriesConfig) throws ScrapeException {
        return null;
    }

}
