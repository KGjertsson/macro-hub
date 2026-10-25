package com.kg.macroanalyzer.application.ports.driving.out.parliament;

import lombok.Builder;

@Builder
public record PartySeats(String code, String name, String color, int seats) {
}
