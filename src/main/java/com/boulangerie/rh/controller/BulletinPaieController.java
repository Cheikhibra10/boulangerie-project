package com.boulangerie.rh.controller;

import com.boulangerie.rh.dto.BulletinPaieDto;
import com.boulangerie.rh.dto.BulletinPaieFilter;
import com.boulangerie.rh.dto.GenererBulletinRequest;
import com.boulangerie.rh.model.StatutBulletin;
import com.boulangerie.rh.service.BulletinPaieService;
import com.boulangerie.shared.dto.PageResponse;
import com.boulangerie.shared.utils.PageUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

@RestController
@RequestMapping("/api/v1/bulletins-paie")
@RequiredArgsConstructor
@Tag(name = "Bulletins de paie", description = "Génération et suivi des bulletins de paie (module RH)")
public class BulletinPaieController {

    private final BulletinPaieService bulletinPaieService;

    @GetMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @Operation(summary = "Rechercher des bulletins de paie")
    @ApiResponse(responseCode = "200", description = "Liste paginée")
    public PageResponse<BulletinPaieDto> search(
            @Parameter(description = "Nom / prénom employé")
            @RequestParam(required = false) String employeNom,
            @Parameter(description = "ID de la période comptable")
            @RequestParam(required = false) Long periodeId,
            @Parameter(description = "Statut du bulletin")
            @RequestParam(required = false) StatutBulletin statut,
            Pageable pageable
    ) {
        return PageUtils.toPageResponse(
                bulletinPaieService.search(
                        new BulletinPaieFilter(employeNom, periodeId, statut),
                        pageable
                )
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @Operation(summary = "Détail d'un bulletin de paie")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Bulletin trouvé"),
            @ApiResponse(responseCode = "404", description = "Bulletin introuvable")
    })
    public BulletinPaieDto getById(@PathVariable Long id) {
        return bulletinPaieService.getById(id);
    }

    @PostMapping("/generer")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @Operation(
            summary = "Générer un bulletin de paie",
            description = """
                    Utilise le salaire de base du contrat actif.
                    Calcule joursAbsents / joursTravailles à partir des absences APPROUVEES
                    sur la période (snapshot). Un seul bulletin par employé et par période.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Bulletin généré (BROUILLON)"),
            @ApiResponse(responseCode = "400", description = "Contrat absent, période invalide ou doublon")
    })
    public ResponseEntity<BulletinPaieDto> generer(
            @Valid @RequestBody GenererBulletinRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(bulletinPaieService.generer(request));
    }

    @PostMapping("/{id}/valider")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @Operation(summary = "Valider un bulletin (BROUILLON → VALIDE)")
    public BulletinPaieDto valider(@PathVariable Long id) {
        return bulletinPaieService.valider(id);
    }

    @PostMapping("/{id}/payer")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @Operation(
            summary = "Marquer comme payé (VALIDE → PAYE)",
            description = "Phase 2: statut RH uniquement. Écriture caisse en Phase 3."
    )
    public BulletinPaieDto marquerPaye(
            @PathVariable Long id,
            @Parameter(description = "Date de paiement (défaut: aujourd'hui)")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate datePaiement
    ) {
        return bulletinPaieService.marquerPaye(id, datePaiement);
    }

    @PostMapping("/{id}/annuler")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @Operation(summary = "Annuler un bulletin non payé")
    public BulletinPaieDto annuler(@PathVariable Long id) {
        return bulletinPaieService.annuler(id);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un bulletin de paie")
    public void delete(@PathVariable Long id) {
        bulletinPaieService.delete(id);
    }
}