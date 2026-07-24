// livreurs/exception/CompteRenduDejaClotureException.java
package com.boulangerie.livreurs.exception;

import com.boulangerie.shared.exception.ConflictException;

public class CompteRenduDejaClotureException extends ConflictException {

    public CompteRenduDejaClotureException(Long journalierId) {
        super("Le compte rendu " + journalierId + " est déjà clôturé");
    }
}