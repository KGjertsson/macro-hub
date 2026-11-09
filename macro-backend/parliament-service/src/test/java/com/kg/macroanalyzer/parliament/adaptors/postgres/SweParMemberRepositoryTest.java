package com.kg.macroanalyzer.parliament.adaptors.postgres;

import com.kg.macroanalyzer.parliament.domain.MemberOfParliament;
import com.kg.macroanalyzer.parliament.domain.TestMemberFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = "scheduler.enabled=false")
@ActiveProfiles("test")
@Transactional
class SweParMemberRepositoryTest {

    private static final String SERVING = "tjänstgörande";

    @Autowired
    SweParMemberRepository repository;

    @Test
    void shouldCountMembersPerParty_whoseStatusContainsTheFragment_inAnyCase() {
        // given
        repository.upsertMembers(List.of(
                member("1", "S", "Förste vice talman (tjänstgörande riksdagsledamot)"),
                member("2", "S", "Tjänstgörande ersättare"),
                member("3", "M", "Tjänstgörande statsrådsersättare"),
                member("4", "-", "Tjänstgörande talmansersättare"),
                member("5", "M", "Tjänstledig riksdagsledamot"),
                member("6", "V", "Tidigare riksdagsledamot"),
                member("7", "V", "Avgången riksdagsledamot"),
                member("8", "V", "Tillgänglig ersättare"),
                member("9", "V", null)
        ));

        // when
        final var result = repository.countMembersByParty(SERVING);

        // then
        assertEquals(Map.of("S", 2, "M", 1, "-", 1), result);
    }

    @Test
    void shouldCountAMemberWithoutAParty_underAnEmptyCode() {
        // given
        repository.upsertMembers(List.of(member("1", null, "Tjänstgörande riksdagsledamot")));

        // when
        final var result = repository.countMembersByParty(SERVING);

        // then
        assertEquals(Map.of("", 1), result);
    }

    @Test
    void shouldReadOneMember_byItsId() {
        // given
        repository.upsertMembers(List.of(member("1", "S", "Tidigare riksdagsledamot"), member("2", "M", "Tidigare riksdagsledamot")));

        // when
        final var result = repository.readMember("2");

        // then
        assertEquals("M", result.orElseThrow().parti());
    }

    @Test
    void shouldReadNoMember_forAnUnknownId() {
        // when
        final var result = repository.readMember("missing");

        // then
        assertTrue(result.isEmpty());
    }

    private static MemberOfParliament member(String id, String party, String status) {
        return TestMemberFactory.member(id, party, status, List.of());
    }

}
