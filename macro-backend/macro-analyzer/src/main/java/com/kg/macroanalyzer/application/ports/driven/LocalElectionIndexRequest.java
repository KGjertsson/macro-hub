package com.kg.macroanalyzer.application.ports.driven;

import com.kg.macroanalyzer.application.domain.localelection.LocalElectionType;

public record LocalElectionIndexRequest(String baseUrl, LocalElectionType type) {
}
