// abonnements/exception/ClientDejaAbonneException.java
package com.boulangerie.abonnements.exception;

import com.boulangerie.shared.exception.ConflictException;

public class ClientDejaAbonneException extends ConflictException {

    public ClientDejaAbonneException(Long abonnementId, Long clientId) {
        super("Le client " + clientId + " est déjà abonné à l'abonnement " + abonnementId);
    }
}