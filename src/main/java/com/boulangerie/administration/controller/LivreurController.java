package com.boulangerie.administration.controller;

import com.boulangerie.administration.dto.LivreurDto;
import com.boulangerie.administration.dto.LivreurFilter;
import com.boulangerie.administration.model.Livreur;
import com.boulangerie.administration.service.LivreurService;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/livreurs")
@Tag(name = "Livreurs", description = "Gestion des livreurs ADMIN-MANAGER")
@PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
public class LivreurController
        extends GenericCrudController<Livreur, LivreurDto> {

    private final LivreurService livreurService;

    public LivreurController(LivreurService service) {
        super(service);
        this.livreurService = service;
    }

    @Operation(summary = "Lister les livreurs")
    @ApiResponse( responseCode = "200", description = "Liste des livreurs récupérée avec succès" )
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @GetMapping
    public ResponseEntity<PageResponse<LivreurDto>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok( livreurService.findAll(page, size));
    }
    @Operation(summary = "Lister tous les livreurs actifs")
    @GetMapping("/actifs")
    public ResponseEntity<List<LivreurDto>> findAllActive() {
        return ResponseEntity.ok(livreurService.findAllActive());
    }

    @Operation(summary = "Vérifier si un livreur est actif")
    @GetMapping("/{id}/is-active")
    public ResponseEntity<Boolean> isActive(@PathVariable Long id) {
        return ResponseEntity.ok(livreurService.isActive(id));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public PageResponse<LivreurDto> search(
            @RequestParam(required = false) String nom,
            @RequestParam(required = false) String prenom,
            @RequestParam(required = false) String telephone,
            @RequestParam(required = false) Boolean actif,
            Pageable pageable
    ) {
        LivreurFilter filter = new LivreurFilter(nom, prenom, telephone, actif);
        return PageUtils.toPageResponse(livreurService.search(filter, pageable));
    }

    @GetMapping("/autocomplete")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public List<AutocompleteItemDto> autocomplete(@RequestParam String q) {
        return livreurService.autocomplete(q);
    }

}