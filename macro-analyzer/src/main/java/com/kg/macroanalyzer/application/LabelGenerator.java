package com.kg.macroanalyzer.application;

import com.kg.macroanalyzer.application.domain.MacroPoint;
import com.kg.macroanalyzer.application.domain.MacroSeries;
import lombok.Builder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

@Component
public class LabelGenerator {

    private final LabelEdges IDENTITY_LABELS = LabelEdges.builder()
            .startDate(LocalDate.MAX)
            .endDate(LocalDate.MIN)
            .build();

    public Optional<List<MacroSeries>> padToFullLabels(List<MacroSeries> macroSeriesList) {
        return findEdges(macroSeriesList)
                .map(this::generateDatesBetween)
                .map(paddedLabels -> extendToFullLabels(macroSeriesList, paddedLabels));
    }

    private List<MacroSeries> extendToFullLabels(
            List<MacroSeries> macroSeriesList,
            List<LocalDate> paddedLabels
    ) {
        return macroSeriesList.stream()
                .map(macroSeries -> {
                    final var macroPoints = macroSeries.macroPoints();
                    final var startDate = LocalDate.from(macroPoints.getFirst().date());
                    final var startDateIndexInFull = paddedLabels.indexOf(startDate);

                    final var newPoints = IntStream.range(0, paddedLabels.size())
                            .mapToObj(index -> pointFromLabel(index, startDateIndexInFull, paddedLabels, macroPoints))
                            .toList();

                    return MacroSeries.builder()
                            .name(macroSeries.name())
                            .macroPoints(newPoints)
                            .build();
                }).toList();
    }

    private MacroPoint pointFromLabel(
            Integer index,
            Integer startDateIndexInFull,
            List<LocalDate> fullLabels,
            List<MacroPoint> macroPoints
    ) {
        final var currentDate = fullLabels.get(index);

        if (index < startDateIndexInFull || index >= macroPoints.size()) {
            return MacroPoint.builder()
                    .date(currentDate)
                    .value(0.0)
                    .build();
        } else {
            return macroPoints.get(index - startDateIndexInFull);
        }
    }

    private List<LocalDate> generateDatesBetween(LabelEdges labelEdges) {
        final var start = labelEdges.startDate;
        final var end = labelEdges.endDate;

        return Stream.iterate(start, date -> date.plusDays(1))
                .limit(ChronoUnit.DAYS.between(start, end) + 1)
                .collect(toList());
    }

    private Optional<LabelEdges> findEdges(List<MacroSeries> macroSeriesList) {
        return macroSeriesList.stream()
                .map(this::toLabelEdges)
                .reduce(IDENTITY_LABELS, this::findEdges)
                .toOptional()
                .filter(this::isNotIdentity);
    }

    private boolean isNotIdentity(LabelEdges labelEdges) {
        return !labelEdges.equals(IDENTITY_LABELS);
    }

    private LabelEdges findEdges(LabelEdges acc, LabelEdges element) {
        final var edgeBuilder = acc.toBuilder();
        if (element.startDate.isBefore(acc.startDate)) {
            edgeBuilder.startDate(element.startDate);
        }
        if (element.endDate.isAfter(acc.endDate)) {
            edgeBuilder.endDate(element.endDate);
        }
        return edgeBuilder.build();
    }

    private LabelEdges toLabelEdges(MacroSeries macroSeries) {
        return LabelEdges.builder()
                .startDate(LocalDate.from(macroSeries.macroPoints().getFirst().date()))
                .endDate(LocalDate.from(macroSeries.macroPoints().getLast().date()))
                .build();
    }

    @Builder(toBuilder = true)
    private record LabelEdges(LocalDate startDate, LocalDate endDate) {

        public Optional<LabelEdges> toOptional() {
            return Optional.of(this);
        }

    }

}
