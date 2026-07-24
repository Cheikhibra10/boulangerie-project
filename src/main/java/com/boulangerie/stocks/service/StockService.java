// stocks/service/StockService.java
package com.boulangerie.stocks.service;

import com.boulangerie.shared.dto.ConsommationIngredient;
import com.boulangerie.shared.dto.PageResponse;
import com.boulangerie.stocks.dto.*;
import com.boulangerie.stocks.model.TypeMouvementStock;

import java.math.BigDecimal;
import java.util.List;

public interface StockService {

    // ===== INGRÉDIENTS =====
    StockDetailDto getStockDetail(Long ingredientId, int page, int size);

    StockInitialDto creerStockInitial(Long periodeId, Long ingredientId, BigDecimal quantite, BigDecimal valeur);

    MouvementStockDto enregistrerMouvement(Long ingredientId, TypeMouvementStock type, BigDecimal quantite, String motif);

    void verifierDisponibiliteIngredients(List<ConsommationIngredient> consommations);
    StockIngredientDto modifierSeuilAlerte(Long stockId, SeuilAlerteRequest request);
    ConsommationResultDto consommerIngredients(Long lotId, List<ConsommationIngredient> consommations);

    void entrerStock(Long ingredientId, BigDecimal quantite, BigDecimal prixUnitaire, Long ligneAchatId);
    void entrerStock(List<StockEntry> entries);
    // ===== PRODUITS FINIS =====
    StockProduitDto getStockProduit(Long produitId);

    void augmenterStockProduit(Long produitId, BigDecimal quantite);

    void diminuerStockProduit(Long produitId, BigDecimal quantite);

    boolean verifierStockProduitSuffisant(Long produitId, BigDecimal quantite);
    void verifierStockSuffisant(Long produitId, BigDecimal quantite);
    // ===== SNAPSHOTS =====
    PageResponse<StockIngredientSnapshotDto> getSnapshots(Long periodeId, int page, int size);

    void decrementerStock(List<StockMovement> stocks);
    void incrementerStock(List<StockMovement> stocks);
}