package com.boulangerie.comptabilite.exception;

import com.boulangerie.shared.exception.EntityNotFoundException;

import java.time.LocalDate;

public class PeriodeeNotFoundException extends EntityNotFoundException {
    public PeriodeeNotFoundException(LocalDate date) {
        super("Aucune période n'existe pour la date : " +date);
    }
}