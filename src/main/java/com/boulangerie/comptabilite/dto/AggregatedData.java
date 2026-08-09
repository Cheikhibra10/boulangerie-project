package com.boulangerie.comptabilite.dto;

import com.boulangerie.shared.exception.BadRequestException;
import lombok.*;

import java.math.BigDecimal;

@Value
@Builder
public class AggregatedData {

    BigDecimal caAbonnements;
    BigDecimal caVentesLivreurs;
    BigDecimal caVentesBoutique;
    BigDecimal caVenteRestants;
    BigDecimal caAutresProduits;

    BigDecimal totalCharges;

    BigDecimal reliquatLivreurs;
    BigDecimal creditsClients;

    public void valider() {

        verifier(caAbonnements);
        verifier(caVentesLivreurs);
        verifier(caVentesBoutique);
        verifier(caVenteRestants);
        verifier(caAutresProduits);
        verifier(totalCharges);
        verifier(reliquatLivreurs);
        verifier(creditsClients);
    }

    private void verifier(BigDecimal montant) {
        if (montant == null || montant.signum() < 0) {
            throw new BadRequestException("Tous les montants doivent être positifs ou nuls.");
        }
    }

    public BigDecimal chiffreAffairesTotal() {
        return caAbonnements
                .add(caVentesLivreurs)
                .add(caVentesBoutique)
                .add(caVenteRestants)
                .add(caAutresProduits);
    }

    public BigDecimal beneficeDistribuable() {

        return chiffreAffairesTotal()
                .subtract(totalCharges)
                .subtract(reliquatLivreurs)
                .subtract(creditsClients);
    }
}