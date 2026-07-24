package com.boulangerie.comptabilite.exception;

import com.boulangerie.shared.exception.EntityNotFoundException;

public class PeriodeNotFoundException extends EntityNotFoundException {
    public PeriodeNotFoundException(Long id) {
        super("Période introuvable: " +id);
    }
}