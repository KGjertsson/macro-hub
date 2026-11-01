package com.kg.macroanalyzer.regionalstatistics.domain;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

// One SCB table holds every version of an area division; the version is only visible in the region code.
// The 2025 versions carry a suffix that the boundary files do not, so it is stripped before storing.
// wildcard is the PxWeb valueCodes pattern that selects the version's codes in a data request
public enum GeoVersion {
    DESO_2018("DeSO2018", ScbLevel.DESO, "\\d{4}[ABC]\\d{4}", "", "?????????"),
    DESO_2025("DeSO2025", ScbLevel.DESO, "\\d{4}[ABC]\\d{4}_DeSO2025", "_DeSO2025", "*_DeSO2025"),
    REGSO_2020("RegSO2020", ScbLevel.REGSO, "\\d{4}R\\d{3}", "", "????????"),
    REGSO_2025("RegSO2025", ScbLevel.REGSO, "\\d{4}R\\d{3}_RegSO2025", "_RegSO2025", "*_RegSO2025");

    private final String code;
    private final ScbLevel level;
    private final Pattern pattern;
    private final String suffix;
    private final String wildcard;
    private final Pattern wildcardPattern;

    GeoVersion(String code, ScbLevel level, String regex, String suffix, String wildcard) {
        this.code = code;
        this.level = level;
        this.pattern = Pattern.compile(regex);
        this.suffix = suffix;
        this.wildcard = wildcard;
        this.wildcardPattern = Pattern.compile(wildcard.replace("?", ".").replace("*", ".*"));
    }

    public String code() {
        return code;
    }

    public ScbLevel level() {
        return level;
    }

    public String wildcard() {
        return wildcard;
    }

    public boolean matches(String regionCode) {
        return pattern.matcher(regionCode).matches();
    }

    // The wildcard can also pick up codes of other divisions, which the cell budget has to count
    public boolean matchesWildcard(String regionCode) {
        return wildcardPattern.matcher(regionCode).matches();
    }

    public String areaCode(String regionCode) {
        return regionCode.substring(0, regionCode.length() - suffix.length());
    }

    public static Optional<GeoVersion> fromRegionCode(String regionCode) {
        return Arrays.stream(values())
                .filter(v -> v.matches(regionCode))
                .findFirst();
    }

    public static Optional<GeoVersion> fromCode(String code) {
        return Arrays.stream(values())
                .filter(v -> v.code.equals(code))
                .findFirst();
    }

    static List<GeoVersion> of(ScbLevel level) {
        return Arrays.stream(values())
                .filter(v -> v.level == level)
                .toList();
    }

}
