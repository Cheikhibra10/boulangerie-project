package com.boulangerie.rh.controller;

import com.boulangerie.rh.dto.CreatePresenceRequest;
import com.boulangerie.rh.dto.PresenceDto;
import com.boulangerie.rh.dto.PresenceFilter;
import com.boulangerie.rh.model.StatutPresence;
import com.boulangerie.rh.service.PresenceService;
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
@RequestMapping("/api/v1/presences")
@RequiredArgsConstructor
@Tag(name = "Présences", description = "Pointage journalier des employés (module RH)")
public class PresenceController {

    private final PresenceService presenceService;

    @GetMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @Operation(summary = "Rechercher les présences")
    @ApiResponse(responseCode = "200", description = "Liste paginée")
    public PageResponse<PresenceDto> search(
            @Parameter(description = "Nom ou prénom de l'employé")
            @RequestParam(required = false) String employeNom,
            @Parameter(description = "Date de début")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @Parameter(description = "Date de fin")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin,
            @Parameter(description = "Statut de présence")
            @RequestParam(required = false) StatutPresence statut,
            Pageable pageable
    ) {
        return PageUtils.toPageResponse(
                presenceService.search(
                        new PresenceFilter(employeNom, dateDebut, dateFin, statut),
                        pageable
                )
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @Operation(summary = "Détail d'une présence")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Présence trouvée"),
            @ApiResponse(responseCode = "404", description = "Présence introuvable")
    })
    public PresenceDto getById(@PathVariable Long id) {
        return presenceService.getById(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @Operation(summary = "Enregistrer une présence (pointage du jour)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Présence créée"),
            @ApiResponse(responseCode = "400", description = "Données invalides ou doublon")
    })
    public ResponseEntity<PresenceDto> create(@Valid @RequestBody CreatePresenceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(presenceService.create(request));
    }
}