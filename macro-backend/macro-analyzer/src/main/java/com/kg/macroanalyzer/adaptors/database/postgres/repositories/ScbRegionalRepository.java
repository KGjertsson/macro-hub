package com.kg.macroanalyzer.adaptors.database.postgres.repositories;

import com.kg.macroanalyzer.application.domain.scb.GeoVersion;
import com.kg.macroanalyzer.application.domain.scb.ScbAreaValue;
import com.kg.macroanalyzer.application.domain.scb.ScbAvailability;
import com.kg.macroanalyzer.application.domain.scb.ScbIndicator;
import com.kg.macroanalyzer.application.domain.scb.ScbIndicatorResult;
import com.kg.macroanalyzer.application.domain.scb.ScbLevel;
import com.kg.macroanalyzer.application.domain.scb.ScbSource;
import com.kg.macroanalyzer.jooq.generated.tables.records.ScbIndicatorRecord;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.JSONB;
import org.jooq.Record3;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.kg.macroanalyzer.jooq.generated.Tables.SCB_AREA_VALUE;
import static com.kg.macroanalyzer.jooq.generated.Tables.SCB_AVAILABILITY;
import static com.kg.macroanalyzer.jooq.generated.Tables.SCB_INDICATOR;
import static com.kg.macroanalyzer.jooq.generated.Tables.SCB_TABLE_STATE;

@Repository
public class ScbRegionalRepository {

    private static final JsonMapper JSON_MAPPER = JsonMapper.builder().build();
    private static final TypeReference<List<SourceJson>> SOURCE_LIST = new TypeReference<>() {
    };
    // 5 columns per row keeps a multi-row insert well below PostgreSQL's 65 535 bind parameters
    private static final int INSERT_BATCH_SIZE = 5_000;

    private final DSLContext dslContext;

    @Autowired
    public ScbRegionalRepository(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    public List<ScbIndicator> readIndicators() {
        return dslContext.selectFrom(SCB_INDICATOR)
                .orderBy(SCB_INDICATOR.SORT_ORDER, SCB_INDICATOR.ID)
                .fetch()
                .map(ScbRegionalRepository::toIndicator);
    }

    public Map<String, Instant> readTableStamps() {
        return dslContext.selectFrom(SCB_TABLE_STATE)
                .fetch()
                .stream()
                .collect(Collectors.toMap(r -> r.getTableId(), r -> r.getUpdated().toInstant()));
    }

    public Set<String> readIndicatorsWithData() {
        return new HashSet<>(dslContext.selectDistinct(SCB_AVAILABILITY.INDICATOR_ID)
                .from(SCB_AVAILABILITY)
                .fetch(SCB_AVAILABILITY.INDICATOR_ID));
    }

    // The indicator is replaced as a whole, so a failed write leaves the old values. scb_availability is replaced in the
    // same transaction, so it always lists exactly the (geo version, year) pairs that have values
    public Integer writeIndicatorResult(ScbIndicatorResult result) {
        final var indicatorId = result.indicator().id();
        dslContext.transaction(configuration -> {
            final var tx = DSL.using(configuration);
            tx.deleteFrom(SCB_AREA_VALUE)
                    .where(SCB_AREA_VALUE.INDICATOR_ID.eq(indicatorId))
                    .execute();
            final var values = result.values();
            for (int i = 0; i < values.size(); i += INSERT_BATCH_SIZE) {
                insertValues(tx, indicatorId, values.subList(i, Math.min(values.size(), i + INSERT_BATCH_SIZE)));
            }
            replaceAvailability(tx, indicatorId, values);
        });

        return result.values().size();
    }

    public void writeTableStamps(Map<String, Instant> stamps, Set<String> droppedTables) {
        dslContext.transaction(configuration -> {
            final var tx = DSL.using(configuration);
            if (!droppedTables.isEmpty()) {
                tx.deleteFrom(SCB_TABLE_STATE)
                        .where(SCB_TABLE_STATE.TABLE_ID.in(droppedTables))
                        .execute();
            }
            stamps.forEach((table, updated) -> tx.insertInto(SCB_TABLE_STATE)
                    .set(SCB_TABLE_STATE.TABLE_ID, table)
                    .set(SCB_TABLE_STATE.UPDATED, updated.atOffset(ZoneOffset.UTC))
                    .onConflict(SCB_TABLE_STATE.TABLE_ID)
                    .doUpdate()
                    .set(SCB_TABLE_STATE.UPDATED, updated.atOffset(ZoneOffset.UTC))
                    .execute());
        });
    }

    private static void insertValues(DSLContext tx, String indicatorId, List<ScbAreaValue> values) {
        var insert = tx.insertInto(
                SCB_AREA_VALUE,
                SCB_AREA_VALUE.INDICATOR_ID,
                SCB_AREA_VALUE.YEAR,
                SCB_AREA_VALUE.GEO_VERSION,
                SCB_AREA_VALUE.AREA_CODE,
                SCB_AREA_VALUE.VALUE
        );
        for (final var value : values) {
            insert = insert.values(
                    indicatorId,
                    (short) value.year(),
                    value.geoVersion().code(),
                    value.areaCode(),
                    BigDecimal.valueOf(value.value())
            );
        }
        insert.execute();
    }

    private static void replaceAvailability(DSLContext tx, String indicatorId, List<ScbAreaValue> values) {
        tx.deleteFrom(SCB_AVAILABILITY)
                .where(SCB_AVAILABILITY.INDICATOR_ID.eq(indicatorId))
                .execute();
        final var available = values.stream()
                .map(value -> new ScbAvailability(indicatorId, value.geoVersion(), value.year()))
                .distinct()
                .toList();
        if (available.isEmpty()) {
            return;
        }
        var insert = tx.insertInto(
                SCB_AVAILABILITY,
                SCB_AVAILABILITY.INDICATOR_ID,
                SCB_AVAILABILITY.GEO_VERSION,
                SCB_AVAILABILITY.YEAR
        );
        for (final var availability : available) {
            insert = insert.values(indicatorId, availability.geoVersion().code(), (short) availability.year());
        }
        insert.execute();
    }

    public List<ScbAvailability> readAvailability() {
        return readAvailability(DSL.noCondition());
    }

    public List<ScbAvailability> readAvailability(String indicatorId) {
        return readAvailability(SCB_AVAILABILITY.INDICATOR_ID.eq(indicatorId));
    }

    private List<ScbAvailability> readAvailability(Condition condition) {
        return dslContext.select(SCB_AVAILABILITY.INDICATOR_ID, SCB_AVAILABILITY.GEO_VERSION, SCB_AVAILABILITY.YEAR)
                .from(SCB_AVAILABILITY)
                .where(condition)
                .fetch()
                .map(ScbRegionalRepository::toAvailability);
    }

    public Map<String, Double> readValues(String indicatorId, GeoVersion geoVersion, int year) {
        final var values = new LinkedHashMap<String, Double>();
        dslContext.select(SCB_AREA_VALUE.AREA_CODE, SCB_AREA_VALUE.VALUE)
                .from(SCB_AREA_VALUE)
                .where(SCB_AREA_VALUE.INDICATOR_ID.eq(indicatorId))
                .and(SCB_AREA_VALUE.GEO_VERSION.eq(geoVersion.code()))
                .and(SCB_AREA_VALUE.YEAR.eq((short) year))
                .orderBy(SCB_AREA_VALUE.AREA_CODE)
                .fetch()
                .forEach(r -> values.put(r.value1(), r.value2().doubleValue()));

        return values;
    }

    private static ScbAvailability toAvailability(Record3<String, String, Short> r) {
        return new ScbAvailability(
                r.value1(),
                GeoVersion.fromCode(r.value2()).orElseThrow(),
                r.value3()
        );
    }

    private static ScbIndicator toIndicator(ScbIndicatorRecord r) {
        return ScbIndicator.builder()
                .id(r.getId())
                .label(r.getLabel())
                .description(r.getDescription())
                .category(r.getCategory())
                .unit(r.getUnit())
                .decimals(r.getDecimals())
                .levels(Arrays.stream(r.getLevels())
                        .map(level -> ScbLevel.fromLabel(level).orElseThrow(
                                () -> new IllegalStateException("Unknown level %s for %s".formatted(level, r.getId()))))
                        .toList())
                .numerator(toSources(r.getNumerator()))
                .denominator(toSources(r.getDenominator()))
                .multiplier(r.getMultiplier().doubleValue())
                .sortOrder(r.getSortOrder())
                .build();
    }

    private static List<ScbSource> toSources(JSONB jsonb) {
        if (jsonb == null) {
            return List.of();
        }

        return JSON_MAPPER.readValue(jsonb.data(), SOURCE_LIST).stream()
                .map(SourceJson::toSource)
                .toList();
    }

    // "contents" is shorthand for selecting one ContentsCode
    private record SourceJson(String table, String contents, Map<String, List<String>> select) {

        private ScbSource toSource() {
            final var selection = new LinkedHashMap<String, List<String>>();
            if (contents != null) {
                selection.put("ContentsCode", List.of(contents));
            }
            if (select != null) {
                selection.putAll(select);
            }

            return new ScbSource(table, Collections.unmodifiableMap(selection));
        }

    }

}
