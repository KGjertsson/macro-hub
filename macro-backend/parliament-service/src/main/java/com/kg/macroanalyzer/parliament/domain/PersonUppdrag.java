package com.kg.macroanalyzer.parliament.domain;

import com.kg.macroanalyzer.shared.collection.ImmutableCopies;

import java.util.List;

public record PersonUppdrag(List<Uppdrag> uppdrag) {

    public PersonUppdrag {
        uppdrag = ImmutableCopies.list(uppdrag);
    }

}
