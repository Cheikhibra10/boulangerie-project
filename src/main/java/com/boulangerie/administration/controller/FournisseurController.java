package com.boulangerie.administration.controller;

import com.boulangerie.administration.dto.FournisseurDto;
import com.boulangerie.administration.dto.FournisseurFilter;
import com.boulangerie.administration.model.Fournisseur;
import com.boulangerie.administration.service.FournisseurService;
import com.boulangerie.shared.controller.GenericCrudController;
import com.boulangerie.shared.dto.AutocompleteItemDto;
import com.boulangerie.shared.dto.PageResponse;
import com.boulangerie.shared.utils.PageUtils;
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
@RequestMapping("/api/v1/fournisseurs")
@Tag(name = "Fournisseurs", description = "Gestion des fournisseurs ADMIN-MANAGER")
@PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
public class FournisseurController
        extends GenericCrudController<Fournisseur, FournisseurDto> {

    private final FournisseurService fournisseurService;


    public FournisseurController(FournisseurService service) {
        super(service);
        this.fournisseurService = service;
    }

    @Operation(summary = "Lister les fournisseurs")
    @ApiResponse( responseCode = "200", description = "Liste des fournisseurs récupérée avec succès" )
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @GetMapping
    public ResponseEntity<PageResponse<FournisseurDto>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok( fournisseurService.findAll(page, size) );
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public PageResponse<FournisseurDto> search(
            @RequestParam(required = false) String nom,
            @RequestParam(required = false) String telephone,
            @RequestParam(required = false) Boolean actif,
            Pageable pageable
    ) {
        FournisseurFilter filter = new FournisseurFilter(nom, telephone, actif);
        return PageUtils.toPageResponse(fournisseurService.search(filter, pageable));
    }

    @GetMapping("/autocomplete")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public List<AutocompleteItemDto> autocomplete(@RequestParam String q) {
        return fournisseurService.autocomplete(q);
    }

}