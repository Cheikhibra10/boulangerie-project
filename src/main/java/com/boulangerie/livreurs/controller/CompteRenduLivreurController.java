// livreurs/controller/LivreurController.java
package com.boulangerie.livreurs.controller;

import com.boulangerie.livreurs.dto.*;
import com.boulangerie.livreurs.service.CompteRenduLivreurService;
import com.boulangerie.shared.dto.ApiResponse;
import com.boulangerie.shared.dto.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/comptes-rendus-livreurs")
@RequiredArgsConstructor
@Tag(name = "Compte Rendu Livreurs", description = "Gestion des comptes rendus livreurs")
public class CompteRenduLivreurController {

    private final CompteRenduLivreurService compteRenduLivreurService;

    // ===================== CRÉATION / RÉCUPÉRATION =====================

    @Operation(summary = "Créer ou récupérer le compte rendu d'un livreur pour une date")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<CompteRenduDto>> creerOuRecupererCompteRendu(
            @Valid @RequestBody CompteRenduCreationDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Compte rendu créé/récupéré",
                        compteRenduLivreurService.creerOuRecupererCompteRendu(dto)));
    }


    // ===================== CLÔTURE =====================

    @Operation(summary = "Clôturer le compte rendu d'un livreur")
    @PostMapping("/{id}/cloturer")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<CompteRenduDto>> cloturerCompteRendu(
            @PathVariable Long id,
            @Valid @RequestBody ClotureCompteRenduDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Compte rendu clôturé",
                        compteRenduLivreurService.cloturerCompteRendu(id, dto)));
    }

    // ===================== CONSULTATION =====================

    @Operation(summary = "Obtenir un compte rendu par ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<CompteRenduDto> getCompteRendu(@PathVariable Long id) {
        return ResponseEntity.ok(compteRenduLivreurService.getCompteRendu(id));
    }

    @Operation(summary = "Consulter l'historique des comptes rendus d'un livreur")
    @GetMapping("/{livreurId}/historique")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<PageResponse<CompteRenduDto>> getHistorique(
            @PathVariable Long livreurId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(compteRenduLivreurService.getHistorique(livreurId, dateDebut, dateFin, page, size));
    }
}