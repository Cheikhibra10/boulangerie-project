package com.boulangerie.production.exception;

import com.boulangerie.shared.exception.BusinessException;

/**
 * Levée quand on tente de distribuer de la production vers un abonnement
 * (canal ABONNEMENT) alors que le livreur rattaché à cet abonnement n'a
 * lui-même reçu aucune distribution (canal LIVREUR) pour cette même date —
 * ni déjà enregistrée, ni dans la même requête. Physiquement, un livreur
 * qui n'a rien reçu ne peut rien livrer à ses abonnés.
 */
public class LivreurSansDistributionException extends BusinessException {

    public LivreurSansDistributionException(Long livreurId, Long abonnementId) {
        super("Distribution vers l'abonnement #" + abonnementId + " refusée : le livreur #" + livreurId
                + " n'a reçu aucune distribution pour cette date — distribuez-lui d'abord sa production.");
    }
}