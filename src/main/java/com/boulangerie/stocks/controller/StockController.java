// stocks/controller/StockController.java
package com.boulangerie.stocks.controller;

import com.boulangerie.shared.dto.ApiResponse;
import com.boulangerie.shared.dto.PageResponse;
import com.boulangerie.stocks.dto.*;
import com.boulangerie.stocks.model.TypeMouvementStock;
import com.boulangerie.stocks.service.StockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/stocks")
@RequiredArgsConstructor
@Tag(name = "Gestion des Stocks", description = "API pour la gestion des stocks ADMIN-MANAGER-GESTIONNAIRE_PRODUCTION")
public class StockController {

    private final StockService stockService;

    // ===================== INGRÉDIENTS =====================

    @Operation(summary = "Consulter le stock d'un ingrédient avec historique paginé")
    @GetMapping("/ingredients/{id}/detail")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'GESTIONNAIRE_PRODUCTION')")
    public ResponseEntity<StockDetailDto> getStockDetail(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok((stockService.getStockDetail(id, page, size)));
    }

    @Operation(summary = "Enregistrer un stock initial pour un ingrédient")
    @PostMapping("/initial")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<StockInitialDto>> creerStockInitial(
            @RequestParam Long periodeId,
            @RequestParam Long ingredientId,
            @RequestParam @NotNull @Min(0) BigDecimal quantite,
            @RequestParam @NotNull @Min(0) BigDecimal valeur) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Stock initial enregistré",
                        stockService.creerStockInitial(periodeId, ingredientId, quantite, valeur)));
    }

    @PatchMapping("/ingredients/{id}/seuil")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Operation(summary = "Modifier le seuil d'alerte d'un stock")
    public ResponseEntity<StockIngredientDto> modifierSeuil(
            @PathVariable Long id,
            @Valid @RequestBody SeuilAlerteRequest request
    ) {
        return ResponseEntity.ok(stockService.modifierSeuilAlerte(id, request));
    }

    @Operation(summary = "Enregistrer un mouvement de stock")
    @PostMapping("/mouvements")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'GESTIONNAIRE_PRODUCTION')")
    public ResponseEntity<ApiResponse<MouvementStockDto>> enregistrerMouvement(
            @RequestParam Long ingredientId,
            @RequestParam TypeMouvementStock type,
            @RequestParam @NotNull @Min(0) BigDecimal quantite,
            @RequestParam(required = false) String motif) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Mouvement enregistré",
                        stockService.enregistrerMouvement(ingredientId, type, quantite, motif)));
    }

    // ===================== PRODUITS FINIS =====================

    @Operation(summary = "Consulter le stock d'un produit fini")
    @GetMapping("/produits/{produitId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CAISSIER', 'GESTIONNAIRE_PRODUCTION')")
    public ResponseEntity<StockProduitDto> getStockProduit(@PathVariable Long produitId) {
        return ResponseEntity.ok((stockService.getStockProduit(produitId)));
    }

    @Operation(summary = "Vérifier si le stock d'un produit est suffisant")
    @GetMapping("/produits/{produitId}/suffisant")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CAISSIER')")
    public ResponseEntity<Boolean> verifierStockSuffisant(
            @PathVariable Long produitId,
            @RequestParam @NotNull @Min(0) BigDecimal quantite) {
        return ResponseEntity.ok((stockService.verifierStockProduitSuffisant(produitId, quantite)));
    }

    // ===================== SNAPSHOTS =====================

    @Operation(summary = "Consulter les snapshots de stock d'une période")
    @GetMapping("/snapshots")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CAISSIER')")
    public ResponseEntity<PageResponse<StockIngredientSnapshotDto>> getSnapshots(
            @RequestParam Long periodeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok((stockService.getSnapshots(periodeId, page, size)));
    }

}