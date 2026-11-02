package com.kg.macroanalyzer.parliament.adaptors.postgres;

import com.kg.macroanalyzer.parliament.application.port.out.ParliamentRepositoryPort;
import com.kg.macroanalyzer.parliament.domain.Election;
import com.kg.macroanalyzer.parliament.domain.MemberActivity;
import com.kg.macroanalyzer.parliament.domain.MemberOfParliament;
import com.kg.macroanalyzer.parliament.domain.Party;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class ParliamentPostgresAdaptor implements ParliamentRepositoryPort {

    private final SweParMemberRepository sweParMemberRepository;
    private final SweParActivityRepository sweParActivityRepository;
    private final RiksdagElectionRepository riksdagElectionRepository;

    public ParliamentPostgresAdaptor(
            SweParMemberRepository sweParMemberRepository,
            SweParActivityRepository sweParActivityRepository,
            RiksdagElectionRepository riksdagElectionRepository
    ) {
        this.sweParMemberRepository = sweParMemberRepository;
        this.sweParActivityRepository = sweParActivityRepository;
        this.riksdagElectionRepository = riksdagElectionRepository;
    }

    @Override
    public Integer writeMembersOfParliament(List<MemberOfParliament> membersOfParliament) {
        return sweParMemberRepository.upsertMembers(membersOfParliament);
    }

    @Override
    public List<MemberOfParliament> readMembersOfParliament() {
        return sweParMemberRepository.readMembers();
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
