package com.kg.macroanalyzer.application;

import com.kg.macroanalyzer.application.domain.MacroBundle;
import com.kg.macroanalyzer.application.domain.MacroSeries;
import lombok.Builder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

@Component
public class LabelGenerator {

    private final LabelEdges IDENTITY_LABELS = LabelEdges.builder()
            .startDate(LocalDate.MAX)
            .endDate(LocalDate.MIN)
            .build();

    public Optional<Formatter.WithFullLabels> generateFullLabels(MacroBundle macroBundle) {
        return findEdges(macroBundle)
                .map(this::generateDatesBetween)
                .map(fullLabels -> buildBundleWithLabels(macroBundle, fullLabels));
    }

    private Formatter.WithFullLabels buildBundleWithLabels(
            MacroBundle macroBundle,
            List<LocalDate> fullLabels
    ) {
        return Formatter.WithFullLabels.builder()
                .macroBundle(macroBundle)
                .labels(fullLabels)
                .build();
    }

    private List<LocalDate> generateDatesBetween(LabelEdges labelEdges) {
        final var start = labelEdges.startDate;
        final var end = labelEdges.endDate;

        return Stream.iterate(start, date -> date.plusDays(1))
                .limit(start.until(end).getDays() + 1)
                .collect(toList());
    }

    private Optional<LabelEdges> findEdges(MacroBundle macroBundle) {
        return macroBundle.macroSeries().stream()
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
                .startDate(macroSeries.macroPoints().getFirst().date())
                .endDate(macroSeries.macroPoints().getLast().date())
                .build();
    }

    @Builder(toBuilder = true)
    private record LabelEdges(LocalDate startDate, LocalDate endDate) {

        public Optional<LabelEdges> toOptional() {
            return Optional.of(this);
        }

    }

}
