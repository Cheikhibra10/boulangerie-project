package com.boulangerie.abonnements.dto;

import java.time.*;
import java.util.Objects;

public record ReportingPeriod(
        YearMonth month,
        LocalDate startDate,
        LocalDate endDateExclusive,
        Instant startInstant,
        Instant endInstant
) {

    public static ReportingPeriod of(
            YearMonth month,
            ZoneId zone
    ) {
        Objects.requireNonNull(month, "La période est obligatoire");
        Objects.requireNonNull(zone, "La zone horaire est obligatoire");

        LocalDate startDate = month.atDay(1);
        LocalDate endDateExclusive = month.plusMonths(1).atDay(1);

        return new ReportingPeriod(
                month,
                startDate,
                endDateExclusive,
                startDate.atStartOfDay(zone).toInstant(),
                endDateExclusive.atStartOfDay(zone).toInstant()
        );
    }

    public LocalDate endDateInclusive() {
        return endDateExclusive.minusDays(1);
    }
}