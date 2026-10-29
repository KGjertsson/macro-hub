package com.kg.macroanalyzer.parliament.application.port.in;

import lombok.Builder;

@Builder
public record PartySeats(String code, String name, String color, int seats) {
}
