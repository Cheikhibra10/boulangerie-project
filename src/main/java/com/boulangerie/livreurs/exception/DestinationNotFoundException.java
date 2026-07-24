// livreurs/exception/DestinationNotFoundException.java
package com.boulangerie.livreurs.exception;

import com.boulangerie.shared.exception.BusinessException;

import java.time.LocalDate;

public class DestinationNotFoundException extends BusinessException {

    public DestinationNotFoundException(LocalDate date, Long livreurId) {
        super("Aucune destination de production trouvée pour le livreur " + livreurId + " à la date " + date);
    }
}