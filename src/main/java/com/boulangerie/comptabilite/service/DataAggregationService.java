package com.boulangerie.comptabilite.service;

import com.boulangerie.abonnements.api.AbonnementStatisticsApi;
import com.boulangerie.administration.model.TypeProduit;
import com.boulangerie.administration.service.ProduitService;
import com.boulangerie.comptabilite.dto.AggregatedData;
import com.boulangerie.comptabilite.model.Periode;
import com.boulangerie.comptabilite.api.FinanceStatistics;
import com.boulangerie.comptabilite.utils.ProduitCodes;
import com.boulangerie.livreurs.api.LivreurStatisticsApi;
import com.boulangerie.ventes.api.LigneVenteBoutiqueStatisticsApi;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DataAggregationService {


    private final AbonnementStatisticsApi abonnementStatisticsApi;
    private final LigneVenteBoutiqueStatisticsApi ligneVenteBoutiqueStatisticsApi;
    private final LivreurStatisticsApi livreurStatisticsApi;
    private final FinanceStatistics financeStatistics;
    private final ProduitService produitService;
    public AggregatedData agregerDonnees(Periode periode) {

        LocalDate debut = periode.getDateDebut();
        LocalDate fin = periode.getDateFin();
        Set<Long> pains = produitService.findIdsByType(TypeProduit.PAIN);

        return AggregatedData.builder()
                .caAbonnements(zero(abonnementStatisticsApi.calculerCA(debut, fin)))
                .caVentesLivreurs(zero(livreurStatisticsApi.calculerCAVentesLivreurs(debut, fin)))
                .caVentesBoutique(zero(ligneVenteBoutiqueStatisticsApi.calculerCAVentesBoutique(debut, fin)))
                .caVenteRestants(zero(ligneVenteBoutiqueStatisticsApi.calculerCAVenteRestants(debut, fin)))
                .caAutresProduits(zero(
                        ligneVenteBoutiqueStatisticsApi.calculerCAAutresProduits(
                                debut,
                                fin,
                                pains)))
                .totalCharges(zero(financeStatistics.calculerTotalCharges(debut, fin)))
                .reliquatLivreurs(zero(livreurStatisticsApi.calculerReliquatLivreurs()))
                .creditsClients(BigDecimal.ZERO)
                .build();
    }

    private BigDecimal zero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }


}