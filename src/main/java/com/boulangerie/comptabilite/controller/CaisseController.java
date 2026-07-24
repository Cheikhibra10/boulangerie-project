// caisse/controller/CaisseController.java
package com.boulangerie.comptabilite.controller;

import com.boulangerie.comptabilite.dto.CaisseDto;
import com.boulangerie.comptabilite.dto.FermetureCaisseDto;
import com.boulangerie.comptabilite.dto.JournalCaisseDto;
import com.boulangerie.comptabilite.dto.OuvertureCaisseDto;
import com.boulangerie.comptabilite.dto.MouvementCaisseDto;
import com.boulangerie.comptabilite.service.CaisseService;
import com.boulangerie.shared.dto.ApiResponse;
import com.boulangerie.shared.dto.PageResponse;
import com.boulangerie.shared.model.TypePaiement;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/caisses")
@RequiredArgsConstructor
@Tag(name = "Caisse", description = "Gestion des caisses")
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
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<PageResponse<CaisseDto>> getCaisses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok((caisseService.getCaisses(page, size)));
    }

    // ===================== MOUVEMENTS =====================
    @Operation(summary = "Enregistrer un paiement en caisse")
    @PostMapping("/{id}/paiements")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CAISSIER')")
    public ResponseEntity<ApiResponse<MouvementCaisseDto>> enregistrerPaiement(
            @PathVariable Long id,
            @RequestParam @Valid BigDecimal montant,
            @RequestParam(required = false) String libelle,
            @RequestParam @Valid TypePaiement modePaiement) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Paiement enregistré",
                        caisseService.enregistrerPaiement(id, montant, libelle, modePaiement)));
    }

    @Operation(summary = "Enregistrer une dépense en caisse")
    @PostMapping("/{id}/depenses")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CAISSIER')")
    public ResponseEntity<ApiResponse<MouvementCaisseDto>> enregistrerDepense(
            @PathVariable Long id,
            @RequestParam Long categorieId,
            @RequestParam @Valid BigDecimal montant,
            @RequestParam String libelle) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Dépense enregistrée",
                        caisseService.enregistrerDepense(id, categorieId, montant, libelle)));
    }

    @Operation(summary = "Enregistrer un versement livreur en caisse")
    @PostMapping("/{id}/versements-livreur")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CAISSIER')")
    public ResponseEntity<ApiResponse<MouvementCaisseDto>> enregistrerVersementLivreur(
            @PathVariable Long id,
            @RequestParam Long livreurId,
            @RequestParam @Valid BigDecimal montant,
            @RequestParam @Valid TypePaiement modePaiement) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Versement livreur enregistré",
                        caisseService.enregistrerVersementLivreur(id, livreurId, montant, modePaiement)));
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