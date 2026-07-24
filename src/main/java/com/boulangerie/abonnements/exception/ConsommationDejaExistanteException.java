// abonnements/exception/ConsommationDejaExistanteException.java
package com.boulangerie.abonnements.exception;

import com.boulangerie.shared.exception.ConflictException;

import java.time.LocalDate;

public class ConsommationDejaExistanteException extends ConflictException {

    public ConsommationDejaExistanteException(Long ligneId, LocalDate date) {
        super("Une consommation existe déjà pour la ligne " + ligneId + " à la date " + date);
    }
}