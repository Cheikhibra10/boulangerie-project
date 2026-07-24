package com.boulangerie.comptabilite.service;

import com.boulangerie.comptabilite.exception.PeriodeNonCloturableException;
import com.boulangerie.comptabilite.model.Periode;
import com.boulangerie.comptabilite.model.ResultatData;
import com.boulangerie.comptabilite.utils.FinancialConstants;
import com.boulangerie.shared.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResultatCalculator {

    private static final BigDecimal PART_GERANT = FinancialConstants.PART_GERANT;
    private static final BigDecimal PART_BOULANGERIE = FinancialConstants.PART_BOULANGERIE;

    public ResultatData calculer(
            Periode periode,
            BigDecimal caAbonnements,
            BigDecimal caVentesLivreurs,
            BigDecimal caVentesBoutique,
            BigDecimal caVenteRestants,
            BigDecimal caAutresProduits,
            BigDecimal totalCharges,
            BigDecimal reliquatLivreurs,
            BigDecimal creditsClients) {
        // 1. Validate period is closable
        if (!periode.estOuverte()) {
            throw new PeriodeNonCloturableException(periode.getId(), periode.getStatut());
        }
        // 2. Validate all amounts are non-negative
        validerMontants(caAbonnements, caVentesLivreurs, caVentesBoutique,
                caVenteRestants, caAutresProduits, totalCharges);
        // 3. Calculate totals
        BigDecimal caTotal = caAbonnements
                .add(caVentesLivreurs)
                .add(caVentesBoutique)
                .add(caVenteRestants)
                .add(caAutresProduits);
        BigDecimal beneficeBrut = caTotal.subtract(totalCharges);
        BigDecimal beneficeDistribuable = beneficeBrut
                .subtract(reliquatLivreurs)
                .subtract(creditsClients);
        // 4. Apply ownership split using constants
        BigDecimal partGerantCalculee = beneficeDistribuable.multiply(PART_GERANT)
                .setScale(FinancialConstants.FINANCIAL_SCALE, FinancialConstants.FINANCIAL_ROUNDING);
        BigDecimal partBoulangerieCalculee = beneficeDistribuable.multiply(PART_BOULANGERIE)
                .setScale(FinancialConstants.FINANCIAL_SCALE, FinancialConstants.FINANCIAL_ROUNDING);
        // 5. Return result
        return ResultatData.builder()
                .caAbonnements(caAbonnements)
                .caVentesLivreurs(caVentesLivreurs)
                .caVentesBoutique(caVentesBoutique)
                .caVenteRestants(caVenteRestants)
                .caAutresProduits(caAutresProduits)
                .totalCharges(totalCharges)
                .reliquatLivreurs(reliquatLivreurs)
                .creditsClients(creditsClients)
                .partGerant(partGerantCalculee)
                .partBoulangerie(partBoulangerieCalculee)
                .build();
    }

    private void validerMontants(BigDecimal... montants) {
        for (BigDecimal montant : montants) {
            if (montant == null || montant.compareTo(BigDecimal.ZERO) < 0) {
                throw new BadRequestException("Tous les montants doivent être positifs ou nuls");
            }
        }
    }
}