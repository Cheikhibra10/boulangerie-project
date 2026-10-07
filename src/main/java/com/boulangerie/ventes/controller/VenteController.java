// ventes/controller/VenteController.java
package com.boulangerie.ventes.controller;

import com.boulangerie.shared.dto.ApiResponse;
import com.boulangerie.shared.dto.AutocompleteItemDto;
import com.boulangerie.shared.dto.PageResponse;
import com.boulangerie.shared.utils.PageUtils;
import com.boulangerie.ventes.dto.*;
import com.boulangerie.ventes.model.StatutVente;
import com.boulangerie.ventes.service.VenteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/ventes")
@RequiredArgsConstructor
@Tag(name = "Ventes Boutique", description = "Gestion des ventes en boutique ADMIN-MANAGER-CAISSIER")
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

    @Operation(summary = "Retourner une vente")
    @PostMapping("/{venteId}/retours")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','CAISSIER')")
    public ResponseEntity<ApiResponse<VenteDto>> retourner(
            @PathVariable Long venteId,
            @Valid @RequestBody RetourVenteRequestDto dto) {
        VenteDto response = venteService.retournerVente(venteId, dto);

        return ResponseEntity.ok(ApiResponse.success("Retour traité avec succès", response));
    }

    @Operation(summary = "Annuler une vente")
    @PostMapping("/{venteId}/annulation")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER', 'CAISSIER')")
    public ResponseEntity<ApiResponse<VenteDto>> annuler(
            @PathVariable Long venteId,
            @Valid @RequestBody AnnulationVenteRequestDto dto) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Vente annulée avec succès",
                        venteService.annulerVente(venteId, dto)
                )
        );
    }

    @Operation(summary = "Rechercher des ventes")
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN', 'CAISSIER')")
    public PageResponse<VenteDto> search(
            @RequestParam(required = false) StatutVente statut,
            @RequestParam(required = false) String utilisateurNom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin,
            Pageable pageable
    ) {
        VenteBoutiqueFilter filter = new VenteBoutiqueFilter(statut, utilisateurNom, dateDebut, dateFin);
        return PageUtils.toPageResponse(venteService.search(filter, pageable));
    }

    @Operation(summary = "Rechercher des éléments pour l'auto-complétion")
    @GetMapping("/autocomplete")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN', 'CAISSIER')")
    public List<AutocompleteItemDto> autocomplete(@RequestParam String q) {
        return venteService.autocomplete(q);
    }
}