package com.kg.macroanalyzer.parliament.domain;

import java.util.List;

public record Uppgift(
        String kod,
        List<Object> uppgift,
        String typ,
        String intressent_id,
        String hangar_id
) {
}
