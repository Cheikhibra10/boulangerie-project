// caisse/controller/CaisseController.java
package com.boulangerie.comptabilite.controller;

import com.boulangerie.comptabilite.dto.*;
import com.boulangerie.comptabilite.model.StatutCaisse;
import com.boulangerie.comptabilite.service.CaisseService;
import com.boulangerie.comptabilite.service.MouvementCaisseService;
import com.boulangerie.shared.dto.ApiResponse;
import com.boulangerie.shared.dto.AutocompleteItemDto;
import com.boulangerie.shared.dto.PageResponse;
import com.boulangerie.shared.model.SensMouvement;
import com.boulangerie.shared.model.TypeMouvement;
import com.boulangerie.shared.utils.PageUtils;
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
@RequestMapping("/api/v1/caisses")
@RequiredArgsConstructor
@Tag(name = "Caisse", description = "Gestion des caisses ADMIN-MANAGER-CAISSIER")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CAISSIER')")
public class CaisseController {

    private final CaisseService caisseService;
    private final MouvementCaisseService mouvementService;

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
    @GetMapping("/{caisseId}/journal")
    public JournalCaisseDto getJournal(
            @PathVariable Long caisseId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin,
            @RequestParam(required = false) TypeMouvement type,
            @RequestParam(required = false) SensMouvement sens,
            @RequestParam(required = false) String libelle,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return caisseService.getJournal(
                caisseId, dateDebut, dateFin, type, sens, libelle, page, size
        );
    }

    @Operation(summary = "Rechercher des caisses")
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN', 'CAISSIER')")
    public PageResponse<CaisseDto> search(
            @RequestParam(required = false) StatutCaisse statut,
            Pageable pageable
    ) {
        CaisseFilter filter = new CaisseFilter(statut);
        return PageUtils.toPageResponse(caisseService.search(filter, pageable));
    }

    @Operation(summary = "Rechercher des caisses par Autocomplétion")
    @GetMapping("/autocomplete")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN', 'CAISSIER')")
    public List<AutocompleteItemDto> autocomplete(@RequestParam String q) {
        return caisseService.autocomplete(q);
    }

    @Operation(summary = "Rechercher des mouvements de caisse")
    @GetMapping("/mouvements/search")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public PageResponse<MouvementCaisseDto> search(
            @RequestParam(required = false) TypeMouvement typeMouvement,
            @RequestParam(required = false) SensMouvement sens,
            @RequestParam(required = false) String libelle,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin,
            @RequestParam(required = false) StatutCaisse caisseStatut,
            Pageable pageable
    ) {
        MouvementCaisseFilter filter = new MouvementCaisseFilter(
                typeMouvement,
                sens,
                libelle,
                dateDebut,
                dateFin,
                caisseStatut
        );
        return PageUtils.toPageResponse(mouvementService.search(filter, pageable));
    }

    @Operation(summary = "Rechercher des mouvements de caisse par Autocomplétion")
    @GetMapping("/mouvements/autocomplete")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public List<AutocompleteItemDto> autocompletemouvement(@RequestParam String q) {
        return mouvementService.autocomplete(q);
    }
}