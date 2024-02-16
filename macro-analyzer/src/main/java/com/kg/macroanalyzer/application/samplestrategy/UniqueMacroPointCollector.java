package com.kg.macroanalyzer.application.samplestrategy;

import com.kg.macroanalyzer.application.domain.MacroPoint;

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
            final var elementAlreadyExists = acc.stream()
                    .map(MacroPoint::date)
                    .toList()
                    .contains(element.date());
            if (!elementAlreadyExists) {
                acc.add(element);
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
