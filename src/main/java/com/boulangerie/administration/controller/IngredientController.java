package com.boulangerie.administration.controller;

import com.boulangerie.administration.dto.FournisseurDto;
import com.boulangerie.administration.dto.IngredientDto;
import com.boulangerie.administration.dto.IngredientFilter;
import com.boulangerie.administration.model.Ingredient;
import com.boulangerie.administration.model.UniteMesure;
import com.boulangerie.administration.service.IngredientAutocompleteService;
import com.boulangerie.administration.service.IngredientService;
import com.boulangerie.shared.controller.GenericCrudController;
import com.boulangerie.shared.dto.AutocompleteItemDto;
import com.boulangerie.shared.dto.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ingredients")
@Tag(name = "Ingrédients", description = "Gestion des ingrédients ADMIN-MANAGER")
@PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
public class IngredientController
        extends GenericCrudController<Ingredient, IngredientDto> {

    private final IngredientService ingredientService;
    private final IngredientAutocompleteService ingredientAutocompleteService;

    public IngredientController(IngredientService service, IngredientAutocompleteService ingredientAutocompleteService) {
        super(service);
        this.ingredientService = service;
        this.ingredientAutocompleteService = ingredientAutocompleteService;
    }

    @Operation(summary = "Lister les ingrédients")
    @ApiResponse( responseCode = "200", description = "Liste des ingrédients récupérée avec succès" )
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN', 'GESTIONNAIRE_PRODUCTION')")
    @GetMapping
    public ResponseEntity<PageResponse<IngredientDto>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok( ingredientService.findAll(page, size) );
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN', 'GESTIONNAIRE_PRODUCTION')")
    public ResponseEntity<PageResponse<IngredientDto>> search(
            @RequestParam(required = false) String libelle,
            @RequestParam(required = false) UniteMesure unite,
            @RequestParam(required = false) Boolean actif,
            Pageable pageable
            ){
        IngredientFilter filter = new IngredientFilter(libelle, unite, actif);
        return ResponseEntity.ok(ingredientService.search(filter, pageable));
    }


    @GetMapping("/autocomplete")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN', 'GESTIONNAIRE_PRODUCTION')")
    public List<AutocompleteItemDto> autocomplete(@RequestParam String q) {
        return ingredientAutocompleteService.search(q);
    }
}