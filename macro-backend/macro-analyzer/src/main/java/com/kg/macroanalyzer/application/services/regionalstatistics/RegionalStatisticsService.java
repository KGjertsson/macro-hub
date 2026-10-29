package com.kg.macroanalyzer.application.services.regionalstatistics;

import com.kg.macroanalyzer.application.domain.scb.GeoVersion;
import com.kg.macroanalyzer.application.domain.scb.ScbAvailability;
import com.kg.macroanalyzer.application.domain.scb.ScbIndicator;
import com.kg.macroanalyzer.application.domain.scb.ScbLevel;
import com.kg.macroanalyzer.application.ports.driven.DatabasePort;
import com.kg.macroanalyzer.application.ports.driving.out.regionalstatistics.AvailabilityDto;
import com.kg.macroanalyzer.application.ports.driving.out.regionalstatistics.RegionalIndicatorDto;
import com.kg.macroanalyzer.application.ports.driving.out.regionalstatistics.RegionalValuesDto;
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
public class RegionalStatisticsService {

    private static final BinaryOperator<GeoVersion> NEWER = (a, b) -> a.compareTo(b) >= 0 ? a : b;

    private final DatabasePort databasePort;

    @Autowired
    public RegionalStatisticsService(DatabasePort databasePort) {
        this.databasePort = databasePort;
    }

    public List<RegionalIndicatorDto> getIndicators() {
        final var availabilityByIndicator = databasePort.readScbAvailability().stream()
                .collect(Collectors.groupingBy(ScbAvailability::indicatorId));

        return databasePort.readScbIndicators().stream()
                .map(indicator -> toDto(indicator, availabilityByIndicator.getOrDefault(indicator.id(), List.of())))
                .toList();
    }

    public Optional<RegionalValuesDto> getValues(String indicatorId, ScbLevel level, Integer year) {
        final var versionByYear = versionByYear(databasePort.readScbAvailability(indicatorId), level);
        if (versionByYear.isEmpty()) {
            return Optional.empty();
        }
        final var resolvedYear = year != null ? year : versionByYear.lastKey();
        final var version = versionByYear.get(resolvedYear);
        if (version == null) {
            return Optional.empty();
        }
        final var values = databasePort.readScbValues(indicatorId, version, resolvedYear);
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
