package com.kg.macroanalyzer.shared.collection;

import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

// List.copyOf and Map.copyOf reject nulls, which parsed sources and SCB's suppressed cells hold,
// and Map.copyOf drops the iteration order that the JSON answers and JSON-stat's dimensions depend
// on. A null collection stays null, because the JSON answers tell an absent list from an empty one
@UtilityClass
public class ImmutableCopies {

    public static <T> List<T> list(List<? extends T> list) {
        return list == null ? null : Collections.unmodifiableList(new ArrayList<>(list));
    }

    public static <T> Set<T> set(Set<? extends T> set) {
        return set == null ? null : Collections.unmodifiableSet(new LinkedHashSet<>(set));
    }

    public static <K, V> Map<K, V> map(Map<? extends K, ? extends V> map) {
        return map == null ? null : Collections.unmodifiableMap(new LinkedHashMap<>(map));
    }

    public static <K, V> Map<K, List<V>> mapOfLists(
            Map<? extends K, ? extends List<? extends V>> map) {
        if (map == null) {
            return null;
        }
        final var copy = new LinkedHashMap<K, List<V>>();
        map.forEach((key, values) -> copy.put(key, list(values)));

        return Collections.unmodifiableMap(copy);
    }

    public static <K, K2, V> Map<K, Map<K2, V>> mapOfMaps(
            Map<? extends K, ? extends Map<? extends K2, ? extends V>> map) {
        if (map == null) {
            return null;
        }
        final var copy = new LinkedHashMap<K, Map<K2, V>>();
        map.forEach((key, values) -> copy.put(key, map(values)));

        return Collections.unmodifiableMap(copy);
    }

}
