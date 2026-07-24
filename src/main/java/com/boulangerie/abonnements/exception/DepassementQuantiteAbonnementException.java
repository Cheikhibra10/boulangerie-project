package com.boulangerie.abonnements.exception;

import com.boulangerie.shared.exception.BusinessException;
import com.boulangerie.shared.exception.ConflictException;

import java.math.BigDecimal;

public class DepassementQuantiteAbonnementException extends ConflictException {
    public DepassementQuantiteAbonnementException(Long abonnementId, BigDecimal disponible, BigDecimal demande) {
        super(String.format(
                        """
                        Quantité insuffisante pour l'abonnement %d.
                        Disponible : %s
                        Demandée : %s
                        """,
                        abonnementId,
                        disponible,
                        demande
                )
        );
    }
}
