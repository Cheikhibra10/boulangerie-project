package com.boulangerie.comptabilite.exception;

import com.boulangerie.shared.exception.ConflictException;

public class PeriodeDejaFermeeException extends PeriodeException {
    public PeriodeDejaFermeeException(Long periodeId) {
        super(periodeId, "La période est déjà fermée");
    }
}