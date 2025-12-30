package com.kg.macroanalyzer.application.domain.parliament;

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
}

