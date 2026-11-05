---
name: immutable-java
description: The backend's immutability rules, and a check for the ones ArchUnit can't see. Use when writing, changing or reviewing Java in macro-backend/ (records, DTOs, services, scrapers, parsers, repositories, Spring beans), and before pushing a backend change.
---

# Immutable Java in macro-backend

Values don't change once built, and objects keep no state that changes. Local mutation inside a method is fine: building a map in a loop and returning it unmodifiable doesn't let anyone see it change. CLAUDE.md's "Immutability" section is the short version; this is the working detail.

## Rules

1. **Fields are `final`.** `ArchitectureTest.fieldsAreFinal`, in every module, fails otherwise.
   - Spring: constructor injection; `@Value` goes on constructor or `@Bean` method parameters, never on a field.
   - A test seam such as a clock is a `final` field set by a second constructor (see `TimeFilterStrategyOneMonth`).
2. **Values are records**, changed with `@Builder(toBuilder = true)`, which returns a new one. No `@Data`, `@Setter` or `@With`, and no setters you write yourself.
3. **A record with a `List`, `Set` or `Map` component copies it in a compact constructor**, with `shared/collection/ImmutableCopies`:
   ```java
   public ScbIndicator {
       levels = ImmutableCopies.list(levels);
       numerator = ImmutableCopies.list(numerator);
   }
   ```
   - Use `list`, `set` or `map`, and `mapOfLists` or `mapOfMaps` for a nested `Map<K, List<..>>` or `Map<K, Map<..>>`.
   - Don't use `List.copyOf`/`Map.copyOf`/`Set.copyOf` here. They throw on nulls, which parsed sources and SCB's suppressed cells hold. `Map.copyOf` also loses the iteration order that JSON answers and JSON-stat's dimensions depend on. `ImmutableCopies` keeps both, and leaves a null collection null, so a JSON answer keeps its shape.
   - Lombok's `@NonNull` on a component still runs before the copy.
   - Static constants go before the compact constructor, the constructor before the methods.
4. **A method that fills a collection returns it unmodifiable.**
   - In a stream: `.toList()`, `Collectors.toUnmodifiableSet()`, `Collectors.toUnmodifiableMap(...)`. Use the last two only when no element, key or value is null and the order doesn't matter.
   - After a loop: `return Collections.unmodifiableList(results);` (or `Map`/`Set`), which keeps nulls and order and doesn't copy.
   - `List.copyOf`/`Set.copyOf` only where nulls can't occur, e.g. a NOT NULL column.
   - A mutable collector inside another one (`groupingBy(..., Collectors.toList())`) is fine when the result goes into a record, which copies it.
5. **No counters or "last seen" fields.** Pass the position or the time in: `IntStream.range(0, list.size()).mapToObj(i -> ...)`, or `scrapeDate(now, position)`, as in `ConstantBackoffStrategy`. Read `Instant.now()` once and pass it on, so the items of one run share it.
6. **Don't reuse a Lombok builder** for several objects. Each `build()` takes the builder's current state, so start each object with its own `builder()` or `of(...)`.
7. **Hot paths wrap instead of copying.** A full SCB scrape passes about 2.4 M values through `ScbValues`, `ScbRegionalScraper` and `ScbTableValues`. There, `Collections.unmodifiableMap` around a map the code just built replaces the deep copy. Any other exception needs a measured reason and a comment that says why.

## State that has to change

Some state has to change: a lock, a rate limiter's timestamp, a cache that is refreshed. Keep it, but:

- Prefer one `volatile` reference to an immutable value, swapped whole (`RefreshingPartyColorAdaptor.colors`, which gets a `Map.copyOf` from `ParliamentPartiesClient`), or a field that is only written under `synchronized`/a lock (`ScbRateLimiter.lastCallMs`).
- A `final` field holding a lock or atomic (`ScrapeService.scrapeLock`) needs nothing extra.
- A per-run memo (`ScbRegionalScraper.Run`) is a `final` field with mutable contents, scoped to one run and never shared. Keep it private to that run.
- A new non-final field goes in `MUTABLE_FIELDS` in that module's `ArchitectureTest`, as `fully.qualified.Class.field`, and in CLAUDE.md's "Dependency rules" list. Tell the user why it can't be final.

## Before you push

From the repo root:

```bash
python3 -I .claude/skills/immutable-java/check.py            # every main source in macro-backend
python3 -I .claude/skills/immutable-java/check.py $(git diff --name-only origin/main...HEAD)   # only what the branch changed
```

- **Errors** (exit 1): a record with a collection component that has no compact constructor, or one that doesn't copy or wrap every such component; Lombok `@Data`, `@Setter` or `@With`. Fix them.
- **Warnings**: `Collectors.toList()`/`toSet()`, and `return new ArrayList<>(...)` and similar. Fix those that are returned or stored. Leave a collector that rule 4 allows.
- Then `mvn test`, whose `ArchitectureTest`s check rule 1.

## When reviewing

The script reads the source text, so it can't tell a mutable collection that leaves a method in a variable from one that doesn't. Also look for:

- a collection from a record or another method that is changed with `add`/`put`/`remove`/`sort`, which now throws `UnsupportedOperationException`. Copy it locally first (`new HashSet<>(config.failedTables())`);
- a helper object that counts or remembers between calls (rule 5);
- a `List.copyOf`/`Map.copyOf`/`toUnmodifiableMap` that can now see a null, or a map whose order matters.
