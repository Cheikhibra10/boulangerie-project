package com.boulangerie.comptabilite.exception;

import com.boulangerie.shared.exception.ConflictException;

public class DateInvalideException extends PeriodeException {
    public DateInvalideException(String message) {
        super(message);
    }
}
