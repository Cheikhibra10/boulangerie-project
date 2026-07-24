// production/exception/DepassementRepartitionException.java
package com.boulangerie.production.exception;

import com.boulangerie.shared.exception.BusinessException;
import com.boulangerie.shared.exception.ConflictException;

import java.math.BigDecimal;

public class DepassementRepartitionException extends ConflictException {

    public DepassementRepartitionException(Long lotId, BigDecimal restant, BigDecimal demande) {
        super("La répartition demandée (" + demande + ") dépasse la quantité restante (" + restant + ") pour le lot " + lotId);
    }
}