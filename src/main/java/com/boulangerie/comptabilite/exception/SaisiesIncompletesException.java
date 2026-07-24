// caisse/exception/SaisiesIncompletesException.java
package com.boulangerie.comptabilite.exception;

import com.boulangerie.shared.exception.BusinessException;

public class SaisiesIncompletesException extends BusinessException {

    public SaisiesIncompletesException() {
        super("Toutes les saisies de la journée ne sont pas complètes. Veuillez finaliser avant de fermer la caisse.");
    }
}