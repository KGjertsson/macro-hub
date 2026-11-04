package com.kg.macroanalyzer.acceptance;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MacroSeriesAcceptanceTest {

    private static final String RIKSBANKEN_OBSERVATIONS = "/swea/v1/Observations/";
    private static final String FED_OBSERVATIONS = "/fred/series/observations";
    private static final String EUROSTAT_GOVERNMENT_DEBT = "/eurostat/api/dissemination/statistics/1.0/data/gov_10dd_edpt1";

    private static final String FIRST_COLOR = "rgb(207,82,48)";
    private static final String SECOND_COLOR = "rgb(245,154,68)";

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private final Gateway gateway = AcceptanceStack.running().gateway();
    private final Sources sources = AcceptanceStack.running().sources();

    @Test
    void aScrapedSeriesCanBeChartedThroughTheGateway() {
        // given
        final var series = "PolicyRateSweden";
        final var sourcePath = RIKSBANKEN_OBSERVATIONS + "SECBREPOEFF/1994-06-01";
        sources.serveJson(sourcePath, "riksbanken/policy-rate-sweden.json");
        assertTrue(listedSeriesNames().contains(series));
        assertEquals(204, chartRequest("DAY", "ALL", series).statusCode());

        // when
        scrape(series);

        // then
        final var chart = chart("DAY", "ALL", series);
        assertEquals(json("[[2024,5,16],[2024,5,17],[2024,5,18],[2024,5,19],[2024,5,20]]"), chart.get("labels"));
        assertEquals(1, chart.get("chartData").size());
        assertSeries(chart, 0, series, "[4.0,3.75,3.75,3.75,3.5]");
        assertEquals(1, sources.requestCount(sourcePath));
    }

    @Test
    void aFedSeriesGapTakesThePreviousDayAndATrailingGapGetsNoLabel() {
        // given
        final var series = "FEDEffectiveFederalFundsRate";
        final var request = Map.of(
                "series_id", "EFFR",
                "api_key", BackendService.FED_SUBSCRIPTION_KEY,
                "file_type", "json");
        sources.serveJson(FED_OBSERVATIONS, request, "fed/effective-federal-funds-rate.json");

        // when
        scrape(series);

        // then
        final var chart = chart("DAY", "ALL", series);
        assertEquals(json("[[2024,12,24],[2024,12,25],[2024,12,26]]"), chart.get("labels"));
        assertSeries(chart, 0, series, "[4.33,4.33,4.34]");
        assertEquals(1, sources.requestCount(FED_OBSERVATIONS, request));
    }

    @Test
    void aEurostatSeriesIsChartedPerYear() {
        // given
        final var series = "DebtPcGdpSe";
        final var request = Map.of(
                "freq", "A",
                "unit", "PC_GDP",
                "sector", "S13",
                "na_item", "GD",
                "geo", "se");
        sources.serveJson(EUROSTAT_GOVERNMENT_DEBT, request, "eurostat/debt-pc-gdp-sweden.json");

        // when
        scrape(series);

        // then
        final var chart = chart("YEAR", "ALL", series);
        assertEquals(json("[2021,2022,2023]"), chart.get("labels"));
        assertSeries(chart, 0, series, "[36.5,33.2,31.5]");
        assertEquals(1, sources.requestCount(EUROSTAT_GOVERNMENT_DEBT, request));
    }

    @Test
    void aValueRevisedAtTheSourceReplacesTheOldOneAndARepeatedScrapeChangesNothing() {
        // given
        final var series = "UsdSekExchangeRate";
        final var sourcePath = RIKSBANKEN_OBSERVATIONS + "SEKUSDPMI/1993-01-04";
        final var labels = json("[[2024,3,4],[2024,3,5],[2024,3,6]]");
        sources.serveJson(sourcePath, "riksbanken/usd-sek.json");

        // when
        scrape(series);
        scrape(series);

        // then
        final var scrapedTwice = chart("DAY", "ALL", series);
        assertEquals(labels, scrapedTwice.get("labels"));
        assertSeries(scrapedTwice, 0, series, "[10.30,10.35,10.40]");

        // given
        sources.serveJson(sourcePath, "riksbanken/usd-sek-revised.json");

        // when
        scrape(series);

        // then
        final var revised = chart("DAY", "ALL", series);
        assertEquals(labels, revised.get("labels"));
        assertSeries(revised, 0, series, "[10.30,10.25,10.40]");
        assertEquals(3, sources.requestCount(sourcePath));
    }

    @Test
    void twoSeriesWithDifferentDatesAreAlignedInOneChartPerDayAndPerMonth() {
        // given
        final var shortRate = "EuroMarketRateUsa3Month";
        final var longRate = "IntGovBondsUsa10Year";
        sources.serveJson(RIKSBANKEN_OBSERVATIONS + "EUDP3MUSD/1979-11-28", "riksbanken/euro-market-rate-usa-3-month.json");
        sources.serveJson(RIKSBANKEN_OBSERVATIONS + "USGVB10Y/1991-01-02", "riksbanken/gov-bond-usa-10-year.json");

        // when
        scrape(shortRate);
        scrape(longRate);

        // then
        final var perDay = chart("DAY", "ALL", longRate, shortRate);
        assertEquals(json("[[2024,1,30],[2024,1,31],[2024,2,1],[2024,2,2],[2024,2,3]]"), perDay.get("labels"));
        assertEquals(2, perDay.get("chartData").size());
        assertSeries(perDay, 0, shortRate, "[5.5,5.5,5.4,5.3,null]");
        assertSeries(perDay, 1, longRate, "[null,4.1,4.1,4.1,4.2]");
        assertEquals(FIRST_COLOR, perDay.get("chartData").get(0).get("color").asString());
        assertEquals(SECOND_COLOR, perDay.get("chartData").get(1).get("color").asString());

        final var perMonth = chart("MONTH", "ALL", shortRate, longRate);
        assertEquals(json("[[2024,1],[2024,2]]"), perMonth.get("labels"));
        assertSeries(perMonth, 0, shortRate, "[5.5,5.4]");
        assertSeries(perMonth, 1, longRate, "[4.1,4.1]");
    }

    @Test
    void aOneYearChartLeavesOutWhatIsOlderThanAYear() {
        // given
        final var series = "GovernmentBondSweden2Year";
        final var today = LocalDate.now();
        final var longAgo = today.minusYears(2);
        final var recently = today.minusDays(30);
        final var dayAfter = recently.plusDays(1);
        sources.serveJsonBody(RIKSBANKEN_OBSERVATIONS + "SEGVB2YC/1987-01-07", """
                [
                  {"date": "%s", "value": 1.0},
                  {"date": "%s", "value": 2.0},
                  {"date": "%s", "value": 3.0}
                ]""".formatted(longAgo, recently, dayAfter));

        // when
        scrape(series);

        // then
        final var chart = chart("DAY", "ONEYEAR", series);
        assertEquals(json("[%s,%s]".formatted(dayLabel(recently), dayLabel(dayAfter))), chart.get("labels"));
        assertSeries(chart, 0, series, "[2.0,3.0]");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            """
            {"timeFrame":"ALL","chartSeriesParams":[{"name":"PolicyRateSweden"}]}""",
            """
            {"strategy":"DAY","chartSeriesParams":[{"name":"PolicyRateSweden"}]}""",
            """
            {"strategy":"DAY","timeFrame":"ALL"}""",
            """
            {"strategy":"WEEK","timeFrame":"ALL","chartSeriesParams":[{"name":"PolicyRateSweden"}]}""",
            """
            {"strategy":"DAY","timeFrame":"TWOYEAR","chartSeriesParams":[{"name":"PolicyRateSweden"}]}"""
    })
    void aChartRequestMissingOrMisspellingAChoiceIsRejected(String body) {
        // when
        final var response = gateway.postJson("/macro-analyzer/chart-data", body);

        // then
        assertEquals(400, response.statusCode());
    }

    private void scrape(String series) {
        assertEquals(202, gateway.post("/macro-analyzer/scrape/macro-series/enqueue?name=" + series).statusCode());
        assertEquals(200, gateway.post("/macro-analyzer/scrape/macro-series/scrape-from-queue").statusCode());
    }

    private JsonNode chart(String strategy, String timeFrame, String... series) {
        final var response = chartRequest(strategy, timeFrame, series);
        assertEquals(200, response.statusCode());

        return JSON.readTree(response.body());
    }

    private HttpResponse<String> chartRequest(String strategy, String timeFrame, String... series) {
        final var params = Arrays.stream(series)
                .map("{\"name\":\"%s\"}"::formatted)
                .collect(Collectors.joining(","));

        return gateway.postJson("/macro-analyzer/chart-data", """
                {"strategy":"%s","timeFrame":"%s","chartSeriesParams":[%s]}""".formatted(strategy, timeFrame, params));
    }

    private void assertSeries(JsonNode chart, int position, String name, String values) {
        final var series = chart.get("chartData").get(position);
        assertEquals(name, series.get("name").asString());
        assertEquals(json(values), series.get("values"));
    }

    private String dayLabel(LocalDate date) {
        return "[%d,%d,%d]".formatted(date.getYear(), date.getMonthValue(), date.getDayOfMonth());
    }

    private JsonNode json(String text) {
        return JSON.readTree(text);
    }

    private List<String> listedSeriesNames() {
        final var response = gateway.get("/macro-analyzer/series-config");
        assertEquals(200, response.statusCode());

        return JSON.readTree(response.body()).valueStream()
                .map(config -> config.get("name"))
                .map(JsonNode::asString)
                .toList();
    }

}
