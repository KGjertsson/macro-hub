package com.kg.macroanalyzer.election.application.port.out;

import com.kg.macroanalyzer.election.domain.LocalElectionType;

public record LocalElectionIndexRequest(String baseUrl, LocalElectionType type) {
}
