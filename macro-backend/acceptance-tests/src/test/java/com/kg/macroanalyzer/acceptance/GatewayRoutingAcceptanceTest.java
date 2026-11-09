package com.kg.macroanalyzer.acceptance;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GatewayRoutingAcceptanceTest {

    private static final String FRONTEND_PAGE_MARKER = "<div id=\"root\">";
    private static final String YEAR_WITHOUT_ELECTIONS = "1900";
    private static final String TELEMETRY_PATH = "/telemetry/collect";

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private final Gateway gateway = AcceptanceStack.running().gateway();

    @Test
    void chartSeriesAreAnsweredByMacroSeries() {
        assertTrue(listedValues("/macro-analyzer/series-config", "name").contains("PolicyRateSweden"));
    }

    @Test
    void partiesAreAnsweredByParliament() {
        assertTrue(listedValues("/macro-analyzer/parliament/parties", "code").contains("S"));
    }

    @Test
    void indicatorsAreAnsweredByRegionalStatistics() {
        assertTrue(listedValues("/macro-analyzer/regional-statistics/indicators", "id").contains("households"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"municipalities", "regions", "constituencies"})
    void electionResultsAreAnsweredByElection(String areaType) {
        // when
        final var response = gateway.get("/macro-analyzer/%s/elections?year=%s".formatted(areaType, YEAR_WITHOUT_ELECTIONS));

        // then
        assertEquals(204, response.statusCode());
    }

    @Test
    void signingKeysAreAnsweredByAuth() {
        // when
        final var response = gateway.get("/macro-analyzer/auth/jwks.json");

        // then
        assertEquals(200, response.statusCode());
        assertEquals(1, JSON.readTree(response.body()).get("keys").size());
    }

    @ParameterizedTest
    @ValueSource(strings = {"regional-statistics", "macro-series", "parliament", "election"})
    void everyServiceAcceptsAScrapeRequestThroughTheGateway(String service) {
        // when
        final var response = gateway.post("/macro-analyzer/scrape/%s/enqueue?name=unknown".formatted(service));

        // then
        assertEquals(202, response.statusCode());
    }

    @ParameterizedTest
    @ValueSource(strings = {"/macro-analyzer", "/macro-analyzer/unknown", "/macro-analyzer/scrape/unknown/enqueue-all"})
    void anUnknownApiPathIsNotFoundRatherThanTheFrontendsPage(String path) {
        // when
        final var get = gateway.get(path);
        final var post = gateway.post(path);

        // then
        assertEquals(404, get.statusCode());
        assertFalse(get.body().contains(FRONTEND_PAGE_MARKER));
        assertEquals(404, post.statusCode());
        assertFalse(post.body().contains(FRONTEND_PAGE_MARKER));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "regional-statistics",
            "parliament",
            "municipalities",
            "regions",
            "constituencies",
            "auth",
            "scrape/regional-statistics",
            "scrape/macro-series",
            "scrape/parliament",
            "scrape/election"
    })
    void aRoutedPrefixWithoutItsSlashIsRedirectedToItsServiceOnTheSameHostAndPort(String prefix) {
        // given
        final var path = "/macro-analyzer/" + prefix;

        // when
        final var redirect = gateway.get(path);
        final var location = redirect.headers().firstValue("Location").orElseThrow();

        // then
        assertEquals(301, redirect.statusCode());
        assertEquals(path + "/", location);

        // when
        final var followed = gateway.get(location);

        // then
        assertEquals(404, followed.statusCode());
        assertEquals(location, JSON.readTree(followed.body()).get("path").asString());
    }

    @ParameterizedTest
    @ValueSource(strings = {"/assets", "/geo"})
    void aDirectoryOfTheFrontendWithoutItsSlashIsRedirectedOnTheSameHostAndPort(String path) {
        // when
        final var response = gateway.get(path);

        // then
        assertEquals(301, response.statusCode());
        assertEquals(path + "/", response.headers().firstValue("Location").orElseThrow());
    }

    @Test
    void browserTelemetryAcceptsOnlyPost() {
        // when
        final var response = gateway.get(TELEMETRY_PATH);

        // then
        assertEquals(405, response.statusCode());
    }

    @Test
    void browserTelemetryAnswersAGatewayErrorWithoutAlloy() {
        // when
        final var response = gateway.postJson(TELEMETRY_PATH, "{}");

        // then
        assertEquals(502, response.statusCode());
        assertFalse(response.body().contains(FRONTEND_PAGE_MARKER));
    }

    @ParameterizedTest
    @ValueSource(strings = {"/parliament", "/konto/logga-in", "/konto/registrera", "/konto/aterstall-losenord"})
    void aPageOfTheDashboardGetsTheFrontend(String page) {
        // when
        final var response = gateway.get(page);

        // then
        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains(FRONTEND_PAGE_MARKER));
    }

    private List<String> listedValues(String path, String field) {
        final var response = gateway.get(path);
        assertEquals(200, response.statusCode());

        return JSON.readTree(response.body()).valueStream()
                .map(entry -> entry.get(field))
                .map(JsonNode::asString)
                .toList();
    }

}
