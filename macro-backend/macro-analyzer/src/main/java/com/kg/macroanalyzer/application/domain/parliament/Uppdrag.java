package com.kg.macroanalyzer.application.domain.parliament;

import java.util.List;

public record Uppdrag(
        String organ_kod,
        String roll_kod,
        String ordningsnummer,
        String status,
        String typ,
        String from,
        String tom,
        List<Object> uppgift,
        String intressent_id,
        String hangar_id,
        String sortering,
        String organ_sortering,
        String uppdrag_rollsortering,
        String uppdrag_statussortering
) {
}
