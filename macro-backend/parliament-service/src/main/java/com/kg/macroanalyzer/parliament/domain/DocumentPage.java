package com.kg.macroanalyzer.parliament.domain;

import com.kg.macroanalyzer.shared.collection.ImmutableCopies;

import java.util.List;

public record DocumentPage(int total, List<ParliamentDocument> recent) {

    public static final DocumentPage EMPTY = new DocumentPage(0, List.of());

    public DocumentPage {
        recent = ImmutableCopies.list(recent);
    }

}
