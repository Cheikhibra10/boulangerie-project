package com.boulangerie.comptabilite.service;

import com.boulangerie.comptabilite.dto.AggregatedData;
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
public class ResultatCalculator {

    private static final BigDecimal PART_GERANT =
            FinancialConstants.PART_GERANT;

    private static final BigDecimal PART_BOULANGERIE =
            FinancialConstants.PART_BOULANGERIE;

    public ResultatData calculer(
            Periode periode,
            AggregatedData donnees) {

        donnees.valider();

        BigDecimal beneficeDistribuable = donnees.beneficeDistribuable();

        BigDecimal partGerant = beneficeDistribuable
                        .multiply(PART_GERANT)
                        .setScale(
                                FinancialConstants.FINANCIAL_SCALE,
                                FinancialConstants.FINANCIAL_ROUNDING);

        BigDecimal partBoulangerie =
                beneficeDistribuable
                        .multiply(PART_BOULANGERIE)
                        .setScale(
                                FinancialConstants.FINANCIAL_SCALE,
                                FinancialConstants.FINANCIAL_ROUNDING);

        return ResultatData.builder()
                .caAbonnements(donnees.getCaAbonnements())
                .caVentesLivreurs(donnees.getCaVentesLivreurs())
                .caVentesBoutique(donnees.getCaVentesBoutique())
                .caVenteRestants(donnees.getCaVenteRestants())
                .caAutresProduits(donnees.getCaAutresProduits())
                .totalCharges(donnees.getTotalCharges())
                .reliquatLivreurs(donnees.getReliquatLivreurs())
                .creditsClients(donnees.getCreditsClients())
                .partGerant(partGerant)
                .partBoulangerie(partBoulangerie)
                .build();
    }
}