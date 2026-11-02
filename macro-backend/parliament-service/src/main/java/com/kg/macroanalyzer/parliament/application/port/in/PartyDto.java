package com.kg.macroanalyzer.parliament.application.port.in;

import lombok.Builder;

@Builder
public record PartyDto(String code, String name, String color, String ideology, boolean active) {
}
