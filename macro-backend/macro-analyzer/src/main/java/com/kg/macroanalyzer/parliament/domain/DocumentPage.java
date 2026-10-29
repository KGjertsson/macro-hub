package com.kg.macroanalyzer.parliament.domain;

import java.util.List;

public record DocumentPage(int total, List<ParliamentDocument> recent) {

    public static final DocumentPage EMPTY = new DocumentPage(0, List.of());

}
