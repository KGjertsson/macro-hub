package com.kg.macroanalyzer.acceptance;

import com.kg.macroanalyzer.acceptance.Valmyndigheten.ResultFile;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

// Election fetches the colours in the background, so what it answers can only be expected to change eventually. Each
// journey owns its stack, because whether parliament has been running is the point of it
class PartyColoursAcceptanceTest {

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private static final BackendService ELECTION_RETRYING_EVERY_SECOND =
            BackendService.ELECTION.withEnvironment(Map.of("ELECTION_PARLIAMENT_RETRYINTERVAL", "1s"));
    private static final Duration PARLIAMENT_REACHED_WITHIN = Duration.ofSeconds(30);

    private static final String MUNICIPAL = "MunicipalElections";
    private static final ResultFile LUND = ResultFile.of("KF", "1281", "valmyndigheten/mandatfordelning-1281-KF.json");
    private static final ResultFile KIRUNA = ResultFile.of("KF", "2584", "valmyndigheten/mandatfordelning-2584-KF.json");

    private static final String LOCAL_PARTY = "KP";
    private static final String LOCAL_PARTY_COLOUR = "#7B2D8E";
    private static final Map<String, String> VALMYNDIGHETENS_COLOURS = Map.of(
            "S", "#FF0000",
            "M", "#66BEE6",
            LOCAL_PARTY, LOCAL_PARTY_COLOUR);

    @Test
    void aRiksdagPartyHasParliamentsColourAndALocalPartyKeepsValmyndighetens() {
        try (var stack = AcceptanceStack.startWith(List.of(BackendService.PARLIAMENT, ELECTION_RETRYING_EVERY_SECOND))) {
            // given
            final var gateway = stack.gateway();
            Valmyndigheten.publish(stack.sources(), LUND, KIRUNA);

            // when
            scrapeMunicipalElections(gateway);

            // then
            final var colours = parliamentsColoursAndTheLocalPartys(gateway);
            Eventually.assertWithin(
                    PARLIAMENT_REACHED_WITHIN,
                    "Election should show parliament's colours",
                    () -> assertElectionShows(colours, gateway));
        }
    }

    @Test
    void withoutParliamentElectionAnswersInValmyndighetensColoursAndSwitchesOnceParliamentHasStarted() {
        try (var stack = AcceptanceStack.startWith(List.of(ELECTION_RETRYING_EVERY_SECOND))) {
            // given
            final var gateway = stack.gateway();
            final var sources = stack.sources();
            Valmyndigheten.publish(sources, LUND, KIRUNA);
            scrapeMunicipalElections(gateway);

            // then
            assertEquals(502, gateway.get("/macro-analyzer/parliament/parties").statusCode());
            assertElectionShows(VALMYNDIGHETENS_COLOURS, gateway);

            // when
            final var indexFetches = sources.requestCount(Valmyndigheten.INDEX);
            stack.startLater(BackendService.PARLIAMENT);

            // then
            Eventually.assertWithin(
                    PARLIAMENT_REACHED_WITHIN,
                    "The gateway should reach parliament once it has started",
                    () -> assertEquals(200, gateway.get("/macro-analyzer/parliament/parties").statusCode()));
            final var colours = parliamentsColoursAndTheLocalPartys(gateway);
            Eventually.assertWithin(
                    PARLIAMENT_REACHED_WITHIN,
                    "Election should switch to parliament's colours without a restart or a new scrape",
                    () -> assertElectionShows(colours, gateway));
            assertEquals(indexFetches, sources.requestCount(Valmyndigheten.INDEX));
            assertEquals(1, sources.requestCount(LUND.url()));
            assertEquals(1, sources.requestCount(KIRUNA.url()));
        }
    }

    private static void assertElectionShows(Map<String, String> colours, Gateway gateway) {
        final var list = jsonFrom(gateway, "/macro-analyzer/municipalities/elections");
        assertEquals(2, list.size());
        assertAreaColours(area(list, "1281"), "S", colours, "S", "M");
        assertAreaColours(area(list, "2584"), LOCAL_PARTY, colours, LOCAL_PARTY, "S");

        final var lund = jsonFrom(gateway, "/macro-analyzer/municipalities/1281");
        assertAreaColours(lund.get("summary"), "S", colours, "S", "M");
        assertEquals(coloursOfThese(colours, "S", "M"), coloursByAbbreviation(lund.get("elected")));
        final var kiruna = jsonFrom(gateway, "/macro-analyzer/municipalities/2584");
        assertAreaColours(kiruna.get("summary"), LOCAL_PARTY, colours, LOCAL_PARTY, "S");
    }

    private static void assertAreaColours(JsonNode summary, String largestParty, Map<String, String> colours, String... parties) {
        assertEquals(largestParty, summary.get("largestParty").asString());
        assertEquals(colours.get(largestParty), summary.get("largestPartyColor").asString());
        assertEquals(coloursOfThese(colours, parties), coloursByAbbreviation(summary.get("parties")));
    }

    // Read through the gateway so the expectation is what parliament answers, and compared with the fixtures, as a test
    // on colours that happen to match would pass without any link between the services
    private static Map<String, String> parliamentsColoursAndTheLocalPartys(Gateway gateway) {
        final var parties = jsonFrom(gateway, "/macro-analyzer/parliament/parties");
        final var parliaments = parties.valueStream().collect(Collectors.toMap(
                party -> party.get("code").asString(),
                party -> party.get("color").asString()));
        assertFalse(parliaments.containsKey(LOCAL_PARTY));
        List.of("S", "M").forEach(party ->
                assertNotEquals(VALMYNDIGHETENS_COLOURS.get(party), parliaments.get(party), "The colour of " + party));

        return Map.of(
                "S", parliaments.get("S"),
                "M", parliaments.get("M"),
                LOCAL_PARTY, LOCAL_PARTY_COLOUR);
    }

    private static Map<String, String> coloursOfThese(Map<String, String> colours, String... parties) {
        return List.of(parties).stream().collect(Collectors.toMap(Function.identity(), colours::get));
    }

    private static Map<String, String> coloursByAbbreviation(JsonNode parties) {
        return parties.valueStream().collect(Collectors.toMap(
                party -> party.get("abbreviation").asString(),
                party -> party.get("color").asString()));
    }

    private static JsonNode area(JsonNode list, String code) {
        return list.valueStream()
                .filter(summary -> code.equals(summary.get("code").asString()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No area %s in %s".formatted(code, list)));
    }

    private static void scrapeMunicipalElections(Gateway gateway) {
        assertEquals(202, gateway.post("/macro-analyzer/scrape/election/enqueue?name=" + MUNICIPAL).statusCode());
        assertEquals(200, gateway.post("/macro-analyzer/scrape/election/scrape-from-queue").statusCode());
    }

    private static JsonNode jsonFrom(Gateway gateway, String path) {
        final var response = gateway.get(path);
        assertEquals(200, response.statusCode(), path);

        return JSON.readTree(response.body());
    }

}
