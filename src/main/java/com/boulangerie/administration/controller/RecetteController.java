// administration/controller/RecetteController.java
package com.boulangerie.administration.controller;

import com.boulangerie.administration.dto.RecetteDto;
import com.boulangerie.administration.model.Recette;
import com.boulangerie.administration.service.RecetteService;
import com.boulangerie.shared.controller.GenericCrudController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/recettes")
@Tag(name = "Recettes", description = "Gestion des recettes de fabrication")
@PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
public class RecetteController
        extends GenericCrudController<Recette, RecetteDto> {

    private final RecetteService recetteService;

    public RecetteController(RecetteService service) {
        super(service);
        this.recetteService = service;
    }

    // ===================== MÉTHODES SPÉCIFIQUES =====================

    @Operation(summary = "Obtenir la recette active d'un produit")
    @GetMapping("/produit/{produitId}/active")
    public ResponseEntity<RecetteDto> getActiveByProduitId(@PathVariable Long produitId) {
        return ResponseEntity.ok(recetteService.getActiveByProduitId(produitId));
    }

    @Operation(summary = "Vérifier si un produit a une recette active")
    @GetMapping("/produit/{produitId}/has-active")
    public ResponseEntity<Boolean> hasActiveRecette(@PathVariable Long produitId) {
        return ResponseEntity.ok(recetteService.hasActiveRecette(produitId));
    }

    @Operation(summary = "Activer ou désactiver une recette")
    @PatchMapping("/{id}/active")
    public ResponseEntity<RecetteDto> setActive(
            @PathVariable Long id,
            @RequestParam boolean actif) {
        return ResponseEntity.ok(recetteService.setActive(id, actif));
    }
}