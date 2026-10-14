package com.boulangerie.rh.controller;

import com.boulangerie.rh.dto.*;
import com.boulangerie.rh.model.StatutContrat;
import com.boulangerie.rh.model.TypeContrat;
import com.boulangerie.rh.service.ContratService;
import com.boulangerie.shared.dto.AutocompleteItemDto;
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
import java.util.List;

@RestController
@RequestMapping("/api/v1/contrats")
@RequiredArgsConstructor
@Tag(name = "Contrats", description = "Gestion des contrats de travail (RH)")
public class ContratController {

    private final ContratService contratService;

    @GetMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @Operation(summary = "Rechercher des contrats", description = "Filtre par employé, type et statut")
    @ApiResponse(responseCode = "200", description = "Liste paginée des contrats")
    public PageResponse<ContratDto> search(
            @Parameter(description = "Nom ou prénom de l'employé") @RequestParam(required = false) String employeNom,
            @Parameter(description = "Type de contrat") @RequestParam(required = false) TypeContrat type,
            @Parameter(description = "Statut du contrat") @RequestParam(required = false) StatutContrat statut,
            Pageable pageable
    ) {
        return PageUtils.toPageResponse(
                contratService.search(new ContratFilter(employeNom, type, statut), pageable)
        );
    }

    @GetMapping("/autocomplete")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @Operation(summary = "Autocomplete contrats actifs")
    public List<AutocompleteItemDto> autocomplete(
            @Parameter(description = "Recherche (nom employé)", required = true) @RequestParam String q
    ) {
        return contratService.autocomplete(q);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @Operation(summary = "Détail d'un contrat")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Contrat trouvé"),
            @ApiResponse(responseCode = "404", description = "Contrat introuvable")
    })
    public ContratDto getById(@PathVariable Long id) {
        return contratService.getById(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @Operation(summary = "Créer un contrat", description = "Un seul contrat ACTIF par employé")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Contrat créé"),
            @ApiResponse(responseCode = "400", description = "Données invalides ou contrat actif existant")
    })
    public ResponseEntity<ContratDto> create(@Valid @RequestBody CreateContratRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(contratService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @Operation(summary = "Mettre à jour un contrat")
    public ContratDto update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateContratRequest request
    ) {
        return contratService.update(id, request);
    }

    @PostMapping("/{id}/resilier")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @Operation(summary = "Résilier un contrat actif")
    public ContratDto resilier(
            @PathVariable Long id,
            @Parameter(description = "Date de fin effective") @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin
    ) {
        return contratService.resilier(id, dateFin);
    }
}