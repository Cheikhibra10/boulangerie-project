package com.boulangerie.abonnements.service;

import com.boulangerie.abonnements.model.CompteAbonnement;
import com.boulangerie.abonnements.model.VersementAbonnement;
import com.boulangerie.shared.model.TypePaiement;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class VersementAbonnementFactory {

    public VersementAbonnement create(CompteAbonnement compteAbonnement, BigDecimal montant, TypePaiement modePaiement){
        VersementAbonnement  versementAbonnement = new VersementAbonnement();
        versementAbonnement.setMontant(montant);
        versementAbonnement.setModePaiement(modePaiement);
        versementAbonnement.setLibelle("Versement abonnement - " + compteAbonnement.getAbonnement().getNom());
        versementAbonnement.setCompte(compteAbonnement);
        return versementAbonnement;
    }
}
