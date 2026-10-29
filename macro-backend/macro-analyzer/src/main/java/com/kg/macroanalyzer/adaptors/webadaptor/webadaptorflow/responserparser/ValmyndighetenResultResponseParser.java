package com.kg.macroanalyzer.adaptors.webadaptor.webadaptorflow.responserparser;

import com.kg.macroanalyzer.application.domain.localelection.ElectedMember;
import com.kg.macroanalyzer.application.domain.localelection.LocalElectionResult;
import com.kg.macroanalyzer.application.domain.localelection.LocalElectionType;
import com.kg.macroanalyzer.application.domain.localelection.LocalPartyResult;
import com.kg.macroanalyzer.shared.web.ResponseParser;
import com.kg.macroanalyzer.shared.web.ScrapeException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// Parses a mandatfördelning file: votes per party, seats per party and the elected members.
// KF and RF files hold one area; the Riksdag file holds every valkrets under valkretsLista
public record ValmyndighetenResultResponseParser() implements ResponseParser<String, List<LocalElectionResult>> {

    private static final JsonMapper JSON_MAPPER = JsonMapper.builder().build();

    // Valkretsar 01-02 split Stockholms län, 11-14 Skåne and 16-20 Västra Götaland; the others are whole län
    private static final Map<String, String> VALKRETS_LAN = Map.ofEntries(
            Map.entry("01", "01"), Map.entry("02", "01"), Map.entry("03", "03"), Map.entry("04", "04"),
            Map.entry("05", "05"), Map.entry("06", "06"), Map.entry("07", "07"), Map.entry("08", "08"),
            Map.entry("09", "09"), Map.entry("10", "10"), Map.entry("11", "12"), Map.entry("12", "12"),
            Map.entry("13", "12"), Map.entry("14", "12"), Map.entry("15", "13"), Map.entry("16", "14"),
            Map.entry("17", "14"), Map.entry("18", "14"), Map.entry("19", "14"), Map.entry("20", "14"),
            Map.entry("21", "17"), Map.entry("22", "18"), Map.entry("23", "19"), Map.entry("24", "20"),
            Map.entry("25", "21"), Map.entry("26", "22"), Map.entry("27", "23"), Map.entry("28", "24"),
            Map.entry("29", "25")
    );

    @Override
    public List<LocalElectionResult> parse(String response) throws ScrapeException {
        final var root = readTree(response);
        final var area = root.path("valomrade");
        final var type = LocalElectionType.valueOf(root.path("valtyp").asString());
        final var year = LocalDate.parse(root.path("valdatum").asString()).getYear();
        if (type == LocalElectionType.RD) {
            return area.path("valkretsLista").valueStream()
                    .map(valkrets -> constituency(year, valkrets))
                    .toList();
        }
        final var code = area.path("kod").asString();

        return List.of(result(year, type, area)
                .areaCode(code)
                .areaName(area.path("namn").asString())
                .lanCode(type == LocalElectionType.KF ? code.substring(0, 2) : code)
                .totalSeats(area.path("totaltAntalMandat").asInt())
                .build());
    }

    // A valkrets only states its fixed seats, so the total is summed from the parties' seats, which include
    // the utjämningsmandat that were assigned to the valkrets
    private static LocalElectionResult constituency(int year, JsonNode valkrets) {
        final var code = valkrets.path("kod").asString();

        return result(year, LocalElectionType.RD, valkrets)
                .areaCode(code)
                .areaName(valkrets.path("namnValkrets").asString())
                .lanCode(VALKRETS_LAN.getOrDefault(code, code))
                .totalSeats(seatsByParty(valkrets).values().stream().mapToInt(Integer::intValue).sum())
                .build();
    }

    private static LocalElectionResult.LocalElectionResultBuilder result(int year, LocalElectionType type, JsonNode area) {
        return LocalElectionResult.builder()
                .year(year)
                .type(type)
                .turnout(decimal(area.path("valdeltagande")))
                .eligibleVoters(integer(area.path("antalRostberattigade")))
                .totalVotes(integer(area.path("totaltAntalRoster")))
                .parties(parties(area, seatsByParty(area)))
                .elected(elected(area));
    }

    private static JsonNode readTree(String response) throws ScrapeException {
        if (response == null || response.isBlank()) {
            throw new ScrapeException("Empty result file from Valmyndigheten");
        }
        try {
            return JSON_MAPPER.readTree(response);
        } catch (JacksonException e) {
            throw new ScrapeException("Failed to parse Valmyndigheten result file: " + e.getMessage());
        }
    }

    private static Map<String, Integer> seatsByParty(JsonNode area) {
        return area.path("mandatfordelning").path("partiLista").valueStream()
                .collect(Collectors.toMap(
                        p -> p.path("partikod").asString(),
                        p -> p.path("antalMandat").asInt(),
                        Integer::sum
                ));
    }

    // Every registered party is listed, so those without votes are dropped
    private static List<LocalPartyResult> parties(JsonNode area, Map<String, Integer> seatsByParty) {
        return area.path("rostfordelning").path("rosterPaverkaMandat").path("partiRoster").valueStream()
                .filter(p -> p.path("antalRoster").asInt() > 0)
                .map(p -> LocalPartyResult.builder()
                        .code(p.path("partikod").asString())
                        .abbreviation(p.path("partiforkortning").asString())
                        .name(p.path("partibeteckning").asString())
                        .color(p.path("fargkod").asString(null))
                        .votes(p.path("antalRoster").asInt())
                        .voteShare(decimal(p.path("andelRoster")))
                        .seats(seatsByParty.getOrDefault(p.path("partikod").asString(), 0))
                        .build())
                .toList();
    }

    private static List<ElectedMember> elected(JsonNode area) {
        return area.path("valda").path("partiLedamoterLista").valueStream()
                .flatMap(party -> party.path("ledamoter").valueStream()
                        .map(m -> ElectedMember.builder()
                                .name(m.path("namn").asString())
                                .partyCode(party.path("partikod").asString())
                                .partyAbbreviation(party.path("partiforkortning").asString())
                                .order(m.path("invalsordning").asInt())
                                .electedOn(m.path("valgrundText").asString(null))
                                .build()))
                .toList();
    }

    private static BigDecimal decimal(JsonNode node) {
        return node.isNumber() ? BigDecimal.valueOf(node.asDouble()) : null;
    }

    private static Integer integer(JsonNode node) {
        return node.isNumber() ? node.asInt() : null;
    }

}
