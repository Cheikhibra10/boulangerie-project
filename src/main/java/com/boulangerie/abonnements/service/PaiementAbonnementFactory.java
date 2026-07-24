package com.boulangerie.abonnements.service;

import com.boulangerie.abonnements.model.*;

import com.boulangerie.shared.model.TypePaiement;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class PaiementAbonnementFactory {
    private final ApplicationEventPublisher publisher;

    public PaiementAbonnement create(
            LigneAbonnement ligne,
            BigDecimal montant,
            TypePaiement modePaiement
    ) {
            PaiementAbonnement paiementAbonnemt = new PaiementAbonnement();
            paiementAbonnemt.setMontant(montant);
            paiementAbonnemt.setModePaiement(modePaiement);
            paiementAbonnemt.setLibelle("Paiement abonnement - " + ligne.getClient().getId());
            paiementAbonnemt.setLigne(ligne);
         return paiementAbonnemt;
    }
}