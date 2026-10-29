package com.kg.macroanalyzer.parliament.adaptors.web;

import com.kg.macroanalyzer.parliament.domain.DocumentPage;
import com.kg.macroanalyzer.parliament.domain.ParliamentDocument;
import com.kg.macroanalyzer.shared.web.ResponseParser;
import com.kg.macroanalyzer.shared.web.ScrapeException;

public record RiksdagenMotionResponseParser() implements ResponseParser<String, DocumentPage> {

    @Override
    public DocumentPage parse(String response) throws ScrapeException {
        final var list = RiksdagenJson.readTree(response, "dokumentlista").path("dokumentlista");
        final var recent = RiksdagenJson.asList(list.path("dokument")).stream()
                .map(d -> new ParliamentDocument(
                        d.path("titel").asString(""),
                        d.path("datum").asString(""),
                        RiksdagenJson.absoluteUrl(d.path("dokument_url_html").asString(""))
                ))
                .toList();

        return new DocumentPage(list.path("@traffar").asInt(0), recent);
    }

}
