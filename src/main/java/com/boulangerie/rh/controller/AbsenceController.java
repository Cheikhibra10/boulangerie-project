package com.boulangerie.rh.controller;

import com.boulangerie.rh.dto.*;
import com.boulangerie.rh.model.*;
import com.boulangerie.rh.service.AbsenceService;
import com.boulangerie.rh.service.impl.AbsenceServiceImpl;
import com.boulangerie.shared.dto.PageResponse;
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

@RestController
@RequestMapping("/api/v1/absences")
@RequiredArgsConstructor
@Tag(name = "Absence", description = "API pour la gestion des absences")
public class AbsenceController {

    private final AbsenceService absenceService;

    @GetMapping
    @Operation(summary = "Recherche des absences avec des filtres et pagination")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public PageResponse<AbsenceDto> search(
            @RequestParam(required = false) String employeNom,
            @RequestParam(required = false) TypeAbsence type,
            @RequestParam(required = false) StatutAbsence statut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin,
            Pageable pageable
    ) {
        return PageUtils.toPageResponse(
                absenceService.search(
                        new AbsenceFilter(employeNom, type, statut, dateDebut, dateFin),
                        pageable
                )
        );
    }

    @PostMapping
    @Operation(summary = "Créer une absence", description = "Créer une nouvelle absence avec les informations fournies.")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<AbsenceDto> create(@Valid @RequestBody CreateAbsenceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(absenceService.create(request));
    }

    @PostMapping("/{id}/approuver")
    @Operation(summary = "Approuver une absence", description = "Approuver une absence spécifique par son ID.")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public AbsenceDto approuver(@PathVariable Long id) {
        return absenceService.approuver(id);
    }

    @PostMapping("/{id}/refuser")
    @Operation(summary = "Refuser une absence", description = "Refuser une absence spécifique par son ID.")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public AbsenceDto refuser(
            @PathVariable Long id,
            @RequestParam(required = false) String motif
    ) {
        return absenceService.refuser(id, motif);
    }
}