package com.kg.macroanalyzer.adaptors.webadaptor.webadaptorflow.getter;

import com.kg.macroanalyzer.application.exceptions.ScrapeException;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipInputStream;

// Returns the first text entry whose name contains entryMarker; other entries are streamed past without being kept
public record ZipEntryGetter(String entryMarker) implements Getter<HttpURLConnection, String> {

    @Override
    public String get(HttpURLConnection connection) throws ScrapeException {
        try {
            final int status = HttpResponses.connect(connection);
            final var is = HttpResponses.getInputStream(status, connection);
            if (status >= 400) {
                final var response = HttpResponses.readText(is);
                connection.disconnect();

                throw new ScrapeException("HTTP " + status + " from upstream: " + response);
            }

            try (var zip = new ZipInputStream(is)) {
                for (var entry = zip.getNextEntry(); entry != null; entry = zip.getNextEntry()) {
                    if (entry.getName().contains(entryMarker)) {
                        return new String(zip.readAllBytes(), StandardCharsets.UTF_8);
                    }
                }
            } finally {
                connection.disconnect();
            }

            throw new ScrapeException("No zip entry containing '%s' in %s".formatted(entryMarker, connection.getURL()));
        } catch (IOException exception) {
            throw new ScrapeException(exception.getMessage());
        }
    }

}
