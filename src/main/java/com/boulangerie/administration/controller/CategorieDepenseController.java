package com.boulangerie.administration.controller;

import com.boulangerie.administration.dto.CategorieDepenseDto;
import com.boulangerie.administration.dto.RecetteDto;
import com.boulangerie.administration.model.CategorieDepense;
import com.boulangerie.administration.service.CategorieDepenseService;
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
@RequestMapping("/api/v1/categories-depense")
@Tag(name = "Catégories Dépense", description = "Gestion des catégories de dépenses ADMIN-MANAGER")
@PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
public class CategorieDepenseController
        extends GenericCrudController<CategorieDepense, CategorieDepenseDto> {

    private final CategorieDepenseService categorieDepenseService;
    public CategorieDepenseController(CategorieDepenseService service) {
        super(service);
        this.categorieDepenseService = service;
    }

    @Operation(summary = "Lister les catégories de dépense")
    @ApiResponse( responseCode = "200", description = "Liste des catégories de dépense récupérée avec succès" )
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @GetMapping
    public ResponseEntity<PageResponse<CategorieDepenseDto>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok( categorieDepenseService.findAll(page, size) );
    }

}