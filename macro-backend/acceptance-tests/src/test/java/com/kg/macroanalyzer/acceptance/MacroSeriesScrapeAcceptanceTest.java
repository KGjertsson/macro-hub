package com.kg.macroanalyzer.acceptance;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.net.http.HttpResponse;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MacroSeriesScrapeAcceptanceTest {

    private static final String SERIES = "PolicyRateSweden";
    private static final String SOURCE_PATH = "/swea/v1/Observations/SECBREPOEFF/1994-06-01";

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private final Gateway gateway = AcceptanceStack.running().gateway();
    private final Sources sources = AcceptanceStack.running().sources();

    @Test
    void aScrapedSeriesCanBeChartedThroughTheGateway() {
        // given
        sources.serveJson(SOURCE_PATH, "riksbanken/policy-rate-sweden.json");
        assertTrue(listedSeriesNames().contains(SERIES));
        assertEquals(204, chartData().statusCode());

        // when
        assertEquals(202, gateway.post("/macro-analyzer/scrape/macro-series/enqueue?name=" + SERIES).statusCode());
        assertEquals(200, gateway.post("/macro-analyzer/scrape/macro-series/scrape-from-queue").statusCode());

        // then
        final var response = chartData();
        assertEquals(200, response.statusCode());
        final var chart = JSON.readTree(response.body());
        assertEquals(
                JSON.readTree("[[2024,5,16],[2024,5,17],[2024,5,18],[2024,5,19],[2024,5,20]]"),
                chart.get("labels"));
        final var series = chart.get("chartData");
        assertEquals(1, series.size());
        assertEquals(SERIES, series.get(0).get("name").asString());
        assertEquals(JSON.readTree("[4.0,3.75,3.75,3.75,3.5]"), series.get(0).get("values"));
        assertEquals(1, sources.requestCount(SOURCE_PATH));
    }

    private List<String> listedSeriesNames() {
        final var response = gateway.get("/macro-analyzer/series-config");
        assertEquals(200, response.statusCode());

        return JSON.readTree(response.body()).valueStream()
                .map(config -> config.get("name"))
                .map(JsonNode::asString)
                .toList();
    }

    private HttpResponse<String> chartData() {
        return gateway.postJson("/macro-analyzer/chart-data", """
                {"strategy":"DAY","timeFrame":"ALL","chartSeriesParams":[{"name":"%s"}]}""".formatted(SERIES));
    }

}
