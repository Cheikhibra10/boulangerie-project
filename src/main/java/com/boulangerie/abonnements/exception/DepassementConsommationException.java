package com.boulangerie.abonnements.exception;

import com.boulangerie.shared.exception.ConflictException;

import java.math.BigDecimal;
import java.time.LocalDate;

public class DepassementConsommationException extends ConflictException {
    public DepassementConsommationException(Long id, LocalDate date, BigDecimal quantiteDistribuee, BigDecimal nouveauTotal) {
        super(
                String.format("" +
                        "")
        );

    }
}
