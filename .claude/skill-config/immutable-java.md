# `immutable-java` config: macro-hub

Read by the `immutable-java` skill. The rules apply to Java in `macro-backend/`. CLAUDE.md's "Immutability" section is the short version.

## Rule 1: what enforces it

`ArchitectureTest.fieldsAreFinal`, in every module, fails on a non-final field. `mvn test` runs it. An example of a test seam is `TimeFilterStrategyOneMonth`.

## Rule 3: the copy helper

A record copies its collection components with `shared/collection/ImmutableCopies`, not the JDK's `copyOf` methods:

```java
public ScbIndicator {
    levels = ImmutableCopies.list(levels);
    numerator = ImmutableCopies.list(numerator);
}
```

- Use `list`, `set` or `map`, and `mapOfLists` or `mapOfMaps` for a nested `Map<K, List<..>>` or `Map<K, Map<..>>`.
- Don't use `List.copyOf`/`Map.copyOf`/`Set.copyOf` here. They throw on nulls, which parsed sources and SCB's suppressed cells hold. `Map.copyOf` also loses the iteration order that JSON answers and JSON-stat's dimensions depend on. `ImmutableCopies` keeps both, and leaves a null collection null, so a JSON answer keeps its shape.

## Rule 5: an example

`scrapeDate(now, position)`, as in `ConstantBackoffStrategy`.

## Rule 7: known hot paths

A full SCB scrape passes about 2.4 M values through `ScbValues`, `ScbRegionalScraper` and `ScbTableValues`. There, `Collections.unmodifiableMap` around a map the code just built replaces the deep copy.

## State that has to change: examples and the allowlist

- One `volatile` reference swapped whole: `RefreshingPartyColorAdaptor.colors`, which gets a `Map.copyOf` from `ParliamentPartiesClient`.
- Only written under a lock: `ScbRateLimiter.lastCallMs`.
- A `final` field holding a lock: `ScrapeService.scrapeLock`.
- A per-run memo: `ScbRegionalScraper.Run`.
- A new non-final field goes in `MUTABLE_FIELDS` in that module's `ArchitectureTest`, as `fully.qualified.Class.field`, and in CLAUDE.md's "Dependency rules" list.
