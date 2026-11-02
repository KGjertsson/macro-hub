package com.kg.macroanalyzer.election.application.port.out;

import java.util.Map;

// The Riksdag parties' colours by party abbreviation. Answers from memory, so a request never waits on or fails
// because of the source. Empty until the first list has loaded
public interface PartyColorPort {

    Map<String, String> partyColors();

}
