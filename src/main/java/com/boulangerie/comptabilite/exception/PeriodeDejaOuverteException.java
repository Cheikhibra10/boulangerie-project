package com.boulangerie.comptabilite.exception;

import com.boulangerie.shared.exception.ConflictException;

public class PeriodeDejaOuverteException extends PeriodeException {
    public PeriodeDejaOuverteException(Long periodeId) {
        super(periodeId, "La période est déjà ouverte");
    }
}
