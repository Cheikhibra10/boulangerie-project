package com.boulangerie.administration.controller;

import com.boulangerie.administration.dto.LivreurDto;
import com.boulangerie.administration.model.Livreur;
import com.boulangerie.administration.service.LivreurService;
import com.boulangerie.shared.controller.GenericCrudController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
}