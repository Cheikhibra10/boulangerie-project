package com.boulangerie.comptabilite.service;

import com.boulangerie.abonnements.api.AbonnementStatisticsApi;
import com.boulangerie.administration.service.ProduitService;
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

@Service
@RequiredArgsConstructor
@Slf4j
public class DataAggregationService {
    private final AbonnementStatisticsApi abonnementStatisticsApi;
    private final LigneVenteBoutiqueStatisticsApi ligneVenteBoutiqueStatisticsApi;
    private final LivreurStatisticsApi livreurStatisticsApi;
    private final FinanceStatistics financeStatistics;
    private final ProduitService produitService;
    @Transactional(readOnly = true)
    public AggregatedData agregerDonnees(Periode periode) {
        LocalDate debut = periode.getDateDebut();
        LocalDate fin = periode.getDateFin();
        Collection<Long> excludedProductIds = List.of(
                produitService.getIdByIdLibelle(ProduitCodes.GP_1KG),
                produitService.getIdByIdLibelle(ProduitCodes.PP_DEMI_KG)
        );

        return AggregatedData.builder()
                .caAbonnements(calculerCAAbonnements(debut, fin))
                .caVentesLivreurs(calculerCAVentesLivreurs(debut, fin))
                .caVentesBoutique(calculerCAVentesBoutique(debut, fin))
                .caVenteRestants(calculerCAVenteRestants(debut, fin))
                .caAutresProduits(calculerCAAutresProduits(debut, fin, excludedProductIds))
                .totalCharges(calculerTotalCharges(debut, fin))
                .reliquatLivreurs(calculerReliquatLivreurs())
                .creditsClients(calculerCreditsClients())
                .build();
    }
    
    private BigDecimal calculerCAAbonnements(LocalDate debut, LocalDate fin) {
        return abonnementStatisticsApi.calculerCA(debut, fin);
    }
    
    private BigDecimal calculerCAVentesLivreurs(LocalDate debut, LocalDate fin) {
        return livreurStatisticsApi.calculerCAVentesLivreurs(debut, fin);
    }
    
    private BigDecimal calculerCAVentesBoutique(LocalDate debut, LocalDate fin) {
        return ligneVenteBoutiqueStatisticsApi.calculerCAVentesBoutique(debut, fin);
    }
    
    private BigDecimal calculerCAVenteRestants(LocalDate debut, LocalDate fin) {
        return ligneVenteBoutiqueStatisticsApi.calculerCAVenteRestants(debut, fin);
    }
    
    private BigDecimal calculerCAAutresProduits(LocalDate debut, LocalDate fin, Collection<Long> excludedProductIds) {
        return ligneVenteBoutiqueStatisticsApi.calculerCAAutresProduits(debut, fin, excludedProductIds);
    }
    
    private BigDecimal calculerTotalCharges(LocalDate debut, LocalDate fin) {
        return financeStatistics.calculerTotalCharges(debut, fin);
    }
    
    private BigDecimal calculerReliquatLivreurs() {
        return livreurStatisticsApi.calculerReliquatLivreurs();
    }
    
    private BigDecimal calculerCreditsClients() {
        // V1: No credit system yet
        return BigDecimal.ZERO;
    }
    
    @Value
    @Builder
    public static class AggregatedData {
        BigDecimal caAbonnements;
        BigDecimal caVentesLivreurs;
        BigDecimal caVentesBoutique;
        BigDecimal caVenteRestants;
        BigDecimal caAutresProduits;
        BigDecimal totalCharges;
        BigDecimal reliquatLivreurs;
        BigDecimal creditsClients;
    }
}