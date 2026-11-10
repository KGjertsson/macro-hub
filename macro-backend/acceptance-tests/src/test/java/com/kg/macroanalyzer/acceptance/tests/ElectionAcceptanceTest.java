package com.kg.macroanalyzer.acceptance.tests;

import com.kg.macroanalyzer.acceptance.support.AcceptanceStack;
import com.kg.macroanalyzer.acceptance.support.BackendService;
import com.kg.macroanalyzer.acceptance.support.Gateway;
import com.kg.macroanalyzer.acceptance.support.Sources;
import com.kg.macroanalyzer.acceptance.support.Valmyndigheten;
import com.kg.macroanalyzer.acceptance.support.Valmyndigheten.ResultFile;
import org.junit.jupiter.api.AutoClose;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

// The lists are answers about every area, so these journeys don't use the shared stack, where another journey's scrape
// would change them. The corrected file starts a stack of its own for the same reason. A Riksdag party's colour depends
// on the link to parliament, so colours are only asserted for a party outside the Riksdag
class ElectionAcceptanceTest {

    private static final String MUNICIPAL = "MunicipalElections";
    private static final String REGIONAL = "RegionElections";
    private static final String PARLIAMENTARY = "ParliamentElections";

    private static final ResultFile LUND = ResultFile.of("KF", "1281", "valmyndigheten/mandatfordelning-1281-KF.json");
    private static final ResultFile LUND_CORRECTED = ResultFile.of("KF", "1281", "valmyndigheten/mandatfordelning-1281-KF-corrected.json");
    private static final ResultFile KIRUNA = ResultFile.of("KF", "2584", "valmyndigheten/mandatfordelning-2584-KF.json");
    private static final ResultFile SKANE = ResultFile.of("RF", "12", "valmyndigheten/mandatfordelning-12-RF.json");
    private static final ResultFile NORRBOTTEN = ResultFile.of("RF", "25", "valmyndigheten/mandatfordelning-25-RF.json");
    private static final ResultFile VALKRETSAR = ResultFile.of("RD", "00", "valmyndigheten/mandatfordelning-00-RD.json");

    private static final String LUND_SUMMARY = """
            {
              "year": 2026, "code": "1281", "name": "Lund", "lanCode": "12", "totalSeats": 65, "turnout": 84.86,
              "largestParty": "S",
              "parties": [
                {"code":"0002","abbreviation":"S","name":"Arbetarepartiet-Socialdemokraterna","votes":22102,"voteShare":56.53,"seats":37},
                {"code":"0001","abbreviation":"M","name":"Moderaterna","votes":16996,"voteShare":43.47,"seats":28}
              ]
            }""";
    private static final String KIRUNA_SUMMARY = """
            {
              "year": 2026, "code": "2584", "name": "Kiruna", "lanCode": "25", "totalSeats": 41, "turnout": 78.5,
              "largestParty": "KP",
              "parties": [
                {"code":"3101","abbreviation":"KP","name":"Kirunapartiet","votes":8000,"voteShare":57.14,"seats":24},
                {"code":"0002","abbreviation":"S","name":"Arbetarepartiet-Socialdemokraterna","votes":6000,"voteShare":42.86,"seats":17}
              ]
            }""";
    private static final String SKANE_SUMMARY = """
            {
              "year": 2026, "code": "12", "name": "Skåne län", "lanCode": "12", "totalSeats": 149, "turnout": 81.2,
              "largestParty": "M",
              "parties": [
                {"code":"0001","abbreviation":"M","name":"Moderaterna","votes":360000,"voteShare":54.55,"seats":81},
                {"code":"0002","abbreviation":"S","name":"Arbetarepartiet-Socialdemokraterna","votes":300000,"voteShare":45.45,"seats":68}
              ]
            }""";
    private static final String NORRBOTTEN_SUMMARY = """
            {
              "year": 2026, "code": "25", "name": "Norrbottens län", "lanCode": "25", "totalSeats": 71, "turnout": 79.3,
              "largestParty": "S",
              "parties": [
                {"code":"0002","abbreviation":"S","name":"Arbetarepartiet-Socialdemokraterna","votes":60000,"voteShare":60.0,"seats":43},
                {"code":"0004","abbreviation":"V","name":"Vänsterpartiet","votes":40000,"voteShare":40.0,"seats":28}
              ]
            }""";
    private static final String STOCKHOLM_SUMMARY = """
            {
              "year": 2026, "code": "01", "name": "Stockholms kommun", "lanCode": "01", "totalSeats": 30, "turnout": 85.84,
              "largestParty": "S",
              "parties": [
                {"code":"0002","abbreviation":"S","name":"Arbetarepartiet-Socialdemokraterna","votes":180000,"voteShare":56.25,"seats":16},
                {"code":"0001","abbreviation":"M","name":"Moderaterna","votes":140000,"voteShare":43.75,"seats":14}
              ]
            }""";
    private static final String GOTLAND_SUMMARY = """
            {
              "year": 2026, "code": "09", "name": "Gotlands län", "lanCode": "09", "totalSeats": 2, "turnout": 83.0,
              "largestParty": "S",
              "parties": [
                {"code":"0002","abbreviation":"S","name":"Arbetarepartiet-Socialdemokraterna","votes":22000,"voteShare":55.0,"seats":1},
                {"code":"0001","abbreviation":"M","name":"Moderaterna","votes":18000,"voteShare":45.0,"seats":1}
              ]
            }""";
    private static final String MALMO_SUMMARY = """
            {
              "year": 2026, "code": "11", "name": "Malmö kommun", "lanCode": "12", "totalSeats": 10, "turnout": 81.0,
              "largestParty": "M",
              "parties": [
                {"code":"0001","abbreviation":"M","name":"Moderaterna","votes":50000,"voteShare":55.56,"seats":6},
                {"code":"0002","abbreviation":"S","name":"Arbetarepartiet-Socialdemokraterna","votes":40000,"voteShare":44.44,"seats":4}
              ]
            }""";

    private static final JsonMapper JSON = JsonMapper.builder().build();

    @AutoClose
    private static AcceptanceStack stack;

    private static Gateway gateway;
    private static Sources sources;

    @BeforeAll
    static void scrapeEveryElectionType() {
        stack = AcceptanceStack.startWith(List.of(BackendService.ELECTION));
        gateway = stack.gateway();
        sources = stack.sources();

        Valmyndigheten.publish(sources, LUND, KIRUNA, SKANE, NORRBOTTEN, VALKRETSAR);

        scrape(gateway, MUNICIPAL);
        scrape(gateway, REGIONAL);
        scrape(gateway, PARLIAMENTARY);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "?year=2026"})
    void aKommunElectionIsListedAndShownWithItsElectedMembers(String query) {
        // when
        final var list = jsonFrom(gateway, "/macro-analyzer/municipalities/elections" + query);
        final var detail = jsonFrom(gateway, "/macro-analyzer/municipalities/1281" + query);

        // then
        assertEquals(json("[%s,%s]".formatted(KIRUNA_SUMMARY, LUND_SUMMARY)), withoutColours(list));
        assertEquals(json("""
                {
                  "summary": %s,
                  "elected": [
                    {
                      "partyCode": "0002", "abbreviation": "S", "name": "Arbetarepartiet-Socialdemokraterna",
                      "members": [
                        {"name":"Sven Sandberg","order":1,"electedOn":"Ledamoten är vald på personröster"},
                        {"name":"Stina Sandell","order":2,"electedOn":"Ledamoten är vald som listtrogen efterträdare till dubbelvalsavvecklad"}
                      ]
                    },
                    {
                      "partyCode": "0001", "abbreviation": "M", "name": "Moderaterna",
                      "members": [
                        {"name":"Maja Malm","order":1,"electedOn":"Ledamoten är vald på personröster"}
                      ]
                    }
                  ],
                  "governance": null
                }""".formatted(LUND_SUMMARY)), withoutColours(detail));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "?year=2026"})
    void aRegionElectionIsListedAndShownWithItsElectedMembers(String query) {
        // when
        final var list = jsonFrom(gateway, "/macro-analyzer/regions/elections" + query);
        final var detail = jsonFrom(gateway, "/macro-analyzer/regions/12" + query);

        // then
        assertEquals(json("[%s,%s]".formatted(NORRBOTTEN_SUMMARY, SKANE_SUMMARY)), withoutColours(list));
        assertEquals(json("""
                {
                  "summary": %s,
                  "elected": [
                    {
                      "partyCode": "0001", "abbreviation": "M", "name": "Moderaterna",
                      "members": [
                        {"name":"Magnus Månsson","order":1,"electedOn":"Ledamoten är vald på personröster"},
                        {"name":"Märta Malmberg","order":2,"electedOn":"Ledamoten är vald som listtrogen efterträdare till dubbelvalsavvecklad"}
                      ]
                    },
                    {
                      "partyCode": "0002", "abbreviation": "S", "name": "Arbetarepartiet-Socialdemokraterna",
                      "members": [
                        {"name":"Sofia Svensson","order":1,"electedOn":"Ledamoten är vald på personröster"}
                      ]
                    }
                  ],
                  "governance": null
                }""".formatted(SKANE_SUMMARY)), withoutColours(detail));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "?year=2026"})
    void everyValkretsInTheRiksdagFileIsListedAndShownWithItsElectedMembers(String query) {
        // when
        final var list = jsonFrom(gateway, "/macro-analyzer/constituencies/elections" + query);
        final var detail = jsonFrom(gateway, "/macro-analyzer/constituencies/01" + query);

        // then
        assertEquals(
                json("[%s,%s,%s]".formatted(GOTLAND_SUMMARY, MALMO_SUMMARY, STOCKHOLM_SUMMARY)),
                withoutColours(list));
        assertEquals(json("""
                {
                  "summary": %s,
                  "elected": [
                    {
                      "partyCode": "0002", "abbreviation": "S", "name": "Arbetarepartiet-Socialdemokraterna",
                      "members": [
                        {"name":"Saga Nyström","order":1,"electedOn":"Ledamoten är vald på personröster"},
                        {"name":"Simon Ek","order":2,"electedOn":"Ledamoten är vald som listtrogen efterträdare till dubbelvalsavvecklad"}
                      ]
                    },
                    {
                      "partyCode": "0001", "abbreviation": "M", "name": "Moderaterna",
                      "members": [
                        {"name":"Mats Lindgren","order":1,"electedOn":"Ledamoten är vald på personröster"}
                      ]
                    }
                  ],
                  "governance": null
                }""".formatted(STOCKHOLM_SUMMARY)), withoutColours(detail));
    }

    @Test
    void aLocalPartyIsShownInTheColourValmyndighetenGivesIt() {
        // when
        final var kiruna = jsonFrom(gateway, "/macro-analyzer/municipalities/2584").get("summary");

        // then
        assertEquals("#7B2D8E", kiruna.get("largestPartyColor").asString());
        assertEquals("#7B2D8E", kiruna.get("parties").get(0).get("color").asString());
    }

    @ParameterizedTest
    @CsvSource({"municipalities,9999", "regions,99", "constituencies,99"})
    void anUnknownAreaIsNotFound(String areaType, String unknownCode) {
        assertEquals(404, gateway.get("/macro-analyzer/%s/%s".formatted(areaType, unknownCode)).statusCode());
    }

    @ParameterizedTest
    @ValueSource(strings = {"municipalities", "regions", "constituencies"})
    void aYearWithoutResultsHasNothingToShow(String areaType) {
        assertEquals(204, gateway.get("/macro-analyzer/%s/elections?year=2022".formatted(areaType)).statusCode());
    }

    @Test
    void aScrapeOfAnUnchangedIndexFetchesTheIndexAgainButNoFile() {
        // given
        final var indexFetches = sources.requestCount(Valmyndigheten.INDEX);
        final var lundBefore = withoutColours(jsonFrom(gateway, "/macro-analyzer/municipalities/1281"));

        // when
        scrape(gateway, MUNICIPAL);
        scrape(gateway, REGIONAL);
        scrape(gateway, PARLIAMENTARY);

        // then
        assertEquals(indexFetches + 3, sources.requestCount(Valmyndigheten.INDEX));
        List.of(LUND, KIRUNA, SKANE, NORRBOTTEN, VALKRETSAR)
                .forEach(file -> assertEquals(1, sources.requestCount(file.url()), file.url()));
        assertEquals(lundBefore, withoutColours(jsonFrom(gateway, "/macro-analyzer/municipalities/1281")));
    }

    @Test
    void aCorrectedFileChangesWhatTheUserSees() {
        try (var ownStack = AcceptanceStack.startWith(List.of(BackendService.ELECTION))) {
            // given
            final var ownGateway = ownStack.gateway();
            final var ownSources = ownStack.sources();
            Valmyndigheten.publish(ownSources, LUND, KIRUNA);
            scrape(ownGateway, MUNICIPAL);
            assertEquals(json(LUND_SUMMARY), withoutColours(jsonFrom(ownGateway, "/macro-analyzer/municipalities/1281").get("summary")));

            // when
            Valmyndigheten.publish(ownSources, LUND_CORRECTED, KIRUNA);
            scrape(ownGateway, MUNICIPAL);

            // then
            final var corrected = """
                    {
                      "year": 2026, "code": "1281", "name": "Lund", "lanCode": "12", "totalSeats": 65, "turnout": 85.01,
                      "largestParty": "M",
                      "parties": [
                        {"code":"0001","abbreviation":"M","name":"Moderaterna","votes":19998,"voteShare":51.15,"seats":33},
                        {"code":"0002","abbreviation":"S","name":"Arbetarepartiet-Socialdemokraterna","votes":19100,"voteShare":48.85,"seats":32}
                      ]
                    }""";
            assertEquals(
                    json("[%s,%s]".formatted(KIRUNA_SUMMARY, corrected)),
                    withoutColours(jsonFrom(ownGateway, "/macro-analyzer/municipalities/elections")));
            final var elected = withoutColours(jsonFrom(ownGateway, "/macro-analyzer/municipalities/1281")).get("elected");
            assertEquals(json("""
                    [
                      {
                        "partyCode": "0001", "abbreviation": "M", "name": "Moderaterna",
                        "members": [
                          {"name":"Maja Malm","order":1,"electedOn":"Ledamoten är vald på personröster"},
                          {"name":"Mikael Mattsson","order":2,"electedOn":"Ledamoten är vald på personröster"}
                        ]
                      },
                      {
                        "partyCode": "0002", "abbreviation": "S", "name": "Arbetarepartiet-Socialdemokraterna",
                        "members": [
                          {"name":"Sven Sandberg","order":1,"electedOn":"Ledamoten är vald på personröster"}
                        ]
                      }
                    ]"""), elected);
            assertEquals(2, ownSources.requestCount(LUND.url()));
            assertEquals(1, ownSources.requestCount(KIRUNA.url()));
        }
    }

    private static void scrape(Gateway gateway, String series) {
        assertEquals(202, gateway.post("/macro-analyzer/scrape/election/enqueue?name=" + series).statusCode());
        assertEquals(200, gateway.post("/macro-analyzer/scrape/election/scrape-from-queue").statusCode());
    }

    private static JsonNode withoutColours(JsonNode answer) {
        final var copy = answer.deepCopy();
        removeColours(copy);

        return copy;
    }

    private static void removeColours(JsonNode node) {
        if (node instanceof ObjectNode object) {
            object.remove(List.of("color", "largestPartyColor"));
        }
        node.values().forEach(ElectionAcceptanceTest::removeColours);
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
