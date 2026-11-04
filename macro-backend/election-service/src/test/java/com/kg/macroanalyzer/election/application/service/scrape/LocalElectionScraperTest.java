package com.kg.macroanalyzer.election.application.service.scrape;

import com.kg.macroanalyzer.election.application.port.out.ConfigWithLocalElections;
import com.kg.macroanalyzer.election.application.port.out.LocalElectionFile;
import com.kg.macroanalyzer.election.application.port.out.LocalElectionIndexRequest;
import com.kg.macroanalyzer.election.domain.LocalElectionResult;
import com.kg.macroanalyzer.election.domain.LocalElectionType;
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
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
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
        assertTrue(result.get().partialFailure());
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
        assertFalse(result.get().partialFailure());
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
        assertFalse(result.get().partialFailure());
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

    @Test
    void shouldReturnEmptyResults_whenIndexListsNoFiles() throws ScrapeException {
        // when
        when(indexWebPort.fetch(any())).thenReturn(List.of());
        final var result = scraper.scrape(input(Set.of()));

        // then
        assertTrue(result.isPresent());
        assertEquals(List.of(), result.get().results());
        assertFalse(result.get().partialFailure());
    }

    @Test
    void shouldStopFetching_afterFiveConsecutiveFailures_andKeepWhatSucceeded() throws ScrapeException {
        // given
        final var files = files(8);
        final var lund = LocalElectionResult.builder().areaCode("1281").build();

        // when
        when(indexWebPort.fetch(any())).thenReturn(files);
        when(resultWebPort.fetch(any())).thenThrow(new ScrapeException("503"));
        doReturn(List.of(lund)).when(resultWebPort).fetch(files.getFirst());
        final var result = scraper.scrape(input(Set.of()));

        // then
        assertTrue(result.isPresent());
        assertEquals(List.of(lund), result.get().results());
        assertTrue(result.get().partialFailure());
        verify(resultWebPort, times(6)).fetch(any());
        verify(resultWebPort, never()).fetch(files.get(6));
        verify(resultWebPort, never()).fetch(files.get(7));
    }

    @Test
    void shouldFail_afterFiveConsecutiveFailures_whenNothingSucceeded() throws ScrapeException {
        // when
        when(indexWebPort.fetch(any())).thenReturn(files(8));
        when(resultWebPort.fetch(any())).thenThrow(new ScrapeException("503"));
        final var result = scraper.scrape(input(Set.of()));

        // then
        assertTrue(result.isEmpty());
        verify(resultWebPort, times(5)).fetch(any());
    }

    @Test
    void shouldKeepFetching_whenASuccessResetsTheFailureCount() throws ScrapeException {
        // given
        final var files = files(10);
        final var lund = LocalElectionResult.builder().areaCode("1281").build();
        final var malmo = LocalElectionResult.builder().areaCode("1280").build();

        // when
        when(indexWebPort.fetch(any())).thenReturn(files);
        when(resultWebPort.fetch(any())).thenThrow(new ScrapeException("503"));
        doReturn(List.of(lund)).when(resultWebPort).fetch(files.get(4));
        doReturn(List.of(malmo)).when(resultWebPort).fetch(files.get(9));
        final var result = scraper.scrape(input(Set.of()));

        // then
        assertTrue(result.isPresent());
        assertEquals(List.of(lund, malmo), result.get().results());
        assertTrue(result.get().partialFailure());
        verify(resultWebPort, times(10)).fetch(any());
    }

    @Test
    void shouldFailWithoutFetching_whenInterruptedBeforeTheFirstFile() throws ScrapeException {
        try {
            // when
            when(indexWebPort.fetch(any())).thenReturn(files(2));
            Thread.currentThread().interrupt();
            final var result = scraper.scrape(input(Set.of()));

            // then
            assertTrue(result.isEmpty());
            assertTrue(Thread.currentThread().isInterrupted());
            verify(resultWebPort, never()).fetch(any());
        } finally {
            Thread.interrupted();
        }
    }

    @Test
    void shouldStopFetching_andDropResults_whenInterruptedDuringTheRun() throws ScrapeException {
        // given
        final var files = files(3);
        final var lund = LocalElectionResult.builder().areaCode("1281").build();

        try {
            // when
            when(indexWebPort.fetch(any())).thenReturn(files);
            when(resultWebPort.fetch(files.getFirst())).thenAnswer(invocation -> {
                Thread.currentThread().interrupt();

                return List.of(lund);
            });
            final var result = scraper.scrape(input(Set.of()));

            // then
            assertTrue(result.isEmpty());
            verify(resultWebPort, times(1)).fetch(any());
        } finally {
            Thread.interrupted();
        }
    }

    private static List<LocalElectionFile> files(int count) {
        return IntStream.range(0, count)
                .mapToObj(i -> new LocalElectionFile("%04d".formatted(i), BASE_URL + "/%d.zip".formatted(i), "md5-%d".formatted(i)))
                .toList();
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
