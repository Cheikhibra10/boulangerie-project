package com.boulangerie.comptabilite.api;

import java.time.LocalDate;

public interface PeriodeLookupApi {
    record PeriodeInfo(Long id, LocalDate dateDebut, LocalDate dateFin) {}
    PeriodeInfo getByIdOrThrow(Long periodeId);
    Long findPeriodeIdOrThrow(Long id);
}