package com.kg.macroanalyzer.adaptors.webadaptor.webadaptorflow.responserparser;

import com.kg.macroanalyzer.application.domain.scb.ScbTableMetadata;
import com.kg.macroanalyzer.shared.web.ResponseParser;
import com.kg.macroanalyzer.shared.web.ScrapeException;

import java.time.Instant;
import java.time.format.DateTimeParseException;

// The metadata of a PxWeb table: its "updated" stamp and every code of every dimension
public record ScbMetadataResponseParser(String table) implements ResponseParser<String, ScbTableMetadata> {

    @Override
    public ScbTableMetadata parse(String response) throws ScrapeException {
        final var root = JsonStat2.readTree(response, table);
        try {
            return new ScbTableMetadata(table, Instant.parse(root.path("updated").asString()), JsonStat2.codes(root));
        } catch (DateTimeParseException e) {
            throw new ScrapeException("No updated stamp in SCB metadata for " + table);
        }
    }

}
