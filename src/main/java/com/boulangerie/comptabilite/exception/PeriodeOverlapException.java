package com.boulangerie.comptabilite.exception;

import java.time.LocalDate;

public class PeriodeOverlapException extends PeriodeException {
    public PeriodeOverlapException(LocalDate debut, LocalDate fin) {
        super(String.format("Une période existe déjà entre %s et %s", debut, fin));
    }
}