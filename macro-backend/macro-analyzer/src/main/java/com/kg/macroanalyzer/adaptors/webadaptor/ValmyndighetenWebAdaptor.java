package com.kg.macroanalyzer.adaptors.webadaptor;

import com.kg.macroanalyzer.adaptors.webadaptor.webadaptorflow.WebAdaptorFlow;
import com.kg.macroanalyzer.adaptors.webadaptor.webadaptorflow.connectionbuilder.ValmyndighetenConnectionBuilder;
import com.kg.macroanalyzer.adaptors.webadaptor.webadaptorflow.getter.ZipEntryGetter;
import com.kg.macroanalyzer.adaptors.webadaptor.webadaptorflow.responserparser.ValmyndighetenResultResponseParser;
import com.kg.macroanalyzer.application.domain.localelection.LocalElectionResult;
import com.kg.macroanalyzer.application.exceptions.ScrapeException;
import com.kg.macroanalyzer.application.ports.driven.LocalElectionFile;
import com.kg.macroanalyzer.application.ports.driven.WebPort;

// Each zip also holds a much larger per-district vote file, which is skipped
public class ValmyndighetenWebAdaptor implements WebPort<LocalElectionFile, LocalElectionResult> {

    private final WebAdaptorFlow<String, LocalElectionResult> flow = new WebAdaptorFlow<>(
            new ValmyndighetenConnectionBuilder(),
            new ZipEntryGetter("mandatfordelning"),
            new ValmyndighetenResultResponseParser()
    );

    @Override
    public LocalElectionResult fetch(LocalElectionFile file) throws ScrapeException {
        return ValmyndighetenRetry.fetch(flow, file.url()).toBuilder()
                .sourceMd5(file.md5())
                .build();
    }

}
