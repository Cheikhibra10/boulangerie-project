// ventes/controller/VenteController.java
package com.boulangerie.ventes.controller;

import com.boulangerie.shared.dto.ApiResponse;
import com.boulangerie.shared.dto.PageResponse;
import com.boulangerie.ventes.dto.*;
import com.boulangerie.ventes.service.VenteService;
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
@RequestMapping("/api/ventes")
@RequiredArgsConstructor
@Tag(name = "Ventes Boutique", description = "Gestion des ventes en boutique")
public class VenteController {

    private final VenteService venteService;

    // ===================== CRÉATION =====================

    @Operation(summary = "Créer une fiche de vente")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CAISSIER')")
    public ResponseEntity<ApiResponse<VenteDto>> creerVente(@Valid @RequestBody VenteRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Fiche de vente créée", venteService.creerVente(dto)));
    }


    // ===================== INVENTAIRE =====================

    @Operation(summary = "Enregistrer l'inventaire des restants (fin de journée)")
    @PostMapping("/{id}/inventaire")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CAISSIER')")
    public ResponseEntity<ApiResponse<InventaireResultDto>> enregistrerRestants(
            @PathVariable Long id,
            @Valid @RequestBody InventaireDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Inventaire enregistré", venteService.enregistrerRestants(id, dto)));
    }

    // ===================== CONSULTATION =====================

    @Operation(summary = "Obtenir une vente par ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CAISSIER')")
    public ResponseEntity<VenteDto> getVente(@PathVariable Long id) {
        return ResponseEntity.ok(venteService.getVente(id));
    }

    @Operation(summary = "Consulter les ventes (paginé)")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CAISSIER')")
    public ResponseEntity<PageResponse<VenteDto>> getVentes(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @RequestParam(required = false) Long produitId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(venteService.getVentes(dateDebut, produitId, page, size));
    }
}