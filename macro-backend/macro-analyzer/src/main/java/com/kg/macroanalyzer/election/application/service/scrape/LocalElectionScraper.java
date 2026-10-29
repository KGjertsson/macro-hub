package com.kg.macroanalyzer.election.application.service.scrape;

import com.kg.macroanalyzer.election.application.port.out.ConfigWithLocalElections;
import com.kg.macroanalyzer.election.application.port.out.LocalElectionFile;
import com.kg.macroanalyzer.election.application.port.out.LocalElectionIndexRequest;
import com.kg.macroanalyzer.election.domain.LocalElectionResult;
import com.kg.macroanalyzer.scrape.application.service.scrapeflow.Scraper;
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
                results.addAll(resultWebPort.fetch(file));
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
