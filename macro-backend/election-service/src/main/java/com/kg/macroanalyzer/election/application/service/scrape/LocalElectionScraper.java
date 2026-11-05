package com.kg.macroanalyzer.election.application.service.scrape;

import com.kg.macroanalyzer.election.application.port.out.ConfigWithLocalElections;
import com.kg.macroanalyzer.election.application.port.out.LocalElectionFile;
import com.kg.macroanalyzer.election.application.port.out.LocalElectionIndexRequest;
import com.kg.macroanalyzer.election.domain.LocalElectionResult;
import com.kg.macroanalyzer.scrape.application.service.scrapeflow.Scraper;
import com.kg.macroanalyzer.shared.collection.ImmutableCopies;
import com.kg.macroanalyzer.shared.web.ScrapeException;
import com.kg.macroanalyzer.shared.web.WebPort;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
public record LocalElectionScraper(
        WebPort<LocalElectionIndexRequest, List<LocalElectionFile>> indexWebPort,
        WebPort<LocalElectionFile, List<LocalElectionResult>> resultWebPort,
        long delayBetweenFilesMs
) implements Scraper<ConfigWithLocalElections> {

    // A failed file takes all its attempts and back-off, so an outage after the index fetch would otherwise hold
    // ScrapeService's scrapeLock for hours
    private static final int MAX_CONSECUTIVE_FAILURES = 5;

    @Override
    public Optional<ConfigWithLocalElections> scrape(ConfigWithLocalElections input) {
        final var request = new LocalElectionIndexRequest(input.seriesConfig().scrapeUrl(), input.type());
        final List<LocalElectionFile> files;
        try {
            files = indexWebPort.fetch(request);
        } catch (ScrapeException e) {
            log.error("Caught ScrapeException for index of {}: {}", request.baseUrl(), e.getMessage());

            return Optional.empty();
        }
        if (files.isEmpty()) {
            log.warn("Index of {} lists no {} result files", request.baseUrl(), input.type());
        }
        final var changedFiles = files.stream()
                .filter(f -> !input.existingMd5s().contains(f.md5()))
                .toList();
        log.info("Fetching {} of {} {} result files", changedFiles.size(), files.size(), input.type());

        return fetchResults(changedFiles)
                .filter(fetched -> changedFiles.isEmpty() || !fetched.results().isEmpty())
                .map(fetched -> input.toBuilder()
                        .results(fetched.results())
                        .partialFailure(fetched.partialFailure())
                        .build());
    }

    private Optional<FetchedResults> fetchResults(List<LocalElectionFile> files) {
        final var results = new ArrayList<LocalElectionResult>();
        var anyFailed = false;
        var consecutiveFailures = 0;
        for (final var file : files) {
            // pause and ValmyndighetenRetry restore the flag, so their sleeps would now throw at once and the loop
            // would call a rate limiting server without any delay
            if (Thread.currentThread().isInterrupted()) {
                log.warn("Interrupted before fetching area={}, abandoning the run", file.areaCode());

                return Optional.empty();
            }
            try {
                results.addAll(resultWebPort.fetch(file));
                consecutiveFailures = 0;
            } catch (ScrapeException e) {
                log.error("Caught ScrapeException for area={}: {}", file.areaCode(), e.getMessage());
                anyFailed = true;
                consecutiveFailures++;
                if (consecutiveFailures == MAX_CONSECUTIVE_FAILURES) {
                    log.error("Stopping after {} consecutive failed files", consecutiveFailures);
                    break;
                }
            }
            pause();
        }

        return Optional.of(new FetchedResults(results, anyFailed));
    }

    // Keeps the scrape polite towards resultat.val.se, which rate limits with 429
    private void pause() {
        try {
            Thread.sleep(delayBetweenFilesMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private record FetchedResults(List<LocalElectionResult> results, boolean partialFailure) {

        public FetchedResults {
            results = ImmutableCopies.list(results);
        }

    }

}
