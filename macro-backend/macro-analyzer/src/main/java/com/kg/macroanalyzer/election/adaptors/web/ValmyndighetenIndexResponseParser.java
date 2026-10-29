package com.kg.macroanalyzer.election.adaptors.web;

import com.kg.macroanalyzer.election.application.port.out.LocalElectionFile;
import com.kg.macroanalyzer.election.domain.LocalElectionType;
import com.kg.macroanalyzer.shared.web.ResponseParser;
import com.kg.macroanalyzer.shared.web.ScrapeException;

import java.util.List;
import java.util.regex.Pattern;

// index.md5 lists every result zip as "<md5>  ./<path>"; only final (slutlig) per-area files are kept,
// which leaves out preliminary counts and the nationwide summaries (area code "OS").
// The Riksdag has a single file (area code "00") that holds every valkrets
public record ValmyndighetenIndexResponseParser(
        String baseUrl,
        LocalElectionType type
) implements ResponseParser<String, List<LocalElectionFile>> {

    @Override
    public List<LocalElectionFile> parse(String response) throws ScrapeException {
        if (response == null || response.isBlank()) {
            throw new ScrapeException("Empty index from Valmyndigheten at " + baseUrl);
        }
        final var pattern = Pattern.compile(
                "^([0-9a-f]{32})\\s+\\./(s/%s/Val_\\d{4}_slutlig_(\\d{2}|\\d{4})_%s\\.zip)$"
                        .formatted(type.name().toLowerCase(), type.name())
        );

        return response.lines()
                .map(String::strip)
                .map(pattern::matcher)
                .filter(m -> m.matches())
                .map(m -> new LocalElectionFile(m.group(3), "%s/%s".formatted(baseUrl, m.group(2)), m.group(1)))
                .toList();
    }

}
