package com.kg.macroanalyzer.application.domain.parliament;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public record MemberOfParliament(
        String hangar_guid,
        String sourceid,
        String intressent_id,
        String hangar_id,
        String fodd_ar,
        String kon,
        String efternamn,
        String tilltalsnamn,
        String sorteringsnamn,
        String iort,
        String parti,
        String valkrets,
        String status,
        String person_url_xml,
        String bild_url_80,
        String bild_url_192,
        String bild_url_max,
        PersonUppdrag personuppdrag,
        PersonUppgift personuppgift
) {

    public String fullName() {
        return "%s %s".formatted(tilltalsnamn, efternamn).trim();
    }

    public List<Uppdrag> assignments() {
        return Optional.ofNullable(personuppdrag)
                .map(PersonUppdrag::uppdrag)
                .orElse(List.of());
    }

    public List<Uppgift> details() {
        return Optional.ofNullable(personuppgift)
                .map(PersonUppgift::uppgift)
                .orElse(List.of());
    }

    public List<Uppdrag> chamberAssignments() {
        return assignments().stream()
                .filter(Uppdrag::chamberAssignment)
                .toList();
    }

    public boolean satInChamberDuring(LocalDate termStart, LocalDate termEnd) {
        return chamberAssignments().stream()
                .anyMatch(u -> u.overlaps(termStart, termEnd));
    }

}
