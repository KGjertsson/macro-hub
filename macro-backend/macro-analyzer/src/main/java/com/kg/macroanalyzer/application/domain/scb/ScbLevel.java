package com.kg.macroanalyzer.application.domain.scb;

import java.util.List;
import java.util.Optional;

// SCB's two small-area divisions: DeSO (~6 000 areas) and RegSO (~3 400 areas), each published in two versions
public enum ScbLevel {
    DESO("DeSO"),
    REGSO("RegSO");

    private final String label;

    ScbLevel(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    // Oldest first, so the last version with data for a year is the newest
    public List<GeoVersion> versions() {
        return GeoVersion.of(this);
    }

    public static Optional<ScbLevel> fromLabel(String label) {
        for (final var level : values()) {
            if (level.label.equalsIgnoreCase(label)) {
                return Optional.of(level);
            }
        }

        return Optional.empty();
    }

}
