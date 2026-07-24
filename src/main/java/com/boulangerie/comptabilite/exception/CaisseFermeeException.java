// caisse/exception/CaisseFermeeException.java
package com.boulangerie.comptabilite.exception;

import com.boulangerie.shared.exception.BusinessException;

public class CaisseFermeeException extends BusinessException {

    public CaisseFermeeException() {
        super("La caisse est fermée. Veuillez l'ouvrir pour effectuer cette opération.");
    }
}