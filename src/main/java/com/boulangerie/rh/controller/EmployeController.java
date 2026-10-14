package com.boulangerie.rh.controller;

import com.boulangerie.rh.dto.*;
import com.boulangerie.rh.model.StatutEmploye;
import com.boulangerie.rh.service.EmployeService;
import com.boulangerie.shared.dto.AutocompleteItemDto;
import com.boulangerie.shared.dto.PageResponse;
import com.boulangerie.shared.utils.PageUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/employes")
@RequiredArgsConstructor
@Tag(name = "Employés", description = "Gestion des employés")
public class EmployeController {

    private final EmployeService employeService;

    @GetMapping
    @Operation(summary = "Rechercher employés par des filtres optionnels", description = "Rechercher employés par nom, prénom, matricule et statut. La pagination est supportée.")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public PageResponse<EmployeDto> search(
            @RequestParam(required = false) String nom,
            @RequestParam(required = false) String prenom,
            @RequestParam(required = false) String matricule,
            @RequestParam(required = false) StatutEmploye statut,
            Pageable pageable
    ) {
        return PageUtils.toPageResponse(
                employeService.search(new EmployeFilter(nom, prenom, matricule, statut), pageable)
        );
    }

    @GetMapping("/autocomplete")
    @Operation(summary = "Autocomplete des employés", description = "Obtenir une liste d'employés pour le complément automatique basé sur un terme de recherche.")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public List<AutocompleteItemDto> autocomplete(@RequestParam String q) {
        return employeService.autocomplete(q);
    }

    @PostMapping
    @Operation(summary = "Créer un employé", description = "Créer un nouvel employé avec les informations fournies.")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<EmployeDto> create(@Valid @RequestBody CreateEmployeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(employeService.create(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtenir un employé par ID", description = "Récupérer les détails d'un employé spécifique par son ID.")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public EmployeDto getById(@PathVariable Long id) {
        return employeService.getById(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour un employé", description = "Mettre à jour les informations d'un employé spécifique par son ID.")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public EmployeDto update(@PathVariable Long id, @Valid @RequestBody UpdateEmployeRequest request) {
        return employeService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Sortir un employé (statut SORTI)")
    public void delete(@PathVariable Long id) {
        employeService.delete(id);
    }
}