package com.boulangerie.abonnements.service;

import com.boulangerie.abonnements.model.LigneAbonnement;
import org.springframework.stereotype.Component;
import lombok.Value;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

// abonnements/domain/service/ReliquatCalculator.java
@Component
public class ReliquatCalculator {

    public BigDecimal calculerNouveauReliquat(LigneAbonnement ligne, BigDecimal montantPaye) {
        return ligne.getReliquat().subtract(montantPaye);
    }

    public VersementRepartition repartirVersement(
            List<LigneAbonnement> clientsEndettes,
            BigDecimal montantTotal) {

        BigDecimal montantRestant = montantTotal;
        List<RepartitionLigne> repartitions = new ArrayList<>();

        for (LigneAbonnement ligne : clientsEndettes) {
            if (montantRestant.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }

            BigDecimal dette = ligne.getReliquat();
            BigDecimal deduction = montantRestant.min(dette);

            repartitions.add(new RepartitionLigne(ligne, deduction));
            montantRestant = montantRestant.subtract(deduction);
        }

        return new VersementRepartition(repartitions, montantRestant);
    }

    @Value
    public static class RepartitionLigne {
        LigneAbonnement ligne;
        BigDecimal deduction;
    }

    @Value
    public static class VersementRepartition {
        List<RepartitionLigne> repartitions;
        BigDecimal montantRestant;
    }
}