package com.kg.macroanalyzer.parliament.adaptors.web;

import com.kg.macroanalyzer.scrape.domain.SeriesConfig;
import com.kg.macroanalyzer.shared.web.ScrapeException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RiksdagenConnectionBuilderTest {

    @Test
    void shouldBuildConnectionWithDetailedHeaders() throws ScrapeException {
        // given
        final var seriesConfig = SeriesConfig.builder()
                .scrapeUrl("https://example.com/data")
                .build();
        final var builder = new RiksdagenConnectionBuilder();

        // when
        final var connection = builder.buildConnection(seriesConfig);

        // then
        assertEquals("GET", connection.getRequestMethod());
        assertEquals(
                "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,"
                        + "image/apng,"
                        + "*/*;q=0.8",
                connection.getRequestProperty("Accept")
        );
        assertEquals("en-US,en;q=0.7", connection.getRequestProperty("Accept-Language"));
        assertEquals(
                "https://data.riksdagen.se/personlista/", connection.getRequestProperty("Referer"));
        assertTrue(connection.getRequestProperty("User-Agent").contains("Mozilla/5.0"));
    }

    @Test
    void shouldThrowScrapeException_whenUrlIsInvalid() {
        // given
        final var seriesConfig = SeriesConfig.builder()
                .scrapeUrl("invalid url")
                .build();
        final var builder = new RiksdagenConnectionBuilder();

        // when && then
        final var exception =
                assertThrows(ScrapeException.class, () -> builder.buildConnection(seriesConfig));
        assertTrue(exception.getMessage().contains("Failed to build Riksdagen connection"));
    }
}
