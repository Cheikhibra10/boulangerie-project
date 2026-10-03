package com.boulangerie.livreurs.exception;

import com.boulangerie.shared.exception.BusinessException;

import java.math.BigDecimal;

/**
 * Levée à la clôture d'un compte-rendu quand la somme des qteAbonnement
 * déclarées par le livreur sur ses lignes ne correspond pas à la quantité
 * officiellement distribuée aux abonnements qui lui sont rattachés pour
 * cette date (canal ABONNEMENT, voir ProductionServiceImpl). Un écart
 * signifie soit une erreur de saisie du livreur, soit une distribution mal
 * planifiée côté production — dans les deux cas, pas quelque chose à
 * clôturer silencieusement.
 */
public class QuantiteAbonnementIncoherenteException extends BusinessException {

    public QuantiteAbonnementIncoherenteException(
            Long livreurId,
            BigDecimal declare,
            BigDecimal attendu
    ) {
        super("Clôture refusée pour le livreur #" + livreurId
                + " : quantité abonnement déclarée (" + declare
                + ") différente de la quantité distribuée aux abonnements pour cette date (" + attendu
                + "). Vérifiez les lignes du compte-rendu ou la distribution de production.");
    }
}