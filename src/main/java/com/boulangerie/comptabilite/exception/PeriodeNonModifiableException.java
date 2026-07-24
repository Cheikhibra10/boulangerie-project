package com.boulangerie.comptabilite.exception;

import com.boulangerie.administration.model.StatutPeriode;

public class PeriodeNonModifiableException extends PeriodeException {
    public PeriodeNonModifiableException(Long id, StatutPeriode statut) {
        super("");
    }
}
