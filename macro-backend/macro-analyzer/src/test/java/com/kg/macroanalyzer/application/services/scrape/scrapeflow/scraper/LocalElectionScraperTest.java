package com.kg.macroanalyzer.application.services.scrape.scrapeflow.scraper;

import com.kg.macroanalyzer.application.domain.localelection.LocalElectionResult;
import com.kg.macroanalyzer.application.domain.localelection.LocalElectionType;
import com.kg.macroanalyzer.application.ports.driven.ConfigWithLocalElections;
import com.kg.macroanalyzer.application.ports.driven.LocalElectionFile;
import com.kg.macroanalyzer.application.ports.driven.LocalElectionIndexRequest;
import com.kg.macroanalyzer.scrape.domain.SeriesConfig;
import com.kg.macroanalyzer.shared.web.ScrapeException;
import com.kg.macroanalyzer.shared.web.WebPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LocalElectionScraperTest {

    private static final String BASE_URL = "https://resultat.val.se/resultatfiler/val2026";

    LocalElectionScraper scraper;

    @Mock
    WebPort<LocalElectionIndexRequest, List<LocalElectionFile>> indexWebPort;
    @Mock
    WebPort<LocalElectionFile, List<LocalElectionResult>> resultWebPort;

    @BeforeEach
    void setUp() {
        scraper = new LocalElectionScraper(indexWebPort, resultWebPort, 0);
    }

    @Test
    void shouldOnlyFetchChangedFiles_andSkipFailedOnes() throws ScrapeException {
        // given
        final var unchanged = new LocalElectionFile("0180", BASE_URL + "/a.zip", "md5-a");
        final var changed = new LocalElectionFile("1281", BASE_URL + "/b.zip", "md5-b");
        final var broken = new LocalElectionFile("0114", BASE_URL + "/c.zip", "md5-c");
        final var lund = LocalElectionResult.builder().areaCode("1281").build();
        final var input = input(Set.of("md5-a"));

        // when
        when(indexWebPort.fetch(new LocalElectionIndexRequest(BASE_URL, LocalElectionType.KF)))
                .thenReturn(List.of(unchanged, changed, broken));
        when(resultWebPort.fetch(changed)).thenReturn(List.of(lund));
        when(resultWebPort.fetch(broken)).thenThrow(new ScrapeException("429"));
        final var result = scraper.scrape(input);

        // then
        assertTrue(result.isPresent());
        assertEquals(List.of(lund), result.get().results());
        verify(resultWebPort, never()).fetch(unchanged);
    }

    @Test
    void shouldKeepEveryArea_whenOneFileHoldsSeveral() throws ScrapeException {
        // given
        final var file = new LocalElectionFile("00", BASE_URL + "/rd.zip", "md5-rd");
        final var stockholm = LocalElectionResult.builder().areaCode("01").build();
        final var gotland = LocalElectionResult.builder().areaCode("09").build();

        // when
        when(indexWebPort.fetch(any())).thenReturn(List.of(file));
        when(resultWebPort.fetch(file)).thenReturn(List.of(stockholm, gotland));
        final var result = scraper.scrape(input(Set.of()));

        // then
        assertTrue(result.isPresent());
        assertEquals(List.of(stockholm, gotland), result.get().results());
    }

    @Test
    void shouldReturnEmptyResults_whenNothingChanged() throws ScrapeException {
        // given
        final var file = new LocalElectionFile("0180", BASE_URL + "/a.zip", "md5-a");

        // when
        when(indexWebPort.fetch(any())).thenReturn(List.of(file));
        final var result = scraper.scrape(input(Set.of("md5-a")));

        // then
        assertTrue(result.isPresent());
        assertEquals(List.of(), result.get().results());
    }

    @Test
    void shouldFail_whenIndexCannotBeFetched() throws ScrapeException {
        // when
        when(indexWebPort.fetch(any())).thenThrow(new ScrapeException("down"));
        final var result = scraper.scrape(input(Set.of()));

        // then
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldFail_whenEveryChangedFileFails() throws ScrapeException {
        // when
        when(indexWebPort.fetch(any())).thenReturn(List.of(new LocalElectionFile("0180", BASE_URL + "/a.zip", "md5-a")));
        when(resultWebPort.fetch(any())).thenThrow(new ScrapeException("429"));
        final var result = scraper.scrape(input(Set.of()));

        // then
        assertTrue(result.isEmpty());
    }

    private static ConfigWithLocalElections input(Set<String> existingMd5s) {
        return ConfigWithLocalElections.builder()
                .seriesConfig(SeriesConfig.builder().name("MunicipalElections").scrapeUrl(BASE_URL).build())
                .type(LocalElectionType.KF)
                .existingMd5s(existingMd5s)
                .results(List.of())
                .build();
    }

}
