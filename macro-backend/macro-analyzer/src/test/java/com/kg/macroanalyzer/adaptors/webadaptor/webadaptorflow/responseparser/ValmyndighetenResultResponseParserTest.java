package com.kg.macroanalyzer.adaptors.webadaptor.webadaptorflow.responseparser;

import com.kg.macroanalyzer.TestJsonReader;
import com.kg.macroanalyzer.adaptors.webadaptor.webadaptorflow.responserparser.ValmyndighetenResultResponseParser;
import com.kg.macroanalyzer.application.domain.localelection.ElectedMember;
import com.kg.macroanalyzer.application.domain.localelection.LocalElectionType;
import com.kg.macroanalyzer.application.domain.localelection.LocalPartyResult;
import com.kg.macroanalyzer.application.exceptions.ScrapeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ValmyndighetenResultResponseParserTest {

    private ValmyndighetenResultResponseParser parser;

    @BeforeEach
    void setUp() {
        parser = new ValmyndighetenResultResponseParser();
    }

    @Test
    void shouldParseVotesSeatsAndElectedMembers() throws ScrapeException {
        // given
        final var response = new TestJsonReader().readResource("json/valmyndigheten/mandatfordelning_1281_KF_sample.json");

        // when
        final var result = parser.parse(response);

        // then
        assertEquals(2026, result.year());
        assertEquals(LocalElectionType.KF, result.type());
        assertEquals("1281", result.areaCode());
        assertEquals("Lund", result.areaName());
        assertEquals("12", result.lanCode());
        assertEquals(65, result.totalSeats());
        assertEquals(new BigDecimal("84.86"), result.turnout());
        assertEquals(105382, result.eligibleVoters());
        assertEquals(89426, result.totalVotes());
        assertEquals(List.of(
                new LocalPartyResult("0001", "M", "Moderaterna", "#66BEE6", 16996, new BigDecimal("19.23"), 13),
                new LocalPartyResult("0002", "S", "Arbetarepartiet-Socialdemokraterna", result.parties().get(1).color(), 22102, new BigDecimal("25.0"), 16)
        ), result.parties());
        assertEquals(List.of(
                new ElectedMember("Rasmus Törnblom", "0001", "M", 1, "Ledamoten är vald på personröster"),
                new ElectedMember("Anders Roger Almgren", "0002", "S", 1, "Ledamoten är vald på personröster")
        ), result.elected());
    }

    @Test
    void shouldDropPartiesWithoutVotes_andUseRegionCodeAsLanCode() throws ScrapeException {
        // given
        final var response = """
                {
                  "valtyp": "RF",
                  "valdatum": "2026-09-13",
                  "valomrade": {
                    "namn": "Region Skåne",
                    "kod": "12",
                    "totaltAntalMandat": 149,
                    "rostfordelning": {
                      "rosterPaverkaMandat": {
                        "partiRoster": [
                          {"partikod": "0002", "partiforkortning": "S", "partibeteckning": "Socialdemokraterna", "fargkod": "#E8112D", "antalRoster": 10, "andelRoster": 100.0},
                          {"partikod": "9999", "partiforkortning": "X", "partibeteckning": "Tomma listan", "fargkod": "#000000", "antalRoster": 0, "andelRoster": 0.0}
                        ]
                      }
                    },
                    "mandatfordelning": {"partiLista": [{"partikod": "0002", "antalMandat": 149}]}
                  }
                }
                """;

        // when
        final var result = parser.parse(response);

        // then
        assertEquals("12", result.lanCode());
        assertEquals(1, result.parties().size());
        assertEquals(149, result.parties().getFirst().seats());
        assertEquals(List.of(), result.elected());
        assertEquals(null, result.turnout());
    }

    @Test
    void shouldThrow_whenResponseIsBlank() {
        assertThrows(ScrapeException.class, () -> parser.parse(" "));
    }

}
