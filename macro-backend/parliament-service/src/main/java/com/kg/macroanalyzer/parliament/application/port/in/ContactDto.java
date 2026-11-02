package com.kg.macroanalyzer.parliament.application.port.in;

import lombok.Builder;

@Builder
public record ContactDto(String email, String phone, String website) {
}
