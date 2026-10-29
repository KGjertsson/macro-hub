package com.kg.macroanalyzer.adaptors.webadaptor;

import com.kg.macroanalyzer.adaptors.webadaptor.webadaptorflow.connectionbuilder.ValmyndighetenConnectionBuilder;
import com.kg.macroanalyzer.adaptors.webadaptor.webadaptorflow.responserparser.ValmyndighetenIndexResponseParser;
import com.kg.macroanalyzer.application.ports.driven.LocalElectionFile;
import com.kg.macroanalyzer.application.ports.driven.LocalElectionIndexRequest;
import com.kg.macroanalyzer.shared.web.HttpGetter;
import com.kg.macroanalyzer.shared.web.ScrapeException;
import com.kg.macroanalyzer.shared.web.WebAdaptorFlow;
import com.kg.macroanalyzer.shared.web.WebPort;

import java.util.List;

public class ValmyndighetenIndexWebAdaptor implements WebPort<LocalElectionIndexRequest, List<LocalElectionFile>> {

    @Override
    public List<LocalElectionFile> fetch(LocalElectionIndexRequest request) throws ScrapeException {
        final var flow = new WebAdaptorFlow<>(
                new ValmyndighetenConnectionBuilder(),
                new HttpGetter(),
                new ValmyndighetenIndexResponseParser(request.baseUrl(), request.type())
        );

        return ValmyndighetenRetry.fetch(flow, request.baseUrl() + "/index.md5");
    }

}
