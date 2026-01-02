package com.kg.macroanalyzer.adaptors.webadaptor.webadaptorflow.connectionbuilder;

import com.kg.macroanalyzer.application.exceptions.ScrapeException;
import com.kg.macroanalyzer.application.ports.driving.out.seriesconfig.SeriesConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RiksbankenConnectionBuilderTest {

    @Test
    void shouldBuildConnectionWithSubscriptionKey_whenKeyIsProvided() throws ScrapeException {
        // given
        final var subscriptionKey = "test-key";
        final var seriesConfig = SeriesConfig.builder()
                .scrapeUrl("https://example.com/data")
                .build();
        final var builder = new RiksbankenConnectionBuilder(subscriptionKey);

        // when
        final var connection = builder.buildConnection(seriesConfig);

        // then
        assertEquals("GET", connection.getRequestMethod());
        assertEquals(subscriptionKey, connection.getRequestProperty("Ocp-Apim-Subscription-Key"));
    }

    @Test
    void shouldBuildConnectionWithoutSubscriptionKey_whenKeyIsNull() throws ScrapeException {
        // given
        final var seriesConfig = SeriesConfig.builder()
                .scrapeUrl("https://example.com/data")
                .build();
        final var builder = new RiksbankenConnectionBuilder(null);

        // when
        final var connection = builder.buildConnection(seriesConfig);

        // then
        assertNull(connection.getRequestProperty("Ocp-Apim-Subscription-Key"));
    }

    @Test
    void shouldThrowScrapeException_whenUrlIsInvalid() {
        // given
        final var seriesConfig = SeriesConfig.builder()
                .scrapeUrl("invalid-url")
                .build();
        final var builder = new RiksbankenConnectionBuilder("key");

        // when && then
        final var exception = assertThrows(ScrapeException.class, () -> builder.buildConnection(seriesConfig));
        assertTrue(exception.getMessage().contains("Failed to build Riksbanken connection"));
    }
}
