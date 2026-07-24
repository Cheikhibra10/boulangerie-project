// abonnements/controller/AbonnementController.java
package com.boulangerie.abonnements.controller;

import com.boulangerie.abonnements.dto.*;
import com.boulangerie.abonnements.service.AbonnementService;
import com.boulangerie.shared.dto.ApiResponse;
import com.boulangerie.shared.dto.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/abonnements")
@RequiredArgsConstructor
@Tag(name = "Abonnements", description = "Gestion des abonnements clients")
@PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
public class AbonnementController {

    private final AbonnementService abonnementService;

    // ===================== ABONNEMENT =====================

    @Operation(summary = "Créer un abonnement")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<AbonnementDto>> creerAbonnement(
            @Valid @RequestBody CreationAbonnementDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Abonnement créé", abonnementService.creerAbonnement(dto)));
    }

    @Operation(summary = "Ajouter un client à un abonnement")
    @PostMapping("/{id}/clients")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<LigneAbonnementDto>> ajouterClient(
            @PathVariable Long id,
            @Valid @RequestBody CreationClientAbonnementDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Client ajouté", abonnementService.ajouterClient(id, dto)));
    }

    // ===================== CONSOMMATION =====================

    @Operation(summary = "Enregistrer une consommation journalière")
    @PostMapping("/lignes/{id}/consommations")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<Void>> enregistrerConsommation(
            @PathVariable Long id,
            @Valid @RequestBody ConsommationDto dto) {
        abonnementService.enregistrerConsommation(id, dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Consommation enregistrée", null));
    }

    // ===================== PAIEMENT CLIENT =====================

    @Operation(summary = "Enregistrer un paiement client")
    @PostMapping("/lignes/{id}/paiements")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<PaiementClientResultDto>> enregistrerPaiementClient(
            @PathVariable Long id,
            @Valid @RequestBody PaiementClientDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Paiement enregistré",
                        abonnementService.enregistrerPaiementClient(id, dto)));
    }

    // ===================== VERSEMENT GLOBAL =====================

    @Operation(summary = "Enregistrer un versement global pour un abonnement")
    @PostMapping("/{id}/versements")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<VersementGlobalResultDto>> enregistrerVersementGlobal(
            @PathVariable Long id,
            @Valid @RequestBody VersementGlobalDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Versement global enregistré",
                        abonnementService.enregistrerVersementGlobal(id, dto)));
    }

    // ===================== CONSULTATION =====================

    @Operation(summary = "Obtenir un abonnement par ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<AbonnementDto> getAbonnement(@PathVariable Long id) {
        return ResponseEntity.ok(abonnementService.getAbonnement(id));
    }

    @Operation(summary = "Lister les abonnements (paginé)")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<PageResponse<AbonnementDto>> getAbonnements(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(abonnementService.getAbonnements(page, size));
    }
}