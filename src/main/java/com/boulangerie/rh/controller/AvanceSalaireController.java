package com.boulangerie.rh.controller;

import com.boulangerie.rh.dto.AvanceSalaireDto;
import com.boulangerie.rh.dto.AvanceSalaireFilter;
import com.boulangerie.rh.dto.CreateAvanceRequest;
import com.boulangerie.rh.model.StatutAvance;
import com.boulangerie.rh.service.AvanceSalaireService;
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
@RequestMapping("/api/v1/avances-salaire")
@RequiredArgsConstructor
@Tag(name = "Avances sur salaire", description = "Demandes et validation des avances (module RH)")
public class AvanceSalaireController {

    private final AvanceSalaireService avanceSalaireService;

    @GetMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @Operation(summary = "Rechercher les avances sur salaire")
    @ApiResponse(responseCode = "200", description = "Liste paginée")
    public PageResponse<AvanceSalaireDto> search(
            @RequestParam(required = false) String employeNom,
            @RequestParam(required = false) StatutAvance statut,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin,
            Pageable pageable
    ) {
        return PageUtils.toPageResponse(
                avanceSalaireService.search(
                        new AvanceSalaireFilter(employeNom, statut, dateDebut, dateFin),
                        pageable
                )
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @Operation(summary = "Détail d'une avance")
    public AvanceSalaireDto getById(@PathVariable Long id) {
        return avanceSalaireService.getById(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @Operation(summary = "Créer une demande d'avance")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Avance créée (EN_ATTENTE)"),
            @ApiResponse(responseCode = "400", description = "Montant ou employé invalide")
    })
    public ResponseEntity<AvanceSalaireDto> create(@Valid @RequestBody CreateAvanceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(avanceSalaireService.create(request));
    }

    @PostMapping("/{id}/approuver")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @Operation(summary = "Approuver une avance (EN_ATTENTE → APPROUVEE)")
    public AvanceSalaireDto approuver(@PathVariable Long id) {
        return avanceSalaireService.approuver(id);
    }

    @PostMapping("/{id}/refuser")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @Operation(summary = "Refuser une avance (EN_ATTENTE → REFUSEE)")
    public AvanceSalaireDto refuser(@PathVariable Long id) {
        return avanceSalaireService.refuser(id);
    }

    @PostMapping("/{id}/annuler")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @Operation(summary = "Annuler une avance non déduite")
    public AvanceSalaireDto annuler(@PathVariable Long id) {
        return avanceSalaireService.annuler(id);
    }
}