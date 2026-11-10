package com.kg.macroanalyzer.parliament.adaptors.postgres;

import com.kg.macroanalyzer.parliament.application.port.out.ParliamentRepositoryPort;
import com.kg.macroanalyzer.parliament.domain.Election;
import com.kg.macroanalyzer.parliament.domain.MemberActivity;
import com.kg.macroanalyzer.parliament.domain.MemberOfParliament;
import com.kg.macroanalyzer.parliament.domain.Party;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class ParliamentPostgresAdaptor implements ParliamentRepositoryPort {

    private final SweParMemberRepository sweParMemberRepository;
    private final CachedMembers cachedMembers;
    private final SweParActivityRepository sweParActivityRepository;
    private final RiksdagElectionRepository riksdagElectionRepository;

    public ParliamentPostgresAdaptor(
            SweParMemberRepository sweParMemberRepository,
            CachedMembers cachedMembers,
            SweParActivityRepository sweParActivityRepository,
            RiksdagElectionRepository riksdagElectionRepository
    ) {
        this.sweParMemberRepository = sweParMemberRepository;
        this.cachedMembers = cachedMembers;
        this.sweParActivityRepository = sweParActivityRepository;
        this.riksdagElectionRepository = riksdagElectionRepository;
    }

    @Override
    public Integer writeMembersOfParliament(List<MemberOfParliament> membersOfParliament) {
        return cachedMembers.write(membersOfParliament);
    }

    @Override
    public List<MemberOfParliament> readMembersOfParliament() {
        return cachedMembers.read();
    }

    @Override
    public Optional<MemberOfParliament> readMemberOfParliament(String intressentId) {
        return sweParMemberRepository.readMember(intressentId);
    }

    @Override
    public Map<String, Integer> countMembersByPartyWhereStatusContainsIgnoringCase(String statusFragment) {
        return sweParMemberRepository.countMembersByParty(statusFragment);
    }

    @Override
    public Integer writeMemberActivity(List<MemberActivity> memberActivity) {
        return sweParActivityRepository.upsertActivity(memberActivity);
    }

    @Override
    public List<MemberActivity> readMemberActivity() {
        return sweParActivityRepository.readActivity();
    }

    @Override
    public List<Party> readParties() {
        return riksdagElectionRepository.readParties();
    }

    @Override
    public List<Election> readElections() {
        return riksdagElectionRepository.readElections();
    }

    @Override
    public Map<String, String> readOrganNames() {
        return riksdagElectionRepository.readOrganNames();
    }

}
