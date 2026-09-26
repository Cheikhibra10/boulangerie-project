// shared/controller/GenericCrudController.java
package com.boulangerie.shared.controller;

import com.boulangerie.shared.dto.PageResponse;
import com.boulangerie.shared.service.DefaultService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
public abstract class GenericCrudController<T, D> {

    protected final DefaultService<T, D> service;

    // ===================== CREATE =====================
    @Operation(summary = "Créer une entité")
    @ApiResponse(responseCode = "201", description = "Créé avec succès")
    @ApiResponse(responseCode = "400", description = "Requête invalide")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    @PostMapping
    public ResponseEntity<D> create(@Valid @RequestBody D dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    // ===================== UPDATE =====================
    @Operation(summary = "Mettre à jour une entité")
    @ApiResponse(responseCode = "200", description = "Mise à jour avec succès")
    @ApiResponse(responseCode = "404", description = "Entité non trouvée")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<D> update(@PathVariable Long id, @RequestBody D dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    // ===================== PATCH =====================
    @Operation(summary = "Mettre à jour partiellement une entité")
    @ApiResponse(responseCode = "200", description = "Mise à jour partielle avec succès")
    @ApiResponse(responseCode = "404", description = "Entité non trouvée")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    @PatchMapping("/{id}")
    public ResponseEntity<D> patch(@PathVariable Long id, @RequestBody D dto) {
        return ResponseEntity.ok(service.patchFields(id, dto));
    }

    // ===================== GET ALL (paginé) =====================
    @Operation(summary = "Lister toutes les entités")
    @ApiResponse(responseCode = "200", description = "Liste récupérée avec succès")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    @GetMapping
    public ResponseEntity<PageResponse<D>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(service.findAll(page, size));
    }

    // ===================== GET BY ID =====================
    @Operation(summary = "Obtenir une entité par son ID")
    @ApiResponse(responseCode = "200", description = "Entité trouvée")
    @ApiResponse(responseCode = "404", description = "Entité non trouvée")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<D> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    // ===================== DELETE =====================
    @Operation(summary = "Supprimer une entité")
    @ApiResponse(responseCode = "200", description = "Supprimée avec succès")
    @ApiResponse(responseCode = "404", description = "Entité non trouvée")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<D> delete(@PathVariable Long id) {
        return ResponseEntity.ok(service.delete(id));
    }

    // ===================== ARCHIVE =====================
    @Operation(summary = "Archiver une entité (désactivation logique)")
    @ApiResponse(responseCode = "200", description = "Archivée avec succès")
    @ApiResponse(responseCode = "404", description = "Entité non trouvée")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    @PutMapping("/{id}/archive")
    public ResponseEntity<D> archive(@PathVariable Long id) {
        return ResponseEntity.ok(service.archive(id));
    }

    // ===================== RESTORE =====================
    @Operation(summary = "Restaurer une entité archivée")
    @ApiResponse(responseCode = "200", description = "Restaurée avec succès")
    @ApiResponse(responseCode = "404", description = "Entité non trouvée")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    @PutMapping("/{id}/restore")
    public ResponseEntity<D> restore(@PathVariable Long id) {
        return ResponseEntity.ok(service.restore(id));
    }
}