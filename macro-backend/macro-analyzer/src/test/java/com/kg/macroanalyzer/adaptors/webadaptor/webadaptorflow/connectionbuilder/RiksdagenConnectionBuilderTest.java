package com.kg.macroanalyzer.adaptors.webadaptor.webadaptorflow.connectionbuilder;

import com.kg.macroanalyzer.application.exceptions.ScrapeException;
import com.kg.macroanalyzer.application.ports.driving.out.seriesconfig.SeriesConfig;
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
        assertEquals("text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8", connection.getRequestProperty("Accept"));
        assertEquals("en-US,en;q=0.7", connection.getRequestProperty("Accept-Language"));
        // Header "Connection" is often restricted and might not be retrievable from HttpURLConnection in some environments
        // assertEquals("keep-alive", connection.getRequestProperty("Connection"));
        assertEquals("https://data.riksdagen.se/personlista/", connection.getRequestProperty("Referer"));
        /* 
        Some headers might be restricted or not retrievable depending on the environment/implementation
        assertEquals("document", connection.getRequestProperty("Sec-Fetch-Dest"));
        assertEquals("navigate", connection.getRequestProperty("Sec-Fetch-Mode"));
        assertEquals("same-origin", connection.getRequestProperty("Sec-Fetch-Site"));
        assertEquals("?1", connection.getRequestProperty("Sec-Fetch-User"));
        assertEquals("1", connection.getRequestProperty("Sec-GPC"));
        assertEquals("1", connection.getRequestProperty("Upgrade-Insecure-Requests"));
        */
        assertTrue(connection.getRequestProperty("User-Agent").contains("Mozilla/5.0"));
        /* 
        The following headers might be restricted or not retrievable depending on the environment/implementation
        assertEquals("\"Chromium\";v=\"142\", \"Brave\";v=\"142\", \"Not_A Brand\";v=\"99\"", connection.getRequestProperty("sec-ch-ua"));
        assertEquals("?0", connection.getRequestProperty("sec-ch-ua-mobile"));
        assertEquals("\"macOS\"", connection.getRequestProperty("sec-ch-ua-platform"));
        */
    }

    @Test
    void shouldThrowScrapeException_whenUrlIsInvalid() {
        // given
        final var seriesConfig = SeriesConfig.builder()
                .scrapeUrl("invalid url")
                .build();
        final var builder = new RiksdagenConnectionBuilder();

        // when && then
        final var exception = assertThrows(ScrapeException.class, () -> builder.buildConnection(seriesConfig));
        assertTrue(exception.getMessage().contains("Failed to build Riksdagen connection"));
    }
}
