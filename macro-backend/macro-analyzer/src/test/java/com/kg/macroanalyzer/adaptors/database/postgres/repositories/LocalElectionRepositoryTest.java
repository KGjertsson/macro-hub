package com.kg.macroanalyzer.adaptors.database.postgres.repositories;

import com.kg.macroanalyzer.TestJsonReader;
import com.kg.macroanalyzer.adaptors.webadaptor.webadaptorflow.responserparser.ValmyndighetenResultResponseParser;
import com.kg.macroanalyzer.application.domain.localelection.LocalElectionType;
import com.kg.macroanalyzer.scrape.application.port.out.SeriesConfigPort;
import com.kg.macroanalyzer.scrape.domain.SeriesConfig;
import com.kg.macroanalyzer.shared.web.ScrapeException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
public class LocalElectionRepositoryTest {

    @Autowired
    LocalElectionRepository repository;
    @Autowired
    SeriesConfigPort seriesConfigPort;

    @Test
    void shouldStoreEveryValkrets_fromTheRiksdagFile() throws ScrapeException {
        // given
        final var response = new TestJsonReader().readResource("json/valmyndigheten/mandatfordelning_00_RD_sample.json");
        final var results = new ValmyndighetenResultResponseParser().parse(response).stream()
                .map(r -> r.toBuilder().sourceMd5("8162d2cd73ecd0a24ef21632f272dfb2").build())
                .toList();

        // when
        repository.writeResults(results);
        // A second write of the same file must replace, not duplicate, the areas
        repository.writeResults(results);

        // then
        assertEquals(Set.of("8162d2cd73ecd0a24ef21632f272dfb2"), repository.readSourceMd5s(LocalElectionType.RD));
        assertEquals(Set.of(), repository.readSourceMd5s(LocalElectionType.KF));
        assertEquals(2026, repository.readLatestYear(LocalElectionType.RD).orElseThrow());

        final var areas = repository.readResults(LocalElectionType.RD, 2026);
        assertEquals(List.of("09", "01"), areas.stream().map(a -> a.areaCode()).toList());
        assertEquals(List.of(), areas.getFirst().elected());

        final var stockholm = repository.readResult(LocalElectionType.RD, 2026, "01").orElseThrow();
        assertEquals("Stockholms kommun", stockholm.areaName());
        assertEquals(15, stockholm.totalSeats());
        assertEquals(List.of("S", "M"), stockholm.parties().stream().map(p -> p.abbreviation()).toList());
        assertEquals(2, stockholm.elected().size());
        assertTrue(repository.readGovernance(LocalElectionType.RD, 2026, "01").isEmpty());
    }

    @Test
    void shouldWriteNoArea_whenOneAreaOfTheFileFails() throws ScrapeException {
        // given
        final var response = new TestJsonReader().readResource("json/valmyndigheten/mandatfordelning_00_RD_sample.json");
        final var parsed = new ValmyndighetenResultResponseParser().parse(response).stream()
                .map(r -> r.toBuilder().year(2030).sourceMd5("partialpartialpartialpartial0000").build())
                .toList();
        // area_name is NOT NULL, so the second valkrets fails after the first has been written
        final var results = List.of(parsed.getFirst(), parsed.get(1).toBuilder().areaName(null).build());

        // when
        assertThrows(RuntimeException.class, () -> repository.writeResults(results));

        // then
        assertEquals(List.of(), repository.readResults(LocalElectionType.RD, 2030));
        assertFalse(repository.readSourceMd5s(LocalElectionType.RD).contains("partialpartialpartialpartial0000"));
    }

    @Test
    void shouldSeedTheRiksdagSeriesConfig() {
        // when
        final var config = seriesConfigPort.readSeriesConfigList().stream()
                .filter(c -> c.name().equals(LocalElectionType.RD.configName()))
                .findFirst();

        // then
        assertTrue(config.isPresent());
        assertEquals("https://resultat.val.se/resultatfiler/val2026", config.map(SeriesConfig::scrapeUrl).orElseThrow());
        assertEquals("Valmyndigheten", config.map(SeriesConfig::category).orElseThrow());
    }

}
