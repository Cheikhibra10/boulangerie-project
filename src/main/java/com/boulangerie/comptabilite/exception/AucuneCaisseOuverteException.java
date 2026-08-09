package com.boulangerie.comptabilite.exception;

import com.boulangerie.shared.exception.BadRequestException;

public class AucuneCaisseOuverteException extends BadRequestException {
    public AucuneCaisseOuverteException() {
        super("Aucune caisse ouverte trouvée");
    }
}
