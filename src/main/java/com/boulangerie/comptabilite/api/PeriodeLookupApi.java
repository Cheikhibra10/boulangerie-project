package com.boulangerie.comptabilite.api;

public interface PeriodeLookupApi {
    Long findPeriodeIdOrThrow(Long id);
}