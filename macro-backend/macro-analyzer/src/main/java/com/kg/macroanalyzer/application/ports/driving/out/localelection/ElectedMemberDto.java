package com.kg.macroanalyzer.application.ports.driving.out.localelection;

import lombok.Builder;

@Builder
public record ElectedMemberDto(String name, int order, String electedOn) {
}
