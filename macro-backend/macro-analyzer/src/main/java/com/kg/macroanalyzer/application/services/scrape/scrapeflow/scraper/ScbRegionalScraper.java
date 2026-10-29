package com.kg.macroanalyzer.application.services.scrape.scrapeflow.scraper;

import com.kg.macroanalyzer.application.domain.scb.GeoVersion;
import com.kg.macroanalyzer.application.domain.scb.ScbIndicator;
import com.kg.macroanalyzer.application.domain.scb.ScbIndicatorResult;
import com.kg.macroanalyzer.application.domain.scb.ScbSlice;
import com.kg.macroanalyzer.application.domain.scb.ScbSource;
import com.kg.macroanalyzer.application.domain.scb.ScbTableMetadata;
import com.kg.macroanalyzer.application.domain.scb.ScbTableValues;
import com.kg.macroanalyzer.application.domain.scb.ScbValues;
import com.kg.macroanalyzer.application.ports.driven.ConfigWithScbIndicators;
import com.kg.macroanalyzer.application.ports.driven.ScbDataRequest;
import com.kg.macroanalyzer.application.ports.driven.ScbMetadataRequest;
import com.kg.macroanalyzer.scrape.application.service.scrapeflow.Scraper;
import com.kg.macroanalyzer.shared.web.ScrapeException;
import com.kg.macroanalyzer.shared.web.WebPort;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// Fetches every indicator whose SCB tables changed since the last scrape, or that has no data yet. Each source
// is fetched once per area division version, chunked by year to stay within SCB's cell limit. A failed indicator
// is skipped and the table that failed is reported, so the persister can drop its stamp and the indicator is
// fetched again next time
@Slf4j
public record ScbRegionalScraper(
        WebPort<ScbMetadataRequest, ScbTableMetadata> metadataWebPort,
        WebPort<ScbDataRequest, ScbTableValues> dataWebPort,
        long maxCells
) implements Scraper<ConfigWithScbIndicators> {

    public static final long SCB_MAX_CELLS = 150_000;

    @Override
    public Optional<ConfigWithScbIndicators> scrape(ConfigWithScbIndicators config) {
        final var run = new Run(config.seriesConfig().scrapeUrl(), sharedSources(config.indicators()));
        final var results = new ArrayList<ScbIndicatorResult>();
        final var failedTables = new HashSet<String>();
        for (final var indicator : config.indicators()) {
            try {
                final var metadata = run.metadata(indicator.tables());
                if (isUnchanged(indicator, metadata, config)) {
                    log.info("Skipping SCB indicator %s, its tables are unchanged".formatted(indicator.id()));
                    continue;
                }
                results.add(ScbIndicatorResult.builder()
                        .indicator(indicator)
                        .tableStamps(metadata.values().stream()
                                .collect(Collectors.toMap(ScbTableMetadata::table, ScbTableMetadata::updated)))
                        .values(ScbValues.toAreaValues(run.values(indicator, metadata)))
                        .build());
                log.info("Fetched SCB indicator %s".formatted(indicator.id()));
            } catch (TableException e) {
                log.error("Caught ScrapeException for SCB indicator %s: %s".formatted(indicator.id(), e.getMessage()));
                failedTables.add(e.table());
            }
        }
        if (results.isEmpty() && !failedTables.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(config.toBuilder()
                .results(results)
                .failedTables(failedTables)
                .build());
    }

    private static boolean isUnchanged(
            ScbIndicator indicator,
            Map<String, ScbTableMetadata> metadata,
            ConfigWithScbIndicators config
    ) {
        return config.indicatorsWithData().contains(indicator.id())
                && metadata.values().stream()
                .allMatch(m -> m.updated().equals(config.tableStamps().get(m.table())));
    }

    // Sources used by more than one indicator (e.g. the total population as denominator) are kept for the whole run
    private static Set<ScbSource> sharedSources(List<ScbIndicator> indicators) {
        return indicators.stream()
                .flatMap(i -> Stream.concat(i.numerator().stream(), i.denominator().stream()).distinct())
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet().stream()
                .filter(e -> e.getValue() > 1)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    private record SourceVersion(ScbSource source, GeoVersion version) {
    }

    // Names the table a failure came from, so only that table's stamp has to be dropped
    private static final class TableException extends ScrapeException {

        private final String table;

        private TableException(String table, String message) {
            super(message);
            this.table = table;
        }

        private String table() {
            return table;
        }

    }

    private final class Run {

        private final String baseUrl;
        private final Set<ScbSource> sharedSources;
        private final Map<String, ScbTableMetadata> metadataByTable = new HashMap<>();
        private final Set<String> failedMetadata = new HashSet<>();
        private final Map<SourceVersion, Map<ScbSlice, Map<String, Double>>> sharedValues = new HashMap<>();

        private Run(String baseUrl, Set<ScbSource> sharedSources) {
            this.baseUrl = baseUrl;
            this.sharedSources = sharedSources;
        }

        private Map<String, ScbTableMetadata> metadata(Set<String> tables) throws TableException {
            final var metadata = new LinkedHashMap<String, ScbTableMetadata>();
            for (final var table : tables) {
                metadata.put(table, metadata(table));
            }

            return metadata;
        }

        // A table whose metadata could not be fetched is not asked for again in the same run
        private ScbTableMetadata metadata(String table) throws TableException {
            if (failedMetadata.contains(table)) {
                throw new TableException(table, "Metadata for %s could not be fetched earlier in this run".formatted(table));
            }
            final var cached = metadataByTable.get(table);
            if (cached != null) {
                return cached;
            }
            try {
                final var fetched = metadataWebPort.fetch(new ScbMetadataRequest(baseUrl, table));
                metadataByTable.put(table, fetched);

                return fetched;
            } catch (ScrapeException e) {
                failedMetadata.add(table);
                throw new TableException(table, e.getMessage());
            }
        }

        private Map<ScbSlice, Map<String, Double>> values(
                ScbIndicator indicator,
                Map<String, ScbTableMetadata> metadata
        ) throws TableException {
            final var numerator = merge(indicator.numerator(), indicator.geoVersions(), metadata);
            if (indicator.denominator().isEmpty()) {
                return ScbValues.scale(numerator, indicator.multiplier());
            }
            final var denominator = merge(indicator.denominator(), indicator.geoVersions(), metadata);

            return ScbValues.ratio(numerator, denominator, indicator.multiplier());
        }

        // A later source replaces an earlier one for every year and version it has data for
        private Map<ScbSlice, Map<String, Double>> merge(
                List<ScbSource> sources,
                List<GeoVersion> versions,
                Map<String, ScbTableMetadata> metadata
        ) throws TableException {
            final var merged = new HashMap<ScbSlice, Map<String, Double>>();
            for (final var source : sources) {
                final var tableMetadata = metadata.get(source.table());
                validateSelection(source, tableMetadata);
                for (final var version : versions) {
                    merged.putAll(sourceValues(source, version, tableMetadata));
                }
            }

            return merged;
        }

        private Map<ScbSlice, Map<String, Double>> sourceValues(
                ScbSource source,
                GeoVersion version,
                ScbTableMetadata metadata
        ) throws TableException {
            final var key = new SourceVersion(source, version);
            final var cached = sharedValues.get(key);
            if (cached != null) {
                return cached;
            }
            final var fetched = fetch(source, version, metadata);
            if (sharedSources.contains(source)) {
                sharedValues.put(key, fetched);
            }

            return fetched;
        }

        private Map<ScbSlice, Map<String, Double>> fetch(
                ScbSource source,
                GeoVersion version,
                ScbTableMetadata metadata
        ) throws TableException {
            final var values = new HashMap<ScbSlice, Map<String, Double>>();
            if (metadata.regionCodes().stream().noneMatch(version::matches)) {
                return values;
            }
            final var regionCount = metadata.regionCodes().stream()
                    .filter(version::matchesWildcard)
                    .count();
            final var cellsPerYear = regionCount * source.cellsPerAreaAndYear();
            for (final var years : ScbValues.yearChunks(metadata.years(), cellsPerYear, maxCells)) {
                final var request = new ScbDataRequest(baseUrl, source.table(), version.wildcard(), years, source.selection());
                ScbValues.slices(fetchData(request)).forEach((slice, byArea) -> {
                    if (slice.geoVersion() == version) {
                        values.put(slice, byArea);
                    }
                });
            }

            return values;
        }

        private ScbTableValues fetchData(ScbDataRequest request) throws TableException {
            try {
                return dataWebPort.fetch(request);
            } catch (ScrapeException e) {
                throw new TableException(request.table(), e.getMessage());
            }
        }

        // An unselected dimension would fall back to SCB's default code, so a selection must name every dimension
        private static void validateSelection(ScbSource source, ScbTableMetadata metadata) throws TableException {
            for (final var entry : metadata.codes().entrySet()) {
                final var dimension = entry.getKey();
                if (dimension.equals(ScbTableMetadata.REGION) || dimension.equals(ScbTableMetadata.TIME)) {
                    continue;
                }
                final var selected = source.selection().get(dimension);
                if (selected == null || selected.isEmpty()) {
                    throw new TableException(source.table(), "%s: no codes selected for %s".formatted(source.table(), dimension));
                }
                final var unknown = selected.stream().filter(code -> !entry.getValue().contains(code)).toList();
                if (!unknown.isEmpty()) {
                    throw new TableException(source.table(), "%s: unknown %s codes %s".formatted(source.table(), dimension, unknown));
                }
            }
            for (final var dimension : source.selection().keySet()) {
                if (!metadata.codes().containsKey(dimension)) {
                    throw new TableException(source.table(), "%s has no dimension %s".formatted(source.table(), dimension));
                }
            }
        }

    }

}
