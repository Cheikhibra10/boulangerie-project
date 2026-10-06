package com.boulangerie.administration.controller;

import com.boulangerie.administration.dto.ProduitDto;
import com.boulangerie.administration.dto.RecetteDto;
import com.boulangerie.administration.dto.RecetteFilter;
import com.boulangerie.administration.model.Recette;
import com.boulangerie.administration.service.RecetteAutocompleteService;
import com.boulangerie.administration.service.RecetteService;
import com.boulangerie.shared.controller.GenericCrudController;
import com.boulangerie.shared.dto.AutocompleteItemDto;
import com.boulangerie.shared.dto.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/recettes")
@Tag(name = "Recettes", description = "Gestion des recettes de fabrication ADMIN-MANAGER")
@PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
public class RecetteController
        extends GenericCrudController<Recette, RecetteDto> {

    private final RecetteService recetteService;
    private final RecetteAutocompleteService recetteAutocompleteService;

    public RecetteController(RecetteService service, RecetteAutocompleteService recetteAutocompleteService) {
        super(service);
        this.recetteService = service;
        this.recetteAutocompleteService = recetteAutocompleteService;
    }

    @Operation(summary = "Lister les recettes")
    @ApiResponse(responseCode = "200", description = "Liste des recettes récupérée avec succès")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN', 'GESTIONNAIRE_PRODUCTION')")
    @GetMapping
    public ResponseEntity<PageResponse<RecetteDto>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(recetteService.findAll(page, size));
    }

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

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN', 'GESTIONNAIRE_PRODUCTION')")
    public ResponseEntity<PageResponse<RecetteDto>> search(
            @RequestParam(required = false) Integer version,
            @RequestParam(required = false) Boolean actif,
            @RequestParam(required = false) String produitNom,
            Pageable pageable
    ) {
        RecetteFilter filter = new RecetteFilter(version, actif, produitNom);
        return ResponseEntity.ok(recetteService.search(filter, pageable));
    }

    @GetMapping("/autocomplete")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN', 'GESTIONNAIRE_PRODUCTION')")
    public List<AutocompleteItemDto> autocomplete(@RequestParam String q) {
        return recetteAutocompleteService.search(q);
    }
}