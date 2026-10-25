package com.kg.macroanalyzer.application.domain.parliament;

import java.time.LocalDate;
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

    public static final String CHAMBER = "kammaruppdrag";

    public boolean chamberAssignment() {
        return CHAMBER.equals(typ);
    }

    // Riksdagen mixes "2018-09-24" and "2026-09-28 11:00:00"
    public LocalDate fromDate() {
        return parseDate(from, LocalDate.MIN);
    }

    public LocalDate tomDate() {
        return parseDate(tom, LocalDate.MAX);
    }

    // A term ending on the day the next one starts does not count as part of the next term
    public boolean overlaps(LocalDate start, LocalDate end) {
        return !fromDate().isAfter(end) && tomDate().isAfter(start);
    }

    public boolean activeOn(LocalDate date) {
        return !fromDate().isAfter(date) && !tomDate().isBefore(date);
    }

    private static LocalDate parseDate(String value, LocalDate fallback) {
        if (value == null || value.length() < 10) {
            return fallback;
        }

        return LocalDate.parse(value.substring(0, 10));
    }

}
