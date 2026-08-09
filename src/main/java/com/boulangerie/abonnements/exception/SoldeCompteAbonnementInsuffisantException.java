package com.boulangerie.abonnements.exception;

import com.boulangerie.shared.exception.ConflictException;

import java.math.BigDecimal;

public class SoldeCompteAbonnementInsuffisantException extends ConflictException {
    public SoldeCompteAbonnementInsuffisantException(Long id, BigDecimal soldeActuel, BigDecimal montant) {
        super("");

    }
}
