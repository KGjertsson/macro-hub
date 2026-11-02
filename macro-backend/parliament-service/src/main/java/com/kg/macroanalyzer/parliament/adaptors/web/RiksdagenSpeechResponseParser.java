package com.kg.macroanalyzer.parliament.adaptors.web;

import com.kg.macroanalyzer.parliament.domain.DocumentPage;
import com.kg.macroanalyzer.parliament.domain.ParliamentDocument;
import com.kg.macroanalyzer.shared.web.ResponseParser;
import com.kg.macroanalyzer.shared.web.ScrapeException;

// anforandelista has no total count, so the whole list is fetched and counted; it is sorted newest first
public record RiksdagenSpeechResponseParser(int recentCount) implements ResponseParser<String, DocumentPage> {

    @Override
    public DocumentPage parse(String response) throws ScrapeException {
        final var root = RiksdagenJson.readTree(response, "anforandelista");
        final var speeches = RiksdagenJson.asList(root.path("anforandelista").path("anforande"));
        final var recent = speeches.stream()
                .limit(recentCount)
                .map(s -> new ParliamentDocument(
                        title(s.path("avsnittsrubrik").asString(""), s.path("underrubrik").asString("")),
                        s.path("dok_datum").asString(""),
                        s.path("protokoll_url_www").asString("")
                ))
                .toList();

        return new DocumentPage(speeches.size(), recent);
    }

    private static String title(String heading, String subHeading) {
        return subHeading.isBlank() ? heading : "%s: %s".formatted(heading, subHeading);
    }

}
