package com.kg.macroanalyzer.acceptance.tests;

import com.kg.macroanalyzer.acceptance.support.AcceptanceStack;
import com.kg.macroanalyzer.acceptance.support.BackendService;
import com.kg.macroanalyzer.acceptance.support.Gateway;
import com.kg.macroanalyzer.acceptance.support.Sources;
import org.junit.jupiter.api.AutoClose;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Composition and member lists are answers about all of parliament, so these journeys don't use the shared stack, where
// another journey's scrape would change them. The re-scrape journey starts a stack of its own for the same reason.
// The stack runs no other service, so it also shows what the gateway answers for the routes of services that aren't running
class ParliamentAcceptanceTest {

    private static final String MEMBERS = "/personlista/";
    private static final String VOTES = "/voteringlista/";
    private static final String SPEECHES = "/anforandelista/";
    private static final String MOTIONS = "/dokumentlista/";

    private static final Map<String, String> ALL_MEMBERS_REQUEST = Map.of("rdlstatus", "samtliga", "utformat", "json");

    private static final String NO_VOTES = "{\"voteringlista\":{\"@antal\":\"0\"}}";
    private static final String NO_SPEECHES = "{\"anforandelista\":{\"@antal\":\"0\"}}";
    private static final String NO_MOTIONS = "{\"dokumentlista\":{\"@traffar\":\"0\"}}";

    private static final String SERVING = "Tjänstgörande riksdagsledamot";
    private static final String SERVING_SUBSTITUTE = "Tjänstgörande ersättare";
    private static final String FORMER = "Tidigare riksdagsledamot";

    private static final String ANNA = "0100000000001";
    private static final String BERTIL = "0100000000002";
    private static final String CECILIA = "0100000000003";
    private static final String DAVID = "0100000000004";
    private static final String EVA = "0100000000005";
    private static final String FREDRIK = "0100000000006";
    private static final String GRETA = "0100000000007";
    private static final String HANS = "0100000000008";
    private static final String INGRID = "0100000000009";

    private static final MemberRow ANNA_ROW = new MemberRow("Anna Andersson", "S", "Stockholms län", SERVING, 94.0);

    private static final JsonMapper JSON = JsonMapper.builder().build();

    @AutoClose
    private static AcceptanceStack stack;

    private static Gateway gateway;
    private static Sources sources;

    @BeforeAll
    static void scrapeMembersAndThenTheirActivity() {
        stack = AcceptanceStack.startWith(List.of(BackendService.PARLIAMENT));
        gateway = stack.gateway();
        sources = stack.sources();

        sources.serveJson(MEMBERS, ALL_MEMBERS_REQUEST, "riksdagen/personlista.json");
        sources.serveJson(VOTES, votesRequest(ANNA), "riksdagen/voteringlista-anna.json");
        sources.serveJson(SPEECHES, speechesRequest(ANNA), "riksdagen/anforandelista-anna.json");
        sources.serveJson(MOTIONS, motionsRequest(ANNA), "riksdagen/dokumentlista-anna.json");
        serveVotesWithoutOtherActivity(sources, BERTIL, votesOfOneSession(45, 3, 2, 0));
        serveVotesWithoutOtherActivity(sources, CECILIA, NO_VOTES);
        serveVotesWithoutOtherActivity(sources, DAVID, votesOfOneSession(150, 50, 100, 0));
        serveVotesWithoutOtherActivity(sources, EVA, votesOfOneSession(40, 10, 0, 0));
        serveVotesWithoutOtherActivity(sources, FREDRIK, votesOfOneSession(10, 5, 5, 5));
        serveVotesWithoutOtherActivity(sources, GRETA, votesOfOneSession(31, 10, 9, 0));
        serveVotesWithoutOtherActivity(sources, HANS, votesOfOneSession(70, 20, 10, 0));

        scrapeMembersThenActivity(gateway);
    }

    @Test
    void compositionShowsTheSeatsOfCurrentlyServingMembersPerParty() {
        // when
        final var composition = jsonFrom(gateway, "/macro-analyzer/parliament/composition");

        // then
        assertEquals(json("""
                [
                  {"code":"S","name":"Socialdemokraterna","color":"#EE2020","seats":3},
                  {"code":"M","name":"Moderaterna","color":"#1B49DD","seats":2},
                  {"code":"SD","name":"Sverigedemokraterna","color":"#DDDD00","seats":1}
                ]"""), composition.get("parties"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "?year=2026"})
    void theMemberListOfTheLatestTermShowsWhoSatThenWithTheirAttendance(String query) {
        // when
        final var members = memberRows(gateway, query);

        // then
        assertEquals(List.of(
                ANNA_ROW,
                new MemberRow("Bertil Berg", "S", "Södermanlands län", SERVING, 96.0),
                new MemberRow("Cecilia Carlsson", "S", "Örebro län", SERVING_SUBSTITUTE, null),
                new MemberRow("David Dahl", "M", "Västra Götalands läns västra", "Förste vice talman (tjänstgörande riksdagsledamot)", 66.7),
                new MemberRow("Eva Ek", "M", "Skåne läns södra", SERVING, 100.0),
                new MemberRow("Fredrik Falk", "SD", "Jönköpings län", SERVING, 80.0),
                new MemberRow("Greta Gran", "V", "Gotlands län", "Tjänstledig", 82.0)
        ), members);
    }

    @Test
    void theMemberListShowsTheActivityOfEachMember() {
        // when
        final var anna = jsonFrom(gateway, "/macro-analyzer/parliament/members").get(0);

        // then
        assertEquals("Anna Andersson", anna.get("name").asString());
        assertEquals(json("""
                {"yes":130,"no":55,"absent":12,"abstain":3}"""), anna.get("votes"));
        assertEquals(6, anna.get("speechCount").asInt());
        assertEquals(12, anna.get("motionCount").asInt());
    }

    @Test
    void theMemberListOfAnEarlierTermShowsWhoSatThen() {
        assertEquals(List.of(
                ANNA_ROW,
                new MemberRow("Hans Holm", "KD", "Kalmar län", FORMER, 90.0)
        ), memberRows(gateway, "?year=2022"));
    }

    @Test
    void aMemberOfATermBeforeTheLatestTwoHasNoActivity() {
        // when
        final var members = memberRows(gateway, "?year=2014");

        // then
        assertEquals(List.of(new MemberRow("Ingrid Isaksson", "L", "Uppsala län", FORMER, null)), members);
        final var ingrid = jsonFrom(gateway, "/macro-analyzer/parliament/members?year=2014").get(0);
        assertTrue(ingrid.get("votes").isNull());
        assertTrue(ingrid.get("speechCount").isNull());
        assertTrue(ingrid.get("motionCount").isNull());
    }

    @Test
    void theActivityScrapeAsksForEachMemberOfTheLatestTwoTermsOnce() {
        final var expectedMembers = List.of(ANNA, BERTIL, CECILIA, DAVID, EVA, FREDRIK, GRETA, HANS);

        assertEquals(1, sources.requestCount(MEMBERS, ALL_MEMBERS_REQUEST));
        expectedMembers.forEach(member -> {
            assertEquals(1, sources.requestCount(VOTES, votesRequest(member)), "votes of " + member);
            assertEquals(1, sources.requestCount(SPEECHES, speechesRequest(member)), "speeches of " + member);
            assertEquals(1, sources.requestCount(MOTIONS, motionsRequest(member)), "motions of " + member);
        });
        assertEquals(expectedMembers.size(), sources.requestCount(VOTES));
        assertEquals(expectedMembers.size(), sources.requestCount(SPEECHES));
        assertEquals(expectedMembers.size(), sources.requestCount(MOTIONS));
    }

    @Test
    void aMembersDetailShowsWhatRiksdagenSaysAboutThem() {
        // when
        final var detail = (ObjectNode) jsonFrom(gateway, "/macro-analyzer/parliament/members/" + ANNA);

        // then
        ((ObjectNode) detail.get("summary")).remove("yearsInParliament");
        detail.get("assignments").forEach(assignment -> ((ObjectNode) assignment).remove("current"));
        assertEquals(json("""
                {
                  "summary": {
                    "id": "0100000000001",
                    "name": "Anna Andersson",
                    "party": "S",
                    "constituency": "Stockholms län",
                    "status": "Tjänstgörande riksdagsledamot",
                    "imageUrl": "https://data.riksdagen.se/filarkiv/bilder/ledamot/anna_192.jpg",
                    "currentRole": "Ordförande, Finansutskottet",
                    "attendancePct": 94.0,
                    "votes": {"yes":130,"no":55,"absent":12,"abstain":3},
                    "speechCount": 6,
                    "motionCount": 12
                  },
                  "imageUrlLarge": "https://data.riksdagen.se/filarkiv/bilder/ledamot/anna_max.jpg",
                  "birthYear": 1975,
                  "gender": "kvinna",
                  "residence": "Sundbyberg",
                  "runningNextElection": true,
                  "contact": {
                    "email": "anna.andersson@riksdagen.se",
                    "phone": "08-786 40 00",
                    "website": "https://www.example.se/anna"
                  },
                  "assignments": [
                    {"type":"uppdrag","organCode":"FiU","organName":"Finansutskottet","role":"Ordförande","status":null,"from":"2026-10-06","to":"2999-12-31"},
                    {"type":"kammaruppdrag","organCode":"kam","organName":"Kammaren","role":"Riksdagsledamot","status":"Tjänstgörande","from":"2026-09-28","to":"2030-09-23"},
                    {"type":"partiuppdrag","organCode":"S","organName":"Socialdemokraterna","role":"Ledamot","status":null,"from":"2023-04-01","to":"2999-12-31"},
                    {"type":"uppdrag","organCode":"AU","organName":"Arbetsmarknadsutskottet","role":"Suppleant","status":null,"from":"2022-10-04","to":"2026-09-27"},
                    {"type":"kammaruppdrag","organCode":"kam","organName":"Kammaren","role":"Riksdagsledamot","status":"Tjänstgörande","from":"2022-09-26","to":"2026-09-27"}
                  ],
                  "biography": [
                    {"label":"Yrken","text":"Nationalekonom\\nUtredare"},
                    {"label":"Utbildning","text":"Civilekonom, Uppsala universitet"}
                  ],
                  "votes": {"yes":130,"no":55,"absent":12,"abstain":3},
                  "votesBySession": [
                    {"riksmote":"2025/26","yes":100,"no":40,"absent":8,"abstain":2},
                    {"riksmote":"2026/27","yes":30,"no":15,"absent":4,"abstain":1}
                  ],
                  "speechCount": 6,
                  "motionCount": 12,
                  "recentSpeeches": [
                    {"title":"Statens budget: Utgiftsområde 2","date":"2026-10-28","url":"https://www.example.se/protokoll/6"},
                    {"title":"Frågestund","date":"2026-10-22","url":"https://www.example.se/protokoll/5"},
                    {"title":"Ekonomisk politik","date":"2026-10-15","url":"https://www.example.se/protokoll/4"},
                    {"title":"Skatter: Moms","date":"2026-05-20","url":"https://www.example.se/protokoll/3"},
                    {"title":"Vårpropositionen","date":"2026-04-21","url":"https://www.example.se/protokoll/2"}
                  ],
                  "recentMotions": [
                    {"title":"En tryggare välfärd","date":"2026-10-05","url":"https://data.riksdagen.se/dokument/HD023"},
                    {"title":"Lägre skatt på arbete","date":"2026-09-30","url":"https://data.riksdagen.se/dokument/HD012"}
                  ]
                }"""), detail);
    }

    @Test
    void anUnknownMemberIsNotFound() {
        assertEquals(404, gateway.get("/macro-analyzer/parliament/members/0999999999999").statusCode());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "/macro-analyzer/series-config",
            "/macro-analyzer/regional-statistics/indicators",
            "/macro-analyzer/municipalities/elections"
    })
    void theRoutesOfAServiceThatIsntRunningAnswerBadGateway(String path) {
        assertEquals(502, gateway.get(path).statusCode());
    }

    @Test
    void theRestOfTheSystemKeepsWorkingWithoutTheOtherServices() {
        assertEquals(200, gateway.get("/macro-analyzer/parliament/parties").statusCode());
        assertEquals(200, gateway.get("/parliament").statusCode());
        assertEquals(404, gateway.get("/macro-analyzer/unknown").statusCode());
    }

    @Test
    void aRescrapeShowsANewPartyAMemberWhoLeftAndTheirReplacement() {
        // given
        final var karin = "0200000000001";
        final var lars = "0200000000002";
        final var maria = "0200000000003";
        final var nils = "0200000000004";

        try (var ownStack = AcceptanceStack.startWith(List.of(BackendService.PARLIAMENT))) {
            final var ownGateway = ownStack.gateway();
            final var ownSources = ownStack.sources();
            ownSources.serveJson(MEMBERS, ALL_MEMBERS_REQUEST, "riksdagen/personlista-before-changes.json");
            serveVotesWithoutOtherActivity(ownSources, karin, votesOfOneSession(45, 0, 5, 0));
            serveVotesWithoutOtherActivity(ownSources, lars, votesOfOneSession(25, 0, 25, 0));
            serveVotesWithoutOtherActivity(ownSources, maria, votesOfOneSession(50, 0, 0, 0));

            // when
            scrapeMembersThenActivity(ownGateway);

            // then
            assertEquals(Map.of("S", 2, "C", 1), seatsPerParty(ownGateway));
            assertEquals(List.of(
                    new MemberRow("Karin Kvist", "S", "Hallands län", SERVING, 90.0),
                    new MemberRow("Lars Lund", "S", "Västerbottens län", SERVING, 50.0),
                    new MemberRow("Maria Moberg", "C", "Dalarnas län", SERVING, 100.0)
            ), memberRows(ownGateway, ""));

            // given
            ownSources.serveJson(MEMBERS, ALL_MEMBERS_REQUEST, "riksdagen/personlista-after-changes.json");
            serveVotesWithoutOtherActivity(ownSources, karin, votesOfOneSession(70, 0, 30, 0));
            serveVotesWithoutOtherActivity(ownSources, nils, votesOfOneSession(15, 0, 5, 0));

            // when
            scrapeMembersThenActivity(ownGateway);

            // then
            assertEquals(Map.of("S", 1, "C", 1, "V", 1), seatsPerParty(ownGateway));
            assertEquals(List.of(
                    new MemberRow("Karin Kvist", "V", "Hallands län", SERVING, 70.0),
                    new MemberRow("Lars Lund", "S", "Västerbottens län", FORMER, 50.0),
                    new MemberRow("Maria Moberg", "C", "Dalarnas län", SERVING, 100.0),
                    new MemberRow("Nils Norén", "S", "Västerbottens län", SERVING_SUBSTITUTE, 75.0)
            ), memberRows(ownGateway, ""));
            final var larsDetail = jsonFrom(ownGateway, "/macro-analyzer/parliament/members/" + lars);
            assertEquals("2026-10-31", larsDetail.get("assignments").get(0).get("to").asString());
            assertEquals(2, ownSources.requestCount(MEMBERS, ALL_MEMBERS_REQUEST));
            assertEquals(2, ownSources.requestCount(VOTES, votesRequest(karin)));
            assertEquals(1, ownSources.requestCount(VOTES, votesRequest(nils)));
        }
    }

    private static void scrapeMembersThenActivity(Gateway gateway) {
        scrape(gateway, "MembersOfParliament");
        scrape(gateway, "MembersOfParliamentActivity");
    }

    private static void scrape(Gateway gateway, String series) {
        assertEquals(202, gateway.post("/macro-analyzer/scrape/parliament/enqueue?name=" + series).statusCode());
        assertEquals(200, gateway.post("/macro-analyzer/scrape/parliament/scrape-from-queue").statusCode());
    }

    private static void serveVotesWithoutOtherActivity(Sources sources, String member, String votes) {
        sources.serveJsonBody(VOTES, votesRequest(member), votes);
        sources.serveJsonBody(SPEECHES, speechesRequest(member), NO_SPEECHES);
        sources.serveJsonBody(MOTIONS, motionsRequest(member), NO_MOTIONS);
    }

    // Riksdagen answers a single row as an object rather than a list of one
    private static String votesOfOneSession(int yes, int no, int absent, int abstain) {
        return """
                {"voteringlista":{"votering":{"rm":"2026/27","Ja":"%d","Nej":"%d","Frånvarande":"%d","Avstår":"%d"}}}"""
                .formatted(yes, no, absent, abstain);
    }

    private static Map<String, String> votesRequest(String member) {
        return Map.of("iid", member, "utformat", "json", "gruppering", "rm");
    }

    private static Map<String, String> speechesRequest(String member) {
        return Map.of("iid", member, "utformat", "json");
    }

    private static Map<String, String> motionsRequest(String member) {
        return Map.of("iid", member, "utformat", "json", "doktyp", "mot");
    }

    private static Map<String, Integer> seatsPerParty(Gateway gateway) {
        return jsonFrom(gateway, "/macro-analyzer/parliament/composition").get("parties").valueStream()
                .collect(Collectors.toMap(party -> party.get("code").asString(), party -> party.get("seats").asInt()));
    }

    private static List<MemberRow> memberRows(Gateway gateway, String query) {
        return jsonFrom(gateway, "/macro-analyzer/parliament/members" + query).valueStream()
                .map(member -> new MemberRow(
                        member.get("name").asString(),
                        member.get("party").asString(),
                        member.get("constituency").asString(),
                        member.get("status").asString(),
                        member.get("attendancePct").isNull() ? null : member.get("attendancePct").asDouble()))
                .toList();
    }

    private static JsonNode jsonFrom(Gateway gateway, String path) {
        final var response = gateway.get(path);
        assertEquals(200, response.statusCode());

        return JSON.readTree(response.body());
    }

    private static JsonNode json(String text) {
        return JSON.readTree(text);
    }

    private record MemberRow(String name, String party, String constituency, String status, Double attendancePct) {
    }

}
