package com.kg.macroanalyzer.application.ports.driving.out.parliament;

import lombok.Builder;

@Builder
public record PartyDto(String code, String name, String color, String ideology, boolean active) {
}
