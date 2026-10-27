package com.kg.macroanalyzer.adaptors.webadaptor.webadaptorflow.responseparser;

import com.kg.macroanalyzer.adaptors.webadaptor.webadaptorflow.responserparser.ValmyndighetenIndexResponseParser;
import com.kg.macroanalyzer.application.domain.localelection.LocalElectionType;
import com.kg.macroanalyzer.application.exceptions.ScrapeException;
import com.kg.macroanalyzer.application.ports.driven.LocalElectionFile;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ValmyndighetenIndexResponseParserTest {

    private static final String BASE_URL = "https://resultat.val.se/resultatfiler/val2026";
    private static final String INDEX = """
            5bc0987a953ea4ad5d607f63bbdad0a6  ./p/kf/Val_2026_preliminar_0180_KF.zip
            3f527948f4e6ebe033236637e292538f  ./s/kf/Val_2026_slutlig_OS_KF.zip
            6bc39a6c9e5a9ecc4eb4f82080abff28  ./s/kf/Val_2026_slutlig_0180_KF.zip
            c47e5b211e79c7c4544ea3742248a5b1  ./s/kf/Val_2026_slutlig_1281_KF.zip
            9ff3844df68b368a1c7afbb1e484f5c3  ./s/rf/Val_2026_slutlig_12_RF.zip
            8162d2cd73ecd0a24ef21632f272dfb2  ./s/rd/Val_2026_slutlig_00_RD.zip
            0d6f1e2b3c4a59687a8b9c0d1e2f3a4b  ./p/rd/Val_2026_preliminar_00_RD.zip
            """;

    @Test
    void shouldKeepFinalMunicipalFiles_only() throws ScrapeException {
        // when
        final var files = new ValmyndighetenIndexResponseParser(BASE_URL, LocalElectionType.KF).parse(INDEX);

        // then
        assertEquals(List.of(
                new LocalElectionFile("0180", BASE_URL + "/s/kf/Val_2026_slutlig_0180_KF.zip", "6bc39a6c9e5a9ecc4eb4f82080abff28"),
                new LocalElectionFile("1281", BASE_URL + "/s/kf/Val_2026_slutlig_1281_KF.zip", "c47e5b211e79c7c4544ea3742248a5b1")
        ), files);
    }

    @Test
    void shouldKeepFinalRegionFiles_only() throws ScrapeException {
        // when
        final var files = new ValmyndighetenIndexResponseParser(BASE_URL, LocalElectionType.RF).parse(INDEX);

        // then
        assertEquals(List.of(
                new LocalElectionFile("12", BASE_URL + "/s/rf/Val_2026_slutlig_12_RF.zip", "9ff3844df68b368a1c7afbb1e484f5c3")
        ), files);
    }

    @Test
    void shouldKeepFinalRiksdagFile_only() throws ScrapeException {
        // when
        final var files = new ValmyndighetenIndexResponseParser(BASE_URL, LocalElectionType.RD).parse(INDEX);

        // then
        assertEquals(List.of(
                new LocalElectionFile("00", BASE_URL + "/s/rd/Val_2026_slutlig_00_RD.zip", "8162d2cd73ecd0a24ef21632f272dfb2")
        ), files);
    }

    @Test
    void shouldThrow_whenIndexIsEmpty() {
        final var parser = new ValmyndighetenIndexResponseParser(BASE_URL, LocalElectionType.KF);

        assertThrows(ScrapeException.class, () -> parser.parse(""));
    }

}
