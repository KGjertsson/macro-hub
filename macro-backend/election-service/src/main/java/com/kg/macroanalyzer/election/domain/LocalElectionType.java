package com.kg.macroanalyzer.election.domain;

import java.util.Optional;

// KF = kommunfullmäktige, RF = regionfullmäktige, RD = riksdag (per valkrets), matching Valmyndigheten's file suffixes
public enum LocalElectionType {
    KF("MunicipalElections"),
    RF("RegionElections"),
    RD("ParliamentElections");

    private final String configName;

    LocalElectionType(String configName) {
        this.configName = configName;
    }

    public String configName() {
        return configName;
    }

    public static Optional<LocalElectionType> fromConfigName(String configName) {
        for (final var type : values()) {
            if (type.configName.equals(configName)) {
                return Optional.of(type);
            }
        }

        return Optional.empty();
    }

}
