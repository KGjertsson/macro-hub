package com.kg.macroanalyzer.parliament.application.port.out;

import com.kg.macroanalyzer.parliament.domain.Election;
import com.kg.macroanalyzer.parliament.domain.MemberActivity;
import com.kg.macroanalyzer.parliament.domain.MemberOfParliament;
import com.kg.macroanalyzer.parliament.domain.Party;

import java.util.List;
import java.util.Map;

public interface ParliamentRepositoryPort {

    Integer writeMembersOfParliament(List<MemberOfParliament> membersOfParliament);

    List<MemberOfParliament> readMembersOfParliament();

    Integer writeMemberActivity(List<MemberActivity> memberActivity);

    List<MemberActivity> readMemberActivity();

    List<Party> readParties();

    List<Election> readElections();

    Map<String, String> readOrganNames();

}
