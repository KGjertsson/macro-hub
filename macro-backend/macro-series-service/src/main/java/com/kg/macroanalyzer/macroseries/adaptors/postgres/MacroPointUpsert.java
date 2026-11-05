package com.kg.macroanalyzer.macroseries.adaptors.postgres;

import com.kg.macroanalyzer.macroseries.domain.MacroPoint;
import lombok.experimental.UtilityClass;
import org.jooq.DSLContext;
import org.jooq.InsertOnDuplicateStep;
import org.jooq.Query;
import org.jooq.Record;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.impl.DSL;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

/**
 * A point stored with the same value isn't touched, so writes that race or repeat change nothing.
 */
@UtilityClass
class MacroPointUpsert {

    static <R extends Record> Function<List<MacroPoint>, Integer> writer(
            DSLContext dslContext,
            Table<R> table,
            TableField<R, UUID> globalId,
            TableField<R, Double> value,
            TableField<R, LocalDate> date,
            TableField<R, LocalDateTime> updated
    ) {
        final var insert = dslContext.insertInto(table, globalId, value, date)
                .values(DSL.val((UUID) null), DSL.val(0.0), DSL.val(LocalDate.MIN));
        final var upsert = replaceRevisedValue(insert, value, updated, date);

        return macroPoints -> execute(
                dslContext,
                upsert,
                macroPoints,
                macroPoint -> new Object[]{UUID.randomUUID(), macroPoint.value(), macroPoint.date()}
        );
    }

    static <R extends Record> Function<List<MacroPoint>, Integer> writer(
            DSLContext dslContext,
            String seriesName,
            Table<R> table,
            TableField<R, String> seriesNameField,
            TableField<R, UUID> globalId,
            TableField<R, Double> value,
            TableField<R, LocalDate> date,
            TableField<R, LocalDateTime> updated
    ) {
        final var insert = dslContext.insertInto(table, globalId, seriesNameField, value, date)
                .values(DSL.val((UUID) null), DSL.val(""), DSL.val(0.0), DSL.val(LocalDate.MIN));
        final var upsert = replaceRevisedValue(insert, value, updated, seriesNameField, date);

        return macroPoints -> execute(
                dslContext,
                upsert,
                macroPoints,
                macroPoint -> new Object[]{UUID.randomUUID(), seriesName, macroPoint.value(), macroPoint.date()}
        );
    }

    private static <R extends Record> Query replaceRevisedValue(
            InsertOnDuplicateStep<R> insert,
            TableField<R, Double> value,
            TableField<R, LocalDateTime> updated,
            TableField<?, ?>... key
    ) {
        return insert.onConflict(key)
                .doUpdate()
                .set(value, DSL.excluded(value))
                .set(updated, DSL.currentLocalDateTime())
                .where(value.isDistinctFrom(DSL.excluded(value)));
    }

    private static int execute(
            DSLContext dslContext,
            Query upsert,
            List<MacroPoint> macroPoints,
            Function<MacroPoint, Object[]> bindValues
    ) {
        if (macroPoints.isEmpty()) {
            return 0;
        }
        final var batch = dslContext.batch(upsert);
        macroPoints.forEach(macroPoint -> batch.bind(bindValues.apply(macroPoint)));

        return Arrays.stream(batch.execute()).sum();
    }

}
