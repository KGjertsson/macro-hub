package com.kg.macroanalyzer.adaptors.webadaptor.webadaptorflow.connectionbuilder;

import com.kg.macroanalyzer.application.exceptions.ScrapeException;
import com.kg.macroanalyzer.application.ports.driving.out.seriesconfig.SeriesConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EuroStatConnectionBuilderTest {

    @Test
    void shouldBuildConnectionWithTsvHeaders_whenFormatIsTsv() throws ScrapeException {
        // given
        final var seriesConfig = SeriesConfig.builder()
                .scrapeUrl("https://example.com/data?format=tsv")
                .build();
        final var builder = new EuroStatConnectionBuilder();

        // when
        final var connection = builder.buildConnection(seriesConfig);

        // then
        assertEquals("GET", connection.getRequestMethod());
        assertEquals(15_000, connection.getConnectTimeout());
        assertEquals(60_000, connection.getReadTimeout());
        assertEquals("macro-hub/1.0 (contact: macro-hub)", connection.getRequestProperty("User-Agent"));
        assertEquals("text/plain, text/tab-separated-values;q=0.9, */*;q=0.8", connection.getRequestProperty("Accept"));
        assertEquals("gzip", connection.getRequestProperty("Accept-Encoding"));
        assertEquals("en", connection.getRequestProperty("Accept-Language"));
    }

    @Test
    void shouldBuildConnectionWithJsonHeaders_whenFormatIsJson() throws ScrapeException {
        // given
        final var seriesConfig = SeriesConfig.builder()
                .scrapeUrl("https://example.com/data?format=json")
                .build();
        final var builder = new EuroStatConnectionBuilder();

        // when
        final var connection = builder.buildConnection(seriesConfig);

        // then
        assertEquals("application/json", connection.getRequestProperty("Accept"));
    }

    @Test
    void shouldBuildConnectionWithDefaultHeaders_whenFormatIsUnknown() throws ScrapeException {
        // given
        final var seriesConfig = SeriesConfig.builder()
                .scrapeUrl("https://example.com/data")
                .build();
        final var builder = new EuroStatConnectionBuilder();

        // when
        final var connection = builder.buildConnection(seriesConfig);

        // then
        assertEquals("*/*", connection.getRequestProperty("Accept"));
    }

    @Test
    void shouldThrowScrapeException_whenUrlIsInvalid() {
        // given
        final var seriesConfig = SeriesConfig.builder()
                .scrapeUrl("invalid-url")
                .build();
        final var builder = new EuroStatConnectionBuilder();

        // when && then
        final var exception = assertThrows(ScrapeException.class, () -> builder.buildConnection(seriesConfig));
        assertTrue(exception.getMessage().contains("Failed to build EuroStat connection"));
    }

}
