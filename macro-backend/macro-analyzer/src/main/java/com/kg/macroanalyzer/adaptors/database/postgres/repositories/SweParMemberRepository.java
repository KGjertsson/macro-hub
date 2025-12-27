package com.kg.macroanalyzer.adaptors.database.postgres.repositories;

import com.kg.macroanalyzer.application.domain.MacroPoint;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

@Repository
public class SweParMemberRepository {

    private final DSLContext dslContext;

    @Autowired
    public SweParMemberRepository(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    public Supplier<List<MacroPoint>> reader() {
        return Collections::emptyList;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public Function<List<MacroPoint>, Integer> writer() {
        return macroPointList -> 1;
    }

}
