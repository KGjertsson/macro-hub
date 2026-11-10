package com.kg.macroanalyzer.acceptance.tests;

import com.kg.macroanalyzer.acceptance.support.AcceptanceStack;
import com.kg.macroanalyzer.acceptance.support.BackendService;
import com.kg.macroanalyzer.acceptance.support.Eventually;
import com.kg.macroanalyzer.acceptance.support.Gateway;
import com.kg.macroanalyzer.acceptance.support.Sources;
import org.junit.jupiter.api.AutoClose;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FailureAndRecoveryAcceptanceTest {

    private static final String POLICY_RATE = "PolicyRateSweden";
    private static final String POLICY_RATE_SOURCE = "/swea/v1/Observations/SECBREPOEFF/1994-06-01";
    private static final String POLICY_RATE_FIXTURE = "riksbanken/policy-rate-sweden.json";
    private static final String POLICY_RATE_LABELS = "[[2024,5,16],[2024,5,17],[2024,5,18],[2024,5,19],[2024,5,20]]";
    private static final String POLICY_RATE_VALUES = "[4.0,3.75,3.75,3.75,3.5]";

    private static final String USD_SEK = "UsdSekExchangeRate";
    private static final String USD_SEK_SOURCE = "/swea/v1/Observations/SEKUSDPMI/1993-01-04";
    private static final String USD_SEK_FIXTURE = "riksbanken/usd-sek.json";
    private static final String USD_SEK_LABELS = "[[2024,3,4],[2024,3,5],[2024,3,6]]";
    private static final String USD_SEK_VALUES = "[10.30,10.35,10.40]";

    private static final String SHORT_RATE = "EuroMarketRateUsa3Month";
    private static final String SHORT_RATE_SOURCE = "/swea/v1/Observations/EUDP3MUSD/1979-11-28";
    private static final String SHORT_RATE_FIXTURE = "riksbanken/euro-market-rate-usa-3-month.json";
    private static final String SHORT_RATE_LABELS = "[[2024,1,30],[2024,1,31],[2024,2,1],[2024,2,2]]";
    private static final String SHORT_RATE_VALUES = "[5.5,5.5,5.4,5.3]";

    private static final String LONG_RATE = "IntGovBondsUsa10Year";
    private static final String LONG_RATE_SOURCE = "/swea/v1/Observations/USGVB10Y/1991-01-02";
    private static final String LONG_RATE_FIXTURE = "riksbanken/gov-bond-usa-10-year.json";
    private static final String LONG_RATE_LABELS = "[[2024,1,31],[2024,2,1],[2024,2,2],[2024,2,3]]";
    private static final String LONG_RATE_VALUES = "[4.1,4.1,4.1,4.2]";

    private static final String DRAIN = "/macro-analyzer/scrape/macro-series/scrape-from-queue";
    private static final Duration HOLD_THE_SCRAPE_OPEN = Duration.ofSeconds(10);
    private static final Duration BACK_AFTER_POSTGRES = Duration.ofSeconds(60);

    private static final JsonMapper JSON = JsonMapper.builder().build();

    @AutoClose
    private static AcceptanceStack stack;

    private static Gateway gateway;
    private static Sources sources;

    @BeforeAll
    static void startAStackForFailures() {
        stack = AcceptanceStack.startWith(List.of(BackendService.MACRO_SERIES));
        gateway = stack.gateway();
        sources = stack.sources();
    }

    @Test
    void anItemThatFailsStaysPendingAndTheNextDrainFetchesItOnceTheSourceHasRecovered() {
        // given
        sources.failWith(POLICY_RATE_SOURCE, 500);
        enqueue(gateway, POLICY_RATE);

        // when
        // Its answer isn't asserted: a drain answers 200 even when every item failed, so it tells a user nothing
        gateway.post(DRAIN);

        // then
        assertEquals(1, sources.requestCount(POLICY_RATE_SOURCE));
        assertEquals(204, chartRequest(gateway, POLICY_RATE).statusCode());

        // when
        gateway.post(DRAIN);

        // then
        assertEquals(2, sources.requestCount(POLICY_RATE_SOURCE));
        assertEquals(204, chartRequest(gateway, POLICY_RATE).statusCode());

        // given
        sources.serveJson(POLICY_RATE_SOURCE, POLICY_RATE_FIXTURE);

        // when
        assertEquals(200, gateway.post(DRAIN).statusCode());

        // then
        assertEquals(3, sources.requestCount(POLICY_RATE_SOURCE));
        assertChart(gateway, POLICY_RATE, POLICY_RATE_LABELS, POLICY_RATE_VALUES);

        // when
        assertEquals(200, gateway.post(DRAIN).statusCode());

        // then
        assertEquals(3, sources.requestCount(POLICY_RATE_SOURCE));
    }

    @Test
    void aSecondDrainIsRejectedWhileAScrapeIsRunningAndTheFirstStillFinishes() {
        // given
        sources.serveJsonDelayed(USD_SEK_SOURCE, USD_SEK_FIXTURE, HOLD_THE_SCRAPE_OPEN);
        enqueue(gateway, USD_SEK);

        // when
        final var first = CompletableFuture.supplyAsync(() -> gateway.post(DRAIN))
                .orTimeout(2, TimeUnit.MINUTES);
        Eventually.assertWithin(Duration.ofSeconds(30), "The first drain reaches the source", () ->
                assertEquals(1, sources.requestCount(USD_SEK_SOURCE)));
        final var second = gateway.post(DRAIN);

        // then
        assertEquals(409, second.statusCode());
        assertEquals(200, first.join().statusCode());
        assertChart(gateway, USD_SEK, USD_SEK_LABELS, USD_SEK_VALUES);
        assertEquals(1, sources.requestCount(USD_SEK_SOURCE));

        // when
        final var afterwards = gateway.post(DRAIN);

        // then
        assertEquals(200, afterwards.statusCode());
        assertEquals(1, sources.requestCount(USD_SEK_SOURCE));
    }

    // Tests of a class run in turn, so this one stops Postgres under the class's stack. Its own series keep the other
    // tests' request counts and charts out of it
    @Test
    void afterAPostgresRestartTheDataIsStillThereAndAScrapeWorksWithoutRestartingTheService() {
        // given
        sources.serveJson(SHORT_RATE_SOURCE, SHORT_RATE_FIXTURE);
        sources.serveJson(LONG_RATE_SOURCE, LONG_RATE_FIXTURE);
        enqueue(gateway, SHORT_RATE);
        assertEquals(200, gateway.post(DRAIN).statusCode());
        assertChart(gateway, SHORT_RATE, SHORT_RATE_LABELS, SHORT_RATE_VALUES);

        // when
        stack.stopPostgres();

        // then
        // The finally keeps a failure here from leaving Postgres stopped for the class's other tests
        try {
            assertEquals(500, chartRequest(gateway, SHORT_RATE).statusCode());
        } finally {
            // when
            stack.startStoppedPostgres();
        }

        // then
        Eventually.assertWithin(BACK_AFTER_POSTGRES, "The chart answers again, with the data from before", () ->
                assertChart(gateway, SHORT_RATE, SHORT_RATE_LABELS, SHORT_RATE_VALUES));

        // when
        enqueue(gateway, LONG_RATE);
        assertEquals(200, gateway.post(DRAIN).statusCode());

        // then
        assertChart(gateway, LONG_RATE, LONG_RATE_LABELS, LONG_RATE_VALUES);
        assertEquals(1, sources.requestCount(LONG_RATE_SOURCE));
    }

    private void enqueue(Gateway target, String series) {
        assertEquals(202, target.post("/macro-analyzer/scrape/macro-series/enqueue?name=" + series).statusCode());
    }

    private HttpResponse<String> chartRequest(Gateway target, String series) {
        return target.postJson("/macro-analyzer/chart-data", """
                {"strategy":"DAY","timeFrame":"ALL","chartSeriesParams":[{"name":"%s"}]}""".formatted(series));
    }

    private void assertChart(Gateway target, String series, String labels, String values) {
        final var response = chartRequest(target, series);
        assertEquals(200, response.statusCode());
        final JsonNode chart = JSON.readTree(response.body());
        assertEquals(JSON.readTree(labels), chart.get("labels"));
        assertEquals(series, chart.get("chartData").get(0).get("name").asString());
        assertEquals(JSON.readTree(values), chart.get("chartData").get(0).get("values"));
    }

}
