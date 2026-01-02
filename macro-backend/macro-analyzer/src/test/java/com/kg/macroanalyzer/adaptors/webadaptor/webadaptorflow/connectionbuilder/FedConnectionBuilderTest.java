package com.kg.macroanalyzer.adaptors.webadaptor.webadaptorflow.connectionbuilder;

import com.kg.macroanalyzer.application.exceptions.ScrapeException;
import com.kg.macroanalyzer.application.ports.driving.out.seriesconfig.SeriesConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


class FedConnectionBuilderTest {

    @Test
    void shouldBuildConnectionWithApiKeyAndFileType_whenKeyIsProvided() throws ScrapeException {
        // given
        final var subscriptionKey = "test-key";
        final var baseUrl = "https://example.com/data?series_id=SOME_ID";
        final var seriesConfig = SeriesConfig.builder()
                .scrapeUrl(baseUrl)
                .build();
        final var builder = new FedConnectionBuilder(subscriptionKey);

        // when
        final var connection = builder.buildConnection(seriesConfig);

        // then
        assertEquals("GET", connection.getRequestMethod());
        final var expectedUrl = baseUrl + "&api_key=" + subscriptionKey + "&file_type=json";
        assertEquals(expectedUrl, connection.getURL().toString());
    }

    @Test
    void shouldThrowScrapeException_whenUrlIsInvalid() {
        // given
        final var seriesConfig = SeriesConfig.builder()
                .scrapeUrl("invalid url")
                .build();
        final var builder = new FedConnectionBuilder("key");

        // when && then
        final var exception = assertThrows(ScrapeException.class, () -> builder.buildConnection(seriesConfig));
        assertTrue(exception.getMessage().contains("Failed to build Fed connection"));
    }

}
