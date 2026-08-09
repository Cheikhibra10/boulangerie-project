// caisse/exception/CaisseFermeeException.java
package com.boulangerie.comptabilite.exception;

import com.boulangerie.shared.exception.BadRequestException;
import com.boulangerie.shared.exception.BusinessException;

public class CaisseFermeeException extends BadRequestException {
    public CaisseFermeeException() {
        super("La caisse est fermée. Veuillez l'ouvrir pour effectuer cette opération.");
    }
}