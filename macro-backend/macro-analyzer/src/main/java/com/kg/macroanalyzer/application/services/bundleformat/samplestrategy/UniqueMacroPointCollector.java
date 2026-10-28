package com.kg.macroanalyzer.application.services.bundleformat.samplestrategy;

import com.kg.macroanalyzer.application.domain.macroseries.MacroPoint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

public class UniqueMacroPointCollector implements Collector<MacroPoint, List<MacroPoint>, List<MacroPoint>> {

    @Override
    public Supplier<List<MacroPoint>> supplier() {
        return ArrayList::new;
    }

    @Override
    public BiConsumer<List<MacroPoint>, MacroPoint> accumulator() {
        return (acc, element) -> {
            final var existingIndex = acc.stream()
                    .map(MacroPoint::date)
                    .toList()
                    .indexOf(element.date());
            if (existingIndex < 0) {
                acc.add(element);
            } else if (acc.get(existingIndex).value() == null && element.value() != null) {
                // A period where the series starts part-way through takes its first observation, not the padding
                acc.set(existingIndex, element);
            }
        };
    }

    @Override
    public BinaryOperator<List<MacroPoint>> combiner() {
        return (list1, list2) -> {
            list1.addAll(list2);
            return list1;
        };
    }

    @Override
    public Function<List<MacroPoint>, List<MacroPoint>> finisher() {
        return Function.identity();
    }

    @Override
    public Set<Characteristics> characteristics() {
        return Collections.emptySet();
    }

}
