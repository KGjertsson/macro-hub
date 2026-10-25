package com.kg.macroanalyzer.application.services.scrape.scrapeflow.scraper;

import com.kg.macroanalyzer.application.domain.localelection.LocalElectionResult;
import com.kg.macroanalyzer.application.exceptions.ScrapeException;
import com.kg.macroanalyzer.application.ports.driven.ConfigWithLocalElections;
import com.kg.macroanalyzer.application.ports.driven.LocalElectionFile;
import com.kg.macroanalyzer.application.ports.driven.LocalElectionIndexRequest;
import com.kg.macroanalyzer.application.ports.driven.WebPort;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
public record LocalElectionScraper(
        WebPort<LocalElectionIndexRequest, List<LocalElectionFile>> indexWebPort,
        WebPort<LocalElectionFile, LocalElectionResult> resultWebPort,
        long delayBetweenFilesMs
) implements Scraper<ConfigWithLocalElections> {

    @Override
    public Optional<ConfigWithLocalElections> scrape(ConfigWithLocalElections configWithLocalElections) {
        final var request = new LocalElectionIndexRequest(
                configWithLocalElections.seriesConfig().scrapeUrl(),
                configWithLocalElections.type()
        );
        final List<LocalElectionFile> files;
        try {
            files = indexWebPort.fetch(request);
        } catch (ScrapeException e) {
            log.error("Caught ScrapeException for index of %s: %s".formatted(request.baseUrl(), e.getMessage()));

            return Optional.empty();
        }
        final var changedFiles = files.stream()
                .filter(f -> !configWithLocalElections.existingMd5s().contains(f.md5()))
                .toList();
        log.info("Fetching %d of %d %s result files".formatted(
                changedFiles.size(), files.size(), configWithLocalElections.type()));

        final var results = new ArrayList<LocalElectionResult>();
        for (final var file : changedFiles) {
            try {
                results.add(resultWebPort.fetch(file));
            } catch (ScrapeException e) {
                log.error("Caught ScrapeException for area=%s: %s".formatted(file.areaCode(), e.getMessage()));
            }
            pause();
        }
        if (results.isEmpty() && !changedFiles.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(configWithLocalElections.toBuilder()
                .results(results)
                .build());
    }

    // Keeps the scrape polite towards resultat.val.se, which rate limits with 429
    private void pause() {
        try {
            Thread.sleep(delayBetweenFilesMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

}
