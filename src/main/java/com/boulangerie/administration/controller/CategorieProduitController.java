package com.boulangerie.administration.controller;

import com.boulangerie.administration.dto.CategorieProduitDto;
import com.boulangerie.administration.dto.RecetteDto;
import com.boulangerie.administration.model.CategorieProduit;
import com.boulangerie.administration.service.CategorieProduitService;
import com.boulangerie.shared.controller.GenericCrudController;
import com.boulangerie.shared.dto.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/categories-produit")
@Tag(name = "Catégories Produit", description = "Gestion des catégories de produits ADMIN-MANAGER")
@PreAuthorize("hasAnyRole('MANAGER','ADMIN', 'CAISSIER')")
public class CategorieProduitController
        extends GenericCrudController<CategorieProduit, CategorieProduitDto> {

    private final CategorieProduitService categorieProduitService;

    public CategorieProduitController(CategorieProduitService service) {
        super(service);
        this.categorieProduitService = service;
    }

    @Operation(summary = "Lister les catégories de produits")
    @ApiResponse( responseCode = "200", description = "Liste des catégories de produits récupérée avec succès" )
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN', 'CAISSIER')")
    @GetMapping
    public ResponseEntity<PageResponse<CategorieProduitDto>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok( categorieProduitService.findAll(page, size) );
    }

}