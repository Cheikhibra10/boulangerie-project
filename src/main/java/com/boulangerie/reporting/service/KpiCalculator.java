// reporting//service/KpiCalculator.java
package com.boulangerie.reporting.service;

import com.boulangerie.abonnements.api.AbonnementStatisticsApi;
import com.boulangerie.achats.model.StatutAchat;
import com.boulangerie.achats.model.StatutPaiement;
import com.boulangerie.achats.model.StatutReception;
import com.boulangerie.achats.repository.AchatRepository;
import com.boulangerie.comptabilite.repository.MouvementCaisseRepository;
import com.boulangerie.livreurs.repository.CompteLivreurRepository;
import com.boulangerie.production.repository.LotProductionRepository;
import com.boulangerie.reporting.dto.*;
import com.boulangerie.stocks.repository.StockIngredientRepository;
import com.boulangerie.ventes.model.TypeVenteLigne;
import com.boulangerie.ventes.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class KpiCalculator {

    private final LigneVenteBoutiqueRepository ligneVenteRepository;
    private final VenteBoutiqueRepository venteRepository;
    private final CompteLivreurRepository compteLivreurRepository;
    private final AbonnementStatisticsApi abonnementStatisticsApi;
    private final StockIngredientRepository stockIngredientRepository;
    private final LotProductionRepository lotProductionRepository;
    private final AchatRepository achatRepository;
    private final MouvementCaisseRepository mouvementCaisseRepository;

    /**
     * Calcule les KPIs pour une période donnée
     */
    @Transactional(readOnly = true)
    public KpiDto calculerKPIs(LocalDate dateDebut, LocalDate dateFin) {
        // ===== CHIFFRE D'AFFAIRES =====
        BigDecimal caBoutique = ligneVenteRepository.sumCaBoutiqueBetweenDates(dateDebut, dateFin, TypeVenteLigne.NORMALE);
        BigDecimal caRestants = ligneVenteRepository.sumCaBoutiqueBetweenDates(dateDebut, dateFin, TypeVenteLigne.RESTANT);
        BigDecimal caLivreurs = compteLivreurRepository.sumCaLivreursBetweenDates(dateDebut, dateFin);
        BigDecimal caAbonnements = abonnementStatisticsApi.calculerCA(dateDebut, dateFin);

        BigDecimal caTotal = caBoutique
                .add(caRestants)
                .add(caLivreurs)
                .add(caAbonnements);

        // ===== PRODUCTION =====
        BigDecimal quantiteProduite = lotProductionRepository.sumQuantiteRealiseeBetweenDates(dateDebut, dateFin);
        BigDecimal quantiteVendue = ligneVenteRepository.sumQuantiteVendueBetweenDates(dateDebut, dateFin);
        Double tauxEcoulement = calculerTauxEcoulement(quantiteProduite, quantiteVendue);

        // ===== ALERTES STOCKS =====
        List<AlerteStockDto> alertes = stockIngredientRepository.findByQuantiteLessThanSeuilAlerte()
                .stream()
                .map(s -> AlerteStockDto.builder()
                        .ingredientId(s.getIngredient().getId())
                        .ingredientLibelle(s.getIngredient().getLibelle())
                        .quantiteActuelle(s.getQuantite())
                        .seuilAlerte(s.getSeuilAlerte())
                        .unite(s.getIngredient().getUnite())
                        .build())
                .toList();

        // ===== RELIQUAT LIVREURS =====
        BigDecimal reliquatTotal = compteLivreurRepository.sumSoldeActuel();

        // ===== CRÉDITS CLIENTS =====
        BigDecimal creditsClients = abonnementStatisticsApi.calculerCC();

        // ===== NOMBRES =====
        Integer nbAbonnementsActifs = abonnementStatisticsApi.calculerNActif();
        Integer nbLivreursActifs = compteLivreurRepository.countLivreursActifs();
        Integer nbIngredientsSousSeuil = stockIngredientRepository.countByQuantiteLessThanSeuilAlerte();

        // ===== MOUVEMENTS CAISSE =====
        BigDecimal totalEntrees = mouvementCaisseRepository.sumEntreesBetweenDates(dateDebut, dateFin);
        BigDecimal totalSorties = mouvementCaisseRepository.sumChargesBetweenDates(dateDebut, dateFin);
        BigDecimal soldeTheorique = totalEntrees.subtract(totalSorties);

        return KpiDto.builder()
                .caTotal(caTotal)
                .caBoutique(caBoutique)
                .caRestants(caRestants)
                .caLivreurs(caLivreurs)
                .caAbonnements(caAbonnements)
                .quantiteProduite(quantiteProduite)
                .quantiteVendue(quantiteVendue)
                .tauxEcoulement(tauxEcoulement)
                .alertesStocks(alertes)
                .nbIngredientsSousSeuil(nbIngredientsSousSeuil)
                .reliquatTotalLivreurs(reliquatTotal)
                .nbLivreursActifs(nbLivreursActifs)
                .creditsClientsTotal(creditsClients)
                .nbAbonnementsActifs(nbAbonnementsActifs)
                .totalEntrees(totalEntrees)
                .totalSorties(totalSorties)
                .soldeTheorique(soldeTheorique)
                .build();
    }

    private Double calculerTauxEcoulement(BigDecimal produit, BigDecimal vendu) {
        if (produit == null || produit.compareTo(BigDecimal.ZERO) == 0) {
            return 0.0;
        }
        return vendu.divide(produit, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();
    }

    /**
     * Calcule les top produits vendus
     */
    @Transactional(readOnly = true)
    public List<TopProduitDto> getTopProduits(LocalDate dateDebut, LocalDate dateFin, int limit) {
        return ligneVenteRepository.findTopProduitsByCa(dateDebut, dateFin, PageRequest.of(0, limit))
                .stream()
                .map(row -> TopProduitDto.builder()
                        .produitId((Long) row[0])
                        .produitNom((String) row[1])
                        .quantiteVendue((BigDecimal) row[2])
                        .caTotal((BigDecimal) row[3])
                        .build())
                .toList();
    }

    /**
     * Calcule l'évolution des ventes par jour
     */
    @Transactional(readOnly = true)
    public List<EvolutionVenteDto> getEvolutionVentes(LocalDate dateDebut, LocalDate dateFin) {
        return venteRepository.findCaByDateBetween(dateDebut, dateFin)
                .stream()
                .map(row -> EvolutionVenteDto.builder()
                        .date((LocalDate) row[0])
                        .montant((BigDecimal) row[1])
                        .build())
                .toList();
    }

    /**
     * Calcule les statistiques des achats par statut
     */
    @Transactional(readOnly = true)
    public List<StatutStatDto> getStatutsAchats() {
        return achatRepository.countAndSumByStatutAchat()
                .stream()
                .map(row -> StatutStatDto.builder()
                        .statut(((StatutAchat) row[0]).name())
                        .nombre((Long) row[1])
                        .montant((BigDecimal) row[2])
                        .build())
                .toList();
    }

    public List<StatutStatDto> getStatutsReception() {

        return achatRepository.countAndSumByStatutReception()
                .stream()
                .map(row -> StatutStatDto.builder()
                        .statut(((StatutReception) row[0]).name())
                        .nombre((Long) row[1])
                        .montant((BigDecimal) row[2])
                        .build())
                .toList();
    }

    public List<StatutStatDto> getStatutsPaiement() {

        return achatRepository.countAndSumByStatutPaiement()
                .stream()
                .map(row -> StatutStatDto.builder()
                        .statut(((StatutPaiement) row[0]).name())
                        .nombre((Long) row[1])
                        .montant((BigDecimal) row[2])
                        .build())
                .toList();
    }

    public AchatStatistiquesDto getStatistiques() {

        return AchatStatistiquesDto.builder()
                .achats(getStatutsAchats())
                .receptions(getStatutsReception())
                .paiements(getStatutsPaiement())
                .build();
    }
}