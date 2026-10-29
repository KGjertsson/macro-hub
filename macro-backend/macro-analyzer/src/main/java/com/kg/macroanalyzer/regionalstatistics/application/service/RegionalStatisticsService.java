package com.kg.macroanalyzer.regionalstatistics.application.service;

import com.kg.macroanalyzer.regionalstatistics.application.port.in.AvailabilityDto;
import com.kg.macroanalyzer.regionalstatistics.application.port.in.RegionalIndicatorDto;
import com.kg.macroanalyzer.regionalstatistics.application.port.in.RegionalStatisticsQueries;
import com.kg.macroanalyzer.regionalstatistics.application.port.in.RegionalValuesDto;
import com.kg.macroanalyzer.regionalstatistics.application.port.out.RegionalStatisticsRepositoryPort;
import com.kg.macroanalyzer.regionalstatistics.domain.GeoVersion;
import com.kg.macroanalyzer.regionalstatistics.domain.ScbAvailability;
import com.kg.macroanalyzer.regionalstatistics.domain.ScbIndicator;
import com.kg.macroanalyzer.regionalstatistics.domain.ScbLevel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

// A year can in principle have data in both versions of a level; the newer version is then used, both in the
// availability list and for the values, so the two always agree
@Service
public class RegionalStatisticsService implements RegionalStatisticsQueries {

    private static final BinaryOperator<GeoVersion> NEWER = (a, b) -> a.compareTo(b) >= 0 ? a : b;

    private final RegionalStatisticsRepositoryPort repositoryPort;

    @Autowired
    public RegionalStatisticsService(RegionalStatisticsRepositoryPort repositoryPort) {
        this.repositoryPort = repositoryPort;
    }

    @Override
    public List<RegionalIndicatorDto> getRegionalIndicators() {
        final var availabilityByIndicator = repositoryPort.readScbAvailability().stream()
                .collect(Collectors.groupingBy(ScbAvailability::indicatorId));

        return repositoryPort.readScbIndicators().stream()
                .map(indicator -> toDto(indicator, availabilityByIndicator.getOrDefault(indicator.id(), List.of())))
                .toList();
    }

    @Override
    public Optional<RegionalValuesDto> getRegionalValues(String indicatorId, ScbLevel level, Integer year) {
        final var versionByYear = versionByYear(repositoryPort.readScbAvailability(indicatorId), level);
        if (versionByYear.isEmpty()) {
            return Optional.empty();
        }
        final var resolvedYear = year != null ? year : versionByYear.lastKey();
        final var version = versionByYear.get(resolvedYear);
        if (version == null) {
            return Optional.empty();
        }
        final var values = repositoryPort.readScbValues(indicatorId, version, resolvedYear);
        if (values.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(RegionalValuesDto.builder()
                .indicator(indicatorId)
                .level(level.label())
                .year(resolvedYear)
                .geoVersion(version.code())
                .values(values)
                .build());
    }

    private static RegionalIndicatorDto toDto(ScbIndicator indicator, List<ScbAvailability> availability) {
        final var byLevel = new LinkedHashMap<String, List<AvailabilityDto>>();
        for (final var level : ScbLevel.values()) {
            byLevel.put(level.label(), versionByYear(availability, level).descendingMap().entrySet().stream()
                    .map(e -> AvailabilityDto.builder()
                            .year(e.getKey())
                            .geoVersion(e.getValue().code())
                            .build())
                    .toList());
        }

        return RegionalIndicatorDto.builder()
                .id(indicator.id())
                .label(indicator.label())
                .description(indicator.description())
                .category(indicator.category())
                .unit(indicator.unit())
                .decimals(indicator.decimals())
                .availability(byLevel)
                .build();
    }

    private static TreeMap<Integer, GeoVersion> versionByYear(List<ScbAvailability> availability, ScbLevel level) {
        return availability.stream()
                .filter(a -> a.geoVersion().level() == level)
                .collect(Collectors.toMap(ScbAvailability::year, ScbAvailability::geoVersion, NEWER, TreeMap::new));
    }

}
