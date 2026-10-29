package com.kg.macroanalyzer.election.adaptors.web;

import com.kg.macroanalyzer.election.application.port.out.LocalElectionFile;
import com.kg.macroanalyzer.election.domain.LocalElectionResult;
import com.kg.macroanalyzer.shared.web.ScrapeException;
import com.kg.macroanalyzer.shared.web.WebAdaptorFlow;
import com.kg.macroanalyzer.shared.web.WebPort;
import com.kg.macroanalyzer.shared.web.ZipEntryGetter;

import java.util.List;

// Each zip also holds a much larger per-district vote file, which is skipped
public class ValmyndighetenWebAdaptor implements WebPort<LocalElectionFile, List<LocalElectionResult>> {

    private final WebAdaptorFlow<String, List<LocalElectionResult>> flow = new WebAdaptorFlow<>(
            new ValmyndighetenConnectionBuilder(),
            new ZipEntryGetter("mandatfordelning"),
            new ValmyndighetenResultResponseParser()
    );

    @Override
    public List<LocalElectionResult> fetch(LocalElectionFile file) throws ScrapeException {
        return ValmyndighetenRetry.fetch(flow, file.url()).stream()
                .map(result -> result.toBuilder()
                        .sourceMd5(file.md5())
                        .build())
                .toList();
    }

}
