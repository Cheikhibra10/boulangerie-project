// caisse/exception/CaisseDejaOuverteException.java
package com.boulangerie.comptabilite.exception;

import com.boulangerie.shared.exception.ConflictException;

public class CaisseDejaOuverteException extends ConflictException {

    public CaisseDejaOuverteException() {
        super("Une caisse est déjà ouverte pour aujourd'hui");
    }
}