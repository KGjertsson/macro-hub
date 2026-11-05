package com.kg.macroanalyzer.parliament.domain;

import com.kg.macroanalyzer.shared.collection.ImmutableCopies;

import java.util.List;

public record PersonUppgift(List<Uppgift> uppgift) {

    public PersonUppgift {
        uppgift = ImmutableCopies.list(uppgift);
    }

}
