package com.kg.macroanalyzer.application.services;

import com.kg.macroanalyzer.application.domain.MacroPoint;
import com.kg.macroanalyzer.application.domain.MacroSeries;
import lombok.Builder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

@Service
public class LabelGenerationService {

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
                .map(this::fillGapsInSeries)
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
                })
                .toList();
    }

    private MacroSeries fillGapsInSeries(MacroSeries macroSeries) {
        final var macroPoints = macroSeries.macroPoints();
        final var endDate = (LocalDate) macroPoints.getLast().date();
        final var filledList = new ArrayList<MacroPoint>();

        int i = 0;
        while (i < macroPoints.size()) {
            if (i == 0) {
                filledList.add(macroSeries.macroPoints().getFirst());
                i++;
            } else {
                final var currentIndexPoint = macroPoints.get(i);
                final var previousIndexPoint = filledList.getLast();

                final LocalDate currentDate = (LocalDate) currentIndexPoint.date();
                final LocalDate previousDate = (LocalDate) previousIndexPoint.date();

                if (endDate.isBefore(currentDate) || endDate.isEqual(previousDate)) {
                    break;
                }

                final var isConsecutive = currentDate.minusDays(1).equals(previousDate);

                if (isConsecutive) {
                    filledList.add(currentIndexPoint);
                    i++;
                } else {
                    final var paddedPoint = MacroPoint.builder()
                            .date(previousDate.plusDays(1))
                            .value(previousIndexPoint.value())
                            .build();
                    filledList.add(paddedPoint);
                }
            }
        }

        return macroSeries.toBuilder()
                .macroPoints(filledList)
                .build();
    }

    private MacroPoint pointFromLabel(
            Integer index,
            Integer startDateIndexInFull,
            List<LocalDate> fullLabels,
            List<MacroPoint> macroPoints
    ) {
        final var currentDate = fullLabels.get(index);

        if (index < startDateIndexInFull) {
            return MacroPoint.builder()
                    .date(currentDate)
                    .value(0.0)
                    .build();
        } else if ((index - startDateIndexInFull) >= macroPoints.size()) {
            return MacroPoint.builder()
                    .date(currentDate)
                    .value(macroPoints.getLast().value())
                    .build();
        }  else {
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
                .flatMap(this::toLabelEdges)
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

    private Stream<LabelEdges> toLabelEdges(MacroSeries macroSeries) {
        if (macroSeries.macroPoints().isEmpty()) return Stream.empty();
        final var labelEdges =  LabelEdges.builder()
                .startDate(LocalDate.from(macroSeries.macroPoints().getFirst().date()))
                .endDate(LocalDate.from(macroSeries.macroPoints().getLast().date()))
                .build();

        return Stream.of(labelEdges);
    }

    @Builder(toBuilder = true)
    private record LabelEdges(LocalDate startDate, LocalDate endDate) {

        public Optional<LabelEdges> toOptional() {
            return Optional.of(this);
        }

    }

}
