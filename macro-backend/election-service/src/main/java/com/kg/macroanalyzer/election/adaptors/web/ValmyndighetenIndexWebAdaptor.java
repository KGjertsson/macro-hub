package com.kg.macroanalyzer.election.adaptors.web;

import com.kg.macroanalyzer.election.application.port.out.LocalElectionFile;
import com.kg.macroanalyzer.election.application.port.out.LocalElectionIndexRequest;
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
