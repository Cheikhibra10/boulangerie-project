// caisse/controller/CaisseController.java
package com.boulangerie.comptabilite.controller;

import com.boulangerie.comptabilite.dto.CaisseDto;
import com.boulangerie.comptabilite.dto.FermetureCaisseDto;
import com.boulangerie.comptabilite.dto.JournalCaisseDto;
import com.boulangerie.comptabilite.dto.OuvertureCaisseDto;
import com.boulangerie.comptabilite.service.CaisseService;
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
@RequestMapping("/api/v1/caisses")
@RequiredArgsConstructor
@Tag(name = "Caisse", description = "Gestion des caisses ADMIN-MANAGER-CAISSIER")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CAISSIER')")
public class CaisseController {

    private final CaisseService caisseService;

    // ===================== OUVERTURE / FERMETURE =====================
    @Operation(summary = "Ouvrir une caisse")
    @PostMapping("/ouvrir")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CAISSIER')")
    public ResponseEntity<ApiResponse<CaisseDto>> ouvrirCaisse(@Valid @RequestBody OuvertureCaisseDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Caisse ouverte avec succès", caisseService.ouvrirCaisse(dto)));
    }

    @Operation(summary = "Fermer une caisse")
    @PostMapping("/{id}/fermer")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CAISSIER')")
    public ResponseEntity<ApiResponse<CaisseDto>> fermerCaisse(
            @PathVariable Long id,
            @Valid @RequestBody FermetureCaisseDto dto) {
        return ResponseEntity.ok(ApiResponse.success("Caisse fermée avec succès", caisseService.fermerCaisse(id, dto)));
    }

    @Operation(summary = "Obtenir la caisse en cours")
    @GetMapping("/courante")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CAISSIER')")
    public ResponseEntity<CaisseDto>
    getCaisseEnCours() {
        return ResponseEntity.ok((caisseService.getCaisseEnCours()));
    }

    @Operation(summary = "Obtenir une caisse par ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CAISSIER')")
    public ResponseEntity<CaisseDto> getCaisse(@PathVariable Long id) {
        return ResponseEntity.ok((caisseService.getCaisse(id)));
    }

    @Operation(summary = "Lister toutes les caisses (paginé)")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CAISSIER')")
    public ResponseEntity<PageResponse<CaisseDto>> getCaisses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok((caisseService.getCaisses(page, size)));
    }


    // ===================== JOURNAL =====================
    @Operation(summary = "Consulter le journal de caisse (paginé)")
    @GetMapping("/{id}/journal")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CAISSIER')")
    public ResponseEntity<JournalCaisseDto> getJournal(
            @PathVariable Long id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Long categorieId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok((
                caisseService.getJournal(id, dateDebut, dateFin, type, categorieId, page, size)));
    }
}