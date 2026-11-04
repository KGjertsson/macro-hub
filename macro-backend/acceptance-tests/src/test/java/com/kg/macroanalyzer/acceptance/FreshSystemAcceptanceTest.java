package com.kg.macroanalyzer.acceptance;

import org.junit.jupiter.api.AutoClose;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FreshSystemAcceptanceTest {

    private static final JsonMapper JSON = JsonMapper.builder().build();

    @AutoClose
    private static AcceptanceStack stack;

    private static Gateway gateway;

    @BeforeAll
    static void startAStackNothingHasScraped() {
        stack = AcceptanceStack.startWith(BackendService.ALL);
        gateway = stack.gateway();
    }

    @Test
    void everyChartSeriesIsListedAndNoneHasDataYet() {
        // when
        final var series = jsonFrom("/macro-analyzer/series-config");
        final var chart = gateway.postJson("/macro-analyzer/chart-data", """
                {"strategy":"DAY","timeFrame":"ALL","chartSeriesParams":[{"name":"PolicyRateSweden"}]}""");

        // then
        assertEquals(108, series.size());
        assertEquals("PolicyRateSweden", series.get(0).get("name").asString());
        assertEquals("Styrränta Sverige", series.get(0).get("displayName").asString());
        assertEquals("DebtPcGdpSk", series.get(107).get("name").asString());
        assertEquals(
                Set.of("Räntor", "Statsobligationer", "Statsskuld", "Valuta", "Nyckeltal USA"),
                series.valueStream().map(config -> config.get("category").asString()).collect(Collectors.toSet()));
        assertEquals(204, chart.statusCode());
    }

    @Test
    void everyIndicatorIsListedAndNoneHasValuesYet() {
        // when
        final var indicators = jsonFrom("/macro-analyzer/regional-statistics/indicators");
        final var values = gateway.get("/macro-analyzer/regional-statistics/values?indicator=households&level=RegSO");

        // then
        assertEquals(26, indicators.size());
        final var households = indicators.valueStream()
                .filter(indicator -> indicator.get("id").asString().equals("households"))
                .findFirst()
                .orElseThrow();
        assertEquals("Antal hushåll", households.get("label").asString());
        assertEquals("antal", households.get("unit").asString());
        assertEquals(JSON.readTree("{\"DeSO\":[],\"RegSO\":[]}"), households.get("availability"));
        assertEquals(204, values.statusCode());
    }

    @Test
    void everyPartyIsListed() {
        // when
        final var parties = jsonFrom("/macro-analyzer/parliament/parties");

        // then
        assertEquals(
                List.of("S", "M", "C", "V", "KD", "L", "MP", "FP", "NYD", "-", "SD"),
                parties.valueStream().map(party -> party.get("code").asString()).toList());
        assertEquals(
                JSON.readTree("""
                        {"code":"S","name":"Socialdemokraterna","color":"#E8112d","ideology":"Social Democracy","active":true}"""),
                parties.get(0));
    }

    @Test
    void riksdagElectionsAreListedNewestFirstWithTheirSeats() {
        // when
        final var elections = jsonFrom("/macro-analyzer/parliament/elections");

        // then
        assertEquals(
                List.of(2026, 2022, 2018, 2014, 2010, 2006, 2002, 1998, 1994, 1991, 1988),
                elections.valueStream().map(election -> election.get("year").asInt()).toList());
        final var latest = elections.get(0);
        assertEquals("2026-09-28", latest.get("termStart").asString());
        assertEquals(
                List.of("S=99", "M=70", "SD=62", "V=30", "C=25", "KD=22", "MP=22", "L=19"),
                latest.get("parties").valueStream()
                        .map(party -> party.get("code").asString() + "=" + party.get("seats").asInt())
                        .toList());
    }

    @Test
    void parliamentHasNoMembersYet() {
        assertEquals(204, gateway.get("/macro-analyzer/parliament/composition").statusCode());
        assertEquals(204, gateway.get("/macro-analyzer/parliament/members").statusCode());
        assertEquals(404, gateway.get("/macro-analyzer/parliament/members/0123456789").statusCode());
    }

    @ParameterizedTest
    @CsvSource({"municipalities,0180", "regions,01", "constituencies,01"})
    void localElectionsHaveNoResultsYet(String areaType, String code) {
        assertEquals(204, gateway.get("/macro-analyzer/%s/elections".formatted(areaType)).statusCode());
        assertEquals(404, gateway.get("/macro-analyzer/%s/%s".formatted(areaType, code)).statusCode());
    }

    private static JsonNode jsonFrom(String path) {
        final var response = gateway.get(path);
        assertEquals(200, response.statusCode());

        return JSON.readTree(response.body());
    }

}
