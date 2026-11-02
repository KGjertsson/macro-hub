package com.kg.macroanalyzer.parliament.domain;

import java.util.List;

public class TestMemberFactory {

    public static MemberOfParliament member(String id, String party, String status, List<Uppdrag> assignments) {
        return member(id, party, status, assignments, List.of());
    }

    public static MemberOfParliament member(
            String id,
            String party,
            String status,
            List<Uppdrag> assignments,
            List<Uppgift> details
    ) {
        return new MemberOfParliament(
                null, null, id, null, "1980", "kvinna", "Efternamn" + id, "Förnamn", "Efternamn" + id + ",Förnamn",
                "", party, "Stockholms län", status, null, null, "https://bild/" + id + "_192.jpg",
                "https://bild/" + id + "_max.jpg", new PersonUppdrag(assignments), new PersonUppgift(details)
        );
    }

    public static Uppdrag uppdrag(String type, String organ, String role, String status, String from, String tom) {
        return new Uppdrag(organ, role, null, status, type, from, tom, List.of(), null, null, null, null, null, null);
    }

    public static Uppdrag chamberSeat(String from, String tom) {
        return uppdrag(Uppdrag.CHAMBER, "kam", "Riksdagsledamot", "Tjänstgörande", from, tom);
    }

    public static Uppgift uppgift(String code, String type, String value) {
        return new Uppgift(code, List.of(value), type, null, null);
    }

}
