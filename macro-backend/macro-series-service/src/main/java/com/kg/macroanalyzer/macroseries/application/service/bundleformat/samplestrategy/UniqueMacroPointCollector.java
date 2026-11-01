package com.kg.macroanalyzer.macroseries.application.service.bundleformat.samplestrategy;

import com.kg.macroanalyzer.macroseries.domain.MacroPoint;

import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

// Keeps the first point per date in encounter order; keyed by date so each point is handled in constant time
public class UniqueMacroPointCollector implements Collector<MacroPoint, Map<Temporal, MacroPoint>, List<MacroPoint>> {

    @Override
    public Supplier<Map<Temporal, MacroPoint>> supplier() {
        return LinkedHashMap::new;
    }

    @Override
    public BiConsumer<Map<Temporal, MacroPoint>, MacroPoint> accumulator() {
        return (acc, element) -> acc.merge(element.date(), element, UniqueMacroPointCollector::pick);
    }

    @Override
    public BinaryOperator<Map<Temporal, MacroPoint>> combiner() {
        return (left, right) -> {
            right.forEach((date, point) -> left.merge(date, point, UniqueMacroPointCollector::pick));
            return left;
        };
    }

    @Override
    public Function<Map<Temporal, MacroPoint>, List<MacroPoint>> finisher() {
        return acc -> new ArrayList<>(acc.values());
    }

    @Override
    public Set<Characteristics> characteristics() {
        return Collections.emptySet();
    }

    // Replacing a value in a LinkedHashMap keeps the key's position, so the earlier point's slot is reused
    private static MacroPoint pick(MacroPoint existing, MacroPoint incoming) {
        // A period where the series starts part-way through takes its first observation, not the padding
        return existing.value() == null && incoming.value() != null
                ? incoming
                : existing;
    }

}
