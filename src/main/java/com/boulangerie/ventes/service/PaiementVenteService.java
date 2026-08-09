package com.boulangerie.ventes.service;

import com.boulangerie.shared.dto.MouvementCaisseEvent;
import com.boulangerie.shared.model.*;
import com.boulangerie.ventes.model.VenteBoutique;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class PaiementVenteService {

    private final ApplicationEventPublisher publisher;

    public void rembourserRetour(VenteBoutique vente, BigDecimal montant) {

        publier(
                vente,
                montant,
                TypeMouvement.REMBOURSEMENT_VENTE,
                SensMouvement.SORTIE,
                "Remboursement retour vente #" + vente.getNumero()
        );
    }

    public void complementPaiement(VenteBoutique vente, BigDecimal montant) {

        publier(
                vente,
                montant,
                TypeMouvement.COMPLEMENT_PAIEMENT_VENTE,
                SensMouvement.ENTREE,
                "Complément de paiement vente #" + vente.getNumero()
        );
    }

    public void rembourserAnnulation(VenteBoutique vente, String motif) {

        publier(
                vente,
                vente.getPaiement().getMontant(),
                TypeMouvement.REMBOURSEMENT_VENTE,
                SensMouvement.SORTIE,
                "Annulation vente #" + vente.getNumero()
                        + (motif == null || motif.isBlank()
                        ? ""
                        : " - " + motif)
        );
    }

    private void publier(
            VenteBoutique vente,
            BigDecimal montant,
            TypeMouvement type,
            SensMouvement sens,
            String libelle) {

        if (montant == null || montant.signum() <= 0) {
            return;
        }

        publisher.publishEvent(
                new MouvementCaisseEvent(
                        vente.getCaisseId(),
                        type,
                        sens,
                        vente.getPaiement().getModePaiement(),
                        montant,
                        libelle
                )
        );
    }
}