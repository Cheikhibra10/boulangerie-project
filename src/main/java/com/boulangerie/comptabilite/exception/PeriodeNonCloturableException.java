package com.boulangerie.comptabilite.exception;

import com.boulangerie.administration.model.StatutPeriode;

public class PeriodeNonCloturableException extends PeriodeException {
    public PeriodeNonCloturableException(Long periodeId, StatutPeriode statut) {
        super(periodeId, String.format("Impossible de clôturer, statut actuel: %s", statut));
    }
}
