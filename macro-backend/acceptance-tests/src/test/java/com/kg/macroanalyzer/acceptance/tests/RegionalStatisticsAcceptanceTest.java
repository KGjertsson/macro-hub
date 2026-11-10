package com.kg.macroanalyzer.acceptance.tests;

import com.kg.macroanalyzer.acceptance.support.AcceptanceStack;
import com.kg.macroanalyzer.acceptance.support.BackendService;
import com.kg.macroanalyzer.acceptance.support.Gateway;
import com.kg.macroanalyzer.acceptance.support.Sources;
import org.junit.jupiter.api.AutoClose;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

// A scrape walks all 26 indicators, and a table that answers an error costs 25 s of retries. So every table except
// households' answers metadata without the dimensions its indicators select, which fails them at once, and only households
// is asserted. Its table has no other indicator, so their failures don't drop its stamp. A corrected table starts a stack
// of its own, because the other journeys read the values it replaces
class RegionalStatisticsAcceptanceTest {

    private static final String SERIES = "ScbRegionalStatistics";
    private static final String HOUSEHOLDS_METADATA = "/api/v2/tables/TAB6568/metadata";
    private static final String HOUSEHOLDS_DATA = "/api/v2/tables/TAB6568/data";

    private static final BackendService REGIONAL_STATISTICS_WITHOUT_THROTTLE =
            BackendService.REGIONAL_STATISTICS.withEnvironment(Map.of("SCB_DELAYMS", "0"));

    private static final Map<String, String> DESO_2018 = dataQueryFor("?????????");
    private static final Map<String, String> DESO_2025 = dataQueryFor("*_DeSO2025");
    private static final Map<String, String> REGSO_2020 = dataQueryFor("????????");
    private static final Map<String, String> REGSO_2025 = dataQueryFor("*_RegSO2025");

    private static final JsonMapper JSON = JsonMapper.builder().build();

    @AutoClose
    private static AcceptanceStack stack;

    private static Gateway gateway;
    private static Sources sources;

    @BeforeAll
    static void scrapeTheHouseholds() {
        stack = startStack();
        gateway = stack.gateway();
        sources = stack.sources();

        publishHouseholds(sources);

        scrape(gateway);
    }

    @Test
    void anIndicatorListsItsYearsPerLevelNewestFirstWithoutThePlaceholderYears() {
        // when
        final var indicators = jsonFrom(gateway, "/macro-analyzer/regional-statistics/indicators");

        // then
        final var households = indicators.valueStream()
                .filter(indicator -> "households".equals(indicator.get("id").asString()))
                .findFirst()
                .orElseThrow();
        assertEquals(json("""
                {
                  "id": "households",
                  "label": "Antal hushåll",
                  "description": "Antal hushåll den 31 december.",
                  "category": "Befolkning",
                  "unit": "antal",
                  "decimals": 0,
                  "availability": {
                    "DeSO": [
                      {"year":2024,"geoVersion":"DeSO2025"},
                      {"year":2023,"geoVersion":"DeSO2018"},
                      {"year":2022,"geoVersion":"DeSO2018"}
                    ],
                    "RegSO": [
                      {"year":2024,"geoVersion":"RegSO2025"},
                      {"year":2023,"geoVersion":"RegSO2020"},
                      {"year":2022,"geoVersion":"RegSO2020"},
                      {"year":2021,"geoVersion":"RegSO2020"}
                    ]
                  }
                }"""), households);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "&year=2024"})
    void theLatestYearIsTheDefaultAndItsAreasAreCodedAsTheBoundaryFiles(String year) {
        // when
        final var deso = valuesFrom(gateway, "DeSO", year);
        final var regso = valuesFrom(gateway, "RegSO", year);

        // then
        assertEquals(json("""
                {
                  "indicator": "households", "level": "DeSO", "year": 2024, "geoVersion": "DeSO2025",
                  "values": {"0114A0010": 421.0, "0114C1050": 615.0}
                }"""), deso);
        assertEquals(json("""
                {
                  "indicator": "households", "level": "RegSO", "year": 2024, "geoVersion": "RegSO2025",
                  "values": {"0114R001": 1235.0, "0114R003": 1400.0}
                }"""), regso);
    }

    @Test
    void anEarlierYearIsServedFromTheBoundaryVersionItHasDataIn() {
        // when
        final var deso2023 = valuesFrom(gateway, "DeSO", "&year=2023");
        final var deso2022 = valuesFrom(gateway, "DeSO", "&year=2022");
        final var regso2021 = valuesFrom(gateway, "RegSO", "&year=2021");

        // then
        assertEquals(json("""
                {
                  "indicator": "households", "level": "DeSO", "year": 2023, "geoVersion": "DeSO2018",
                  "values": {"0114A0010": 415.0, "0114A0020": 0.0, "0114C1010": 530.0}
                }"""), deso2023);
        assertEquals(json("""
                {
                  "indicator": "households", "level": "DeSO", "year": 2022, "geoVersion": "DeSO2018",
                  "values": {"0114A0010": 410.0, "0114A0020": 520.0}
                }"""), deso2022);
        assertEquals(json("""
                {
                  "indicator": "households", "level": "RegSO", "year": 2021, "geoVersion": "RegSO2020",
                  "values": {"0114R001": 1200.0, "0114R002": 1300.0}
                }"""), regso2021);
    }

    @ParameterizedTest
    @CsvSource({
            "households,DeSO,2021",
            "households,RegSO,2020",
            "no_such_indicator,DeSO,2024"
    })
    void aYearOrIndicatorWithoutDataHasNothingToShow(String indicator, String level, int year) {
        // when
        final var response = gateway.get(
                "/macro-analyzer/regional-statistics/values?indicator=%s&level=%s&year=%d".formatted(indicator, level, year));

        // then
        assertEquals(204, response.statusCode());
    }

    @Test
    void aLevelThatDoesNotExistIsABadRequest() {
        // when
        final var response = gateway.get("/macro-analyzer/regional-statistics/values?indicator=households&level=Kommun");

        // then
        assertEquals(400, response.statusCode());
    }

    // The other indicators fail again and nothing is written, so this queue item stays pending. It is the last drain on
    // this stack, so nothing retries it
    @Test
    void aScrapeOfAnUnchangedTableFetchesItsMetadataButNoData() {
        // given
        final var metadataFetches = sources.requestCount(HOUSEHOLDS_METADATA);
        final var before = valuesFrom(gateway, "DeSO", "");

        // when
        scrape(gateway);

        // then
        assertEquals(metadataFetches + 1, sources.requestCount(HOUSEHOLDS_METADATA));
        List.of(DESO_2018, DESO_2025, REGSO_2020, REGSO_2025)
                .forEach(query -> assertEquals(1, sources.requestCount(HOUSEHOLDS_DATA, query), query.toString()));
        assertEquals(before, valuesFrom(gateway, "DeSO", ""));
    }

    @Test
    void aTableWithANewUpdatedStampIsFetchedAgainAndTheUserSeesTheNewValues() {
        try (var ownStack = startStack()) {
            // given
            final var ownGateway = ownStack.gateway();
            final var ownSources = ownStack.sources();
            publishHouseholds(ownSources);
            scrape(ownGateway);
            assertEquals(421.0, valuesFrom(ownGateway, "DeSO", "").get("values").get("0114A0010").asDouble());

            // when
            ownSources.serveJson(HOUSEHOLDS_METADATA, Map.of("lang", "sv"), "scb/TAB6568-metadata-updated.json");
            ownSources.serveJson(HOUSEHOLDS_DATA, DESO_2025, "scb/TAB6568-DeSO2025-revised.json");
            scrape(ownGateway);

            // then
            assertEquals(json("""
                    {
                      "indicator": "households", "level": "DeSO", "year": 2024, "geoVersion": "DeSO2025",
                      "values": {"0114A0010": 433.0, "0114C1050": 615.0}
                    }"""), valuesFrom(ownGateway, "DeSO", ""));
            List.of(DESO_2018, DESO_2025, REGSO_2020, REGSO_2025)
                    .forEach(query -> assertEquals(2, ownSources.requestCount(HOUSEHOLDS_DATA, query), query.toString()));
        }
    }

    private static AcceptanceStack startStack() {
        return AcceptanceStack.startWith(List.of(REGIONAL_STATISTICS_WITHOUT_THROTTLE));
    }

    private static void publishHouseholds(Sources sources) {
        sources.serveJsonForOtherPathsMatching("/api/v2/tables/[^/]+/metadata", "scb/another-table-metadata.json");
        sources.serveJson(HOUSEHOLDS_METADATA, Map.of("lang", "sv"), "scb/TAB6568-metadata.json");
        sources.serveJson(HOUSEHOLDS_DATA, DESO_2018, "scb/TAB6568-DeSO2018.json");
        sources.serveJson(HOUSEHOLDS_DATA, DESO_2025, "scb/TAB6568-DeSO2025.json");
        sources.serveJson(HOUSEHOLDS_DATA, REGSO_2020, "scb/TAB6568-RegSO2020.json");
        sources.serveJson(HOUSEHOLDS_DATA, REGSO_2025, "scb/TAB6568-RegSO2025.json");
    }

    // The wildcard is what tells one boundary version's request from another's
    private static Map<String, String> dataQueryFor(String regionWildcard) {
        return Map.of(
                "lang", "sv",
                "outputFormat", "json-stat2",
                "valueCodes[Region]", regionWildcard,
                "valueCodes[Tid]", "2021,2022,2023,2024",
                "valueCodes[ContentsCode]", "000007Y1",
                "valueCodes[Hushallstyp]", "TOTALT");
    }

    private static void scrape(Gateway gateway) {
        assertEquals(202, gateway.post("/macro-analyzer/scrape/regional-statistics/enqueue?name=" + SERIES).statusCode());
        assertEquals(200, gateway.post("/macro-analyzer/scrape/regional-statistics/scrape-from-queue").statusCode());
    }

    private static JsonNode valuesFrom(Gateway gateway, String level, String year) {
        return jsonFrom(gateway, "/macro-analyzer/regional-statistics/values?indicator=households&level=%s%s".formatted(level, year));
    }

    private static JsonNode jsonFrom(Gateway gateway, String path) {
        final var response = gateway.get(path);
        assertEquals(200, response.statusCode());

        return JSON.readTree(response.body());
    }

    private static JsonNode json(String text) {
        return JSON.readTree(text);
    }

}
