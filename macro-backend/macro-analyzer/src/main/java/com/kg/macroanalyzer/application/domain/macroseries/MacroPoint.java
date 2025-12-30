package com.kg.macroanalyzer.application.domain.macroseries;


import lombok.Builder;
import lombok.NonNull;

import java.time.LocalDate;
import java.time.temporal.Temporal;

@Builder(toBuilder = true)
public record MacroPoint(
        @NonNull Double value,
        @NonNull Temporal date
) implements Comparable<MacroPoint> {
    @Override
    public int compareTo(MacroPoint macroPoint) {
        final var localDate = (LocalDate) this.date;
        final var remoteDate = (LocalDate) macroPoint.date;

        if (localDate.isBefore(remoteDate)) return -1;
        else if (localDate.equals(remoteDate)) return 0;
        else return 1;
    }

}
