package com.boulangerie.comptabilite.exception;

import com.boulangerie.administration.model.StatutPeriode;
import com.boulangerie.shared.exception.ConflictException;

public class PeriodeNonOuverteException extends ConflictException {
    public PeriodeNonOuverteException(Long id, StatutPeriode statut) {
        super(String.format("Impossible d'ouvrir periode: %d, statut actuel: %s", id, statut));
    }
}
