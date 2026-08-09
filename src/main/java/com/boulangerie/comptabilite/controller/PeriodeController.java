// comptabilite/api/controller/ComptabiliteController.java
package com.boulangerie.comptabilite.controller;

import com.boulangerie.comptabilite.dto.ComptabiliteStatistiquesDto;
import com.boulangerie.comptabilite.dto.PeriodeDto;
import com.boulangerie.comptabilite.dto.CreationPeriodeDto;
import com.boulangerie.comptabilite.dto.ResultatDto;
import com.boulangerie.comptabilite.service.*;
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
@RequestMapping("/api/periodes")
@RequiredArgsConstructor
@Tag(name = "Période", description = "Gestion des périodes comptables, clôtures et résultats ADMIN-MANAGER")
@PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
public class PeriodeController {

    private final PeriodeManagementService periodeManagementService;
    private final PeriodeClosureService periodeClosureService;
    private final ResultatQueryService resultatQueryService;

    @Operation(summary = "Créer une nouvelle période mensuelle ADMIN-MANAGER")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<PeriodeDto>> creerPeriode(@Valid @RequestBody CreationPeriodeDto dto) {
        PeriodeDto periode = periodeManagementService.creerPeriode(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Période créée avec succès", periode));
    }

    @Operation(summary = "Obtenir une période par ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<PeriodeDto> getPeriode(@PathVariable Long id) {
        return ResponseEntity.ok(periodeManagementService.getPeriode(id));
    }

    @Operation(summary = "Obtenir la période ouverte en cours")
    @GetMapping("/courante")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<PeriodeDto> getPeriodeOuverte() {
        return ResponseEntity.ok(periodeManagementService.getPeriodeOuverte());
    }

    @Operation(summary = "Obtenir la période pour une date donnée")
    @GetMapping("/date")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<PeriodeDto> getPeriodeByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(periodeManagementService.getPeriodeByDate(date));
    }

    @Operation(summary = "Lister toutes les périodes (paginated)")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<PageResponse<PeriodeDto>> getPeriodes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(periodeManagementService.getPeriodes(page, size));
    }

    @Operation(summary = "Ouvrir une période (réouverture)")
    @PatchMapping("/{id}/ouvrir")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<PeriodeDto>> ouvrirPeriode(@PathVariable Long id) {
        PeriodeDto periode = periodeManagementService.ouvrirPeriode(id);
        return ResponseEntity.ok(ApiResponse.success("Période ouverte avec succès", periode));
    }

    @Operation(summary = "Fermer une période (sans clôture)")
    @PatchMapping("/{id}/fermer")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<PeriodeDto>> fermerPeriode(@PathVariable Long id) {
        PeriodeDto periode = periodeManagementService.fermerPeriode(id);
        return ResponseEntity.ok(ApiResponse.success("Période fermée avec succès", periode));
    }

    @Operation(summary = "Clôturer une période et générer le résultat")
    @PostMapping("/{id}/cloturer")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<PeriodeDto>> cloturerPeriode(@PathVariable Long id) {
        PeriodeDto periode = periodeClosureService.cloturerPeriode(id);
        return ResponseEntity.ok(ApiResponse.success("Période clôturée avec succès", periode));
    }

    @Operation(summary = "Obtenir le résultat d'une période")
    @GetMapping("/resultats/{periodeId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ResultatDto> getResultat(@PathVariable Long periodeId) {
        return ResponseEntity.ok(resultatQueryService.getResultat(periodeId));
    }

    @Operation(summary = "Lister tous les résultats (paginated)")
    @GetMapping("/resultats")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<PageResponse<ResultatDto>> getResultats(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(resultatQueryService.getResultats(page, size));
    }



    @Operation(summary = "Obtenir les statistiques comptables")
    @GetMapping("/statistiques")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<ComptabiliteStatistiquesDto>> getStatistiques() {
        ComptabiliteStatistiquesDto stats = periodeManagementService.getStatistiques();
        return ResponseEntity.ok(ApiResponse.success("Statistiques récupérées", stats));
    }
}