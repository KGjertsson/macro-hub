package com.kg.macroanalyzer.election.application.port.in;

import lombok.Builder;

@Builder
public record ElectedMemberDto(String name, int order, String electedOn) {
}
