package com.kg.macroanalyzer.application.domain.parliament;

import lombok.Builder;

@Builder
public record Party(String code, String name, String color, String ideology, boolean active) {
}
