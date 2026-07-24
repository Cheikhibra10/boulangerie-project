// production/exception/LotAlreadyExistsException.java
package com.boulangerie.production.exception;

import com.boulangerie.shared.exception.ConflictException;

import java.time.LocalDate;

public class LotAlreadyExistsException extends ConflictException {

    public LotAlreadyExistsException(LocalDate date, Long produitId) {
        super("Un lot de production existe déjà pour la date " + date + " et le produit " + produitId);
    }
}