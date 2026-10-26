package com.kg.macroanalyzer.adaptors.webadaptor.webadaptorflow.responseparser;

import com.kg.macroanalyzer.adaptors.webadaptor.webadaptorflow.responserparser.RiksdagenMotionResponseParser;
import com.kg.macroanalyzer.adaptors.webadaptor.webadaptorflow.responserparser.RiksdagenSpeechResponseParser;
import com.kg.macroanalyzer.adaptors.webadaptor.webadaptorflow.responserparser.RiksdagenVoteResponseParser;
import com.kg.macroanalyzer.application.domain.parliament.DocumentPage;
import com.kg.macroanalyzer.application.domain.parliament.ParliamentDocument;
import com.kg.macroanalyzer.application.domain.parliament.SessionVotes;
import com.kg.macroanalyzer.application.domain.parliament.VoteTotals;
import com.kg.macroanalyzer.application.exceptions.ScrapeException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class RiksdagenActivityResponseParserTest {

    @Test
    void shouldParseVotesPerSession_andSumThemIntoTotals() throws ScrapeException {
        // given
        final var response = """
                {"voteringlista": {"@antal": "2", "votering": [
                  {"rm": "2025/26", "Ja": "573", "Nej": "1", "Frånvarande": "223", "Avstår": ""},
                  {"rm": "2024/25", "Ja": "525", "Nej": "2", "Frånvarande": "125", "Avstår": "3"}
                ]}}
                """;

        // when
        final var result = new RiksdagenVoteResponseParser().parse(response);

        // then
        assertEquals(new VoteTotals(1098, 3, 348, 3, List.of(
                new SessionVotes("2024/25", 525, 2, 125, 3),
                new SessionVotes("2025/26", 573, 1, 223, 0)
        )), result);
    }

    @Test
    void shouldParseSingleSession_whenVoteringIsAnObject() throws ScrapeException {
        // given
        final var response = """
                {"voteringlista": {"@antal": "1", "votering": {
                  "rm": "2026/27", "Ja": "", "Nej": "1", "Frånvarande": "", "Avstår": ""
                }}}
                """;

        // when
        final var result = new RiksdagenVoteResponseParser().parse(response);

        // then
        assertEquals(new VoteTotals(0, 1, 0, 0, List.of(new SessionVotes("2026/27", 0, 1, 0, 0))), result);
    }

    @Test
    void shouldReturnNoVotes_whenVoteringMissing() throws ScrapeException {
        // given
        final var response = """
                {"voteringlista": {"@antal": "0"}}
                """;

        // when
        final var result = new RiksdagenVoteResponseParser().parse(response);

        // then
        assertEquals(VoteTotals.NONE, result);
    }

    @Test
    void shouldCountAllSpeeches_andKeepMostRecent() throws ScrapeException {
        // given
        final var response = """
                {"anforandelista": {"@antal": "3", "anforande": [
                  {"avsnittsrubrik": "Allmänpolitisk debatt", "underrubrik": "Näringspolitik", "dok_datum": "2026-10-14", "protokoll_url_www": "https://riksdagen.se/a"},
                  {"avsnittsrubrik": "Budget", "underrubrik": "", "dok_datum": "2026-10-01", "protokoll_url_www": "https://riksdagen.se/b"},
                  {"avsnittsrubrik": "Skatter", "underrubrik": "", "dok_datum": "2026-09-30", "protokoll_url_www": "https://riksdagen.se/c"}
                ]}}
                """;

        // when
        final var result = new RiksdagenSpeechResponseParser(2).parse(response);

        // then
        assertEquals(3, result.total());
        assertEquals(List.of(
                new ParliamentDocument("Allmänpolitisk debatt: Näringspolitik", "2026-10-14", "https://riksdagen.se/a"),
                new ParliamentDocument("Budget", "2026-10-01", "https://riksdagen.se/b")
        ), result.recent());
    }

    @Test
    void shouldParseSingleSpeechObject() throws ScrapeException {
        // given
        final var response = """
                {"anforandelista": {"@antal": "1", "anforande":
                  {"avsnittsrubrik": "Budget", "dok_datum": "2026-10-01", "protokoll_url_www": "https://riksdagen.se/b"}
                }}
                """;

        // when
        final var result = new RiksdagenSpeechResponseParser(5).parse(response);

        // then
        assertEquals(1, result.total());
    }

    @Test
    void shouldUseHitCountForMotions_andMakeUrlsAbsolute() throws ScrapeException {
        // given
        final var response = """
                {"dokumentlista": {"@traffar": "68", "dokument": [
                  {"titel": "Samhällsbyggnadsutbildningar", "datum": "2025-10-07", "dokument_url_html": "//data.riksdagen.se/dokument/HD023796.html"}
                ]}}
                """;

        // when
        final var result = new RiksdagenMotionResponseParser().parse(response);

        // then
        assertEquals(new DocumentPage(68, List.of(new ParliamentDocument(
                "Samhällsbyggnadsutbildningar", "2025-10-07", "https://data.riksdagen.se/dokument/HD023796.html"
        ))), result);
    }

    @Test
    void shouldThrowScrapeException_whenResponseIsEmpty() {
        // given
        final var parser = new RiksdagenMotionResponseParser();

        // when / then
        assertThrows(ScrapeException.class, () -> parser.parse(""));
    }

}
