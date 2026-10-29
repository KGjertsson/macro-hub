package com.kg.macroanalyzer.parliament.application.port.in;

import com.kg.macroanalyzer.parliament.domain.Party;

import java.util.List;

// The Riksdag parties with their names and colours, for other modules that show party results
public interface PartyDirectory {

    List<Party> listParties();

}
