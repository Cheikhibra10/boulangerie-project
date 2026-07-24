package com.boulangerie.achats.exception;

import com.boulangerie.shared.exception.BadRequestException;
import com.boulangerie.shared.exception.ConflictException;

import java.math.BigDecimal;

public class MontantExcedentaireException extends ConflictException {
    public MontantExcedentaireException(Long achatId, BigDecimal montant, BigDecimal restantDu) {
        super(String.format("Montant %s dépasse le restant dû %s avec l'achat %d", montant, restantDu, achatId));
    }
}